package com.example.meuappfirebase

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.apol.myapplication.AppDatabase
import com.apol.myapplication.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = Firebase.auth
    private val firestore = Firebase.firestore
    private val appDb = AppDatabase.getDatabase(application)
    private val userDao = appDb.userDao()

    private val mapaDeHabitosRuins = mapOf(
        "Fumar" to "🚭 Fumar Menos",
        "Beber" to "🚱 Não Beber",
        "Sono ruim" to "😴 Dormir Melhor",
        "Procrastinação" to "✅ Não Procrastinar",
        "Uso excessivo do celular" to "📵 Usar Menos o Celular"
    )

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _onboardingStepUpdated = MutableStateFlow(false)
    val onboardingStepUpdated: StateFlow<Boolean> = _onboardingStepUpdated.asStateFlow()

    fun signUp(email: String, pass: String, onSuccess: () -> Unit) {
        _uiState.value = AuthUiState(isLoading = true)
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result?.user?.sendEmailVerification()
                        ?.addOnCompleteListener { verificationTask ->
                            if (verificationTask.isSuccessful) {
                                Log.d("EmailVerification", "E-mail de verificação enviado com sucesso.")
                                _uiState.value = AuthUiState(isLoading = false)
                                onSuccess()
                            } else {
                                Log.w("EmailVerification", "Falha ao enviar e-mail.", verificationTask.exception)
                                _uiState.value = AuthUiState(isLoading = false, error = "Falha ao enviar e-mail de verificação.")
                            }
                        }
                } else {
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        error = task.exception?.message ?: "Ocorreu um erro no cadastro."
                    )
                }
            }
    }

    fun login(email: String, pass: String, onLoginSuccess: () -> Unit) {
        _uiState.value = AuthUiState(isLoading = true)
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        onLoginSuccess()
                    } else {
                        auth.signOut()
                        _uiState.value = AuthUiState(isLoading = false, error = "Por favor, verifique seu e-mail antes de fazer login.")
                    }
                } else {
                    _uiState.value = AuthUiState(isLoading = false, error = task.exception?.message ?: "E-mail ou senha inválidos.")
                }
            }
    }

    fun syncUserProfileOnLogin(onSyncComplete: () -> Unit) {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            onSyncComplete()
            return
        }
        _uiState.value = AuthUiState(isLoading = true)
        firestore.collection("usuarios").document(firebaseUser.uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val firestoreProfile = document.toObject(User::class.java)
                    if (firestoreProfile != null) {
                        viewModelScope.launch {
                            userDao.insertUser(firestoreProfile)
                            Log.d("Sync", "Perfil encontrado e salvo no Room.")
                            _uiState.value = AuthUiState(isLoading = false)
                            onSyncComplete()
                        }
                    } else {
                        onSyncComplete()
                    }
                } else {
                    val newUser = User(
                        userId = firebaseUser.uid,
                        email = firebaseUser.email,
                        onboardingStep = 1
                    )
                    firestore.collection("usuarios").document(firebaseUser.uid).set(newUser)
                        .addOnSuccessListener {
                            viewModelScope.launch {
                                userDao.insertUser(newUser)
                                Log.d("Sync", "Novo perfil inicial criado no Firestore e Room.")
                                _uiState.value = AuthUiState(isLoading = false)
                                onSyncComplete()
                            }
                        }
                        .addOnFailureListener { e ->
                            _uiState.value = AuthUiState(isLoading = false, error = "Falha ao criar perfil: ${e.message}")
                            onSyncComplete()
                        }
                }
            }
            .addOnFailureListener { e ->
                _uiState.value = AuthUiState(isLoading = false, error = "Falha ao buscar dados: ${e.message}")
                onSyncComplete()
            }
    }

    fun salvarDadosEtapa1(nome: String, idade: Int, peso: Float, altura: Float, genero: String) {
        viewModelScope.launch {
            try {
                val user = getCurrentUserFromRoom()
                user?.let {
                    it.nome = nome
                    it.idade = idade
                    it.peso = peso
                    it.altura = altura
                    it.genero = genero
                    it.onboardingStep = 2
                    updateUser(it)
                    _onboardingStepUpdated.value = true
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState(error = "Erro ao salvar: ${e.message}")
            }
        }
    }

    fun salvarDadosEtapa2(temHabitoLeitura: Boolean, segueDieta: Boolean, gostariaSeguirDieta: Boolean) {
        viewModelScope.launch {
            try {
                val user = getCurrentUserFromRoom()
                user?.let {
                    it.temHabitoLeitura = temHabitoLeitura
                    it.segueDieta = segueDieta
                    it.gostariaSeguirDieta = gostariaSeguirDieta
                    it.onboardingStep = 3
                    updateUser(it)
                    _onboardingStepUpdated.value = true
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState(error = "Erro ao salvar: ${e.message}")
            }
        }
    }

    fun salvarDadosEtapa3(habitos: List<String>, problemas: List<String>) {
        viewModelScope.launch {
            try {
                val user = getCurrentUserFromRoom()
                user?.let {
                    it.habitosNegativos = habitos
                    it.problemasEmocionais = problemas
                    it.onboardingStep = 4
                    updateUser(it)

                    createHabitsFromQuestionnaire(habitos) {
                        _onboardingStepUpdated.value = true
                    }
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState(error = "Erro ao salvar: ${e.message}")
            }
        }
    }

    fun salvarDadosEtapa4(pratica: String, tempo: String, espacos: List<String>) {
        viewModelScope.launch {
            try {
                val user = getCurrentUserFromRoom()
                user?.let {
                    it.praticaAtividade = pratica
                    it.tempoDisponivel = tempo
                    it.espacosDisponiveis = espacos
                    it.onboardingStep = 5

                    updateUser(it) // Salva os dados da Etapa 4

                    if (it.userId != null) {

                        // 2. Calcular o IMC
                        val peso = it.peso ?: 0f
                        val altura = it.altura ?: 0f
                        val imc = if (altura > 0) peso / (altura * altura) else 0f

                        // 3. Obter dados com valores padrão
                        val pratica = it.praticaAtividade ?: "Não"
                        val tempo = it.tempoDisponivel ?: "Menos de 30 minutos"
                        val espacos = it.espacosDisponiveis ?: listOf("Casa")
                        val userId = it.userId!! // Garantido pelo 'if'

                        // 4. Instanciar e chamar o Gerador
                        val generator = WorkoutGenerator()
                        val treinosCompletos = generator.gerarTreinos(
                            pratica = pratica,
                            tempo = tempo,
                            espacos = espacos,
                            imc = imc,
                            userId = userId
                        )

                        // 5. Salvar a estrutura completa no Room E no Firestore
                        val treinoDao = appDb.treinoDao()
                        val batch = firestore.batch() // (NOVO) Batch para o Firestore

                        Log.d("WorkoutGeneratorSync", "Iniciando batch de sincronização para Firestore...")

                        treinosCompletos.forEach { generatedWorkout ->

                            // --- 5.1 SALVAR TREINO ---
                            // Room: Salva e obtém o ID
                            val treinoParaSalvarRoom = generatedWorkout.treino.copy(userOwnerId = userId)
                            val novoTreinoId = treinoDao.insertTreino(treinoParaSalvarRoom)

                            // Firestore: Prepara o batch com o ID do Room
                            val treinoParaSalvarFirestore = treinoParaSalvarRoom.copy(id = novoTreinoId)
                            val treinoDocRef = firestore.collection("treinos").document(novoTreinoId.toString())
                            batch.set(treinoDocRef, treinoParaSalvarFirestore)

                            generatedWorkout.divisoes.forEach { generatedDivision ->

                                // --- 5.2 SALVAR DIVISÃO ---
                                // Room: Prepara, salva e obtém o ID
                                val divisaoParaSalvarRoom = generatedDivision.divisao.copy(
                                    treinoId = novoTreinoId,
                                    userOwnerId = userId
                                )
                                val novaDivisaoId = treinoDao.insertDivisao(divisaoParaSalvarRoom)

                                // Firestore: Prepara o batch com o ID do Room
                                val divisaoParaSalvarFirestore = divisaoParaSalvarRoom.copy(id = novaDivisaoId)
                                val divisaoDocRef = firestore.collection("divisoes_treino").document(novaDivisaoId.toString())
                                batch.set(divisaoDocRef, divisaoParaSalvarFirestore)

                                generatedDivision.notas.forEach { nota ->

                                    // --- 5.3 SALVAR NOTA ---
                                    // Room: Prepara, salva e obtém o ID (graças à mudança no DAO)
                                    val notaParaSalvarRoom = nota.copy(
                                        divisaoId = novaDivisaoId,
                                        userOwnerId = userId
                                    )
                                    val novaNotaId = treinoDao.insertTreinoNota(notaParaSalvarRoom)

                                    // Firestore: Prepara o batch com o ID do Room
                                    val notaParaSalvarFirestore = notaParaSalvarRoom.copy(id = novaNotaId)
                                    val notaDocRef = firestore.collection("treino_notas").document(novaNotaId.toString())
                                    batch.set(notaDocRef, notaParaSalvarFirestore)
                                }
                            }
                        }

                        // (NOVO) 5.6. Executa todo o salvamento no Firestore de uma vez
                        batch.commit()
                            .addOnSuccessListener {
                                Log.d("WorkoutGeneratorSync", "SUCESSO: Batch de treinos gerados salvo no Firestore.")
                            }
                            .addOnFailureListener { e ->
                                Log.e("WorkoutGeneratorSync", "FALHA: Erro ao salvar batch de treinos no Firestore.", e)
                                // Os dados ainda estão salvos localmente (Room)
                                _uiState.value = AuthUiState(error = "Treinos salvos localmente, mas falha ao sincronizar: ${e.message}")
                            }

                        Log.d("WorkoutGenerator", "SUCESSO: ${treinosCompletos.size} treinos completos salvos no Room.")

                    } else {
                        Log.e("WorkoutGenerator", "FALHA: UserID nulo. Nenhum treino foi gerado.")
                    }
                    // --- FIM DA GERAÇÃO DE TREINO (V2.1 - COM SYNC FIREBASE) ---

                    _onboardingStepUpdated.value = true
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState(error = "Erro ao salvar: ${e.message}")
            }
        }
    }

    fun salvarDadosEtapa5(interesses: List<String>) {
        viewModelScope.launch {
            try {
                val user = getCurrentUserFromRoom()
                user?.let {
                    it.sugestoesInteresse = interesses
                    it.onboardingStep = 6
                    updateUser(it)
                    _onboardingStepUpdated.value = true
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState(error = "Erro ao salvar: ${e.message}")
            }
        }
    }

    private suspend fun updateUser(user: User) {
        userDao.updateUser(user)
        Log.d("UpdateUser", "Usuário atualizado no Room.")
        user.userId?.let { uid ->
            firestore.collection("usuarios").document(uid).set(user)
                .addOnSuccessListener {
                    Log.d("UpdateUser", "Usuário atualizado com sucesso no Firestore.")
                }
                .addOnFailureListener { e ->
                    Log.e("UpdateUser", "Erro ao atualizar usuário no Firestore.", e)
                    _uiState.value = AuthUiState(error = "Erro ao sincronizar com a nuvem: ${e.message}")
                }
        }
    }

    private fun createHabitsFromQuestionnaire(habitosParaMudar: List<String>, onSuccess: () -> Unit) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            val novasMetas = habitosParaMudar.mapNotNull { mapaDeHabitosRuins[it] }
            if (novasMetas.isEmpty()) {
                onSuccess()
                return@launch
            }

            val batch = firestore.batch()
            val allDays = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
            novasMetas.forEach { nomeDoHabito ->
                val novoHabitoDocRef = firestore.collection("habitos").document()
                val habitoData = hashMapOf(
                    "userOwnerId" to user.uid, "nome" to nomeDoHabito, "isFavorito" to false,
                    "isGoodHabit" to false, "diasProgramados" to allDays, "progresso" to emptyList<String>()
                )
                batch.set(novoHabitoDocRef, habitoData)
            }

            batch.commit()
                .addOnSuccessListener {
                    Log.d("Sync", "${novasMetas.size} novos hábitos criados no Firestore.")
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    _uiState.value = AuthUiState(isLoading = false, error = "Erro ao criar hábitos: ${e.message}")
                }
        }
    }

    fun resetOnboardingStepUpdated() {
        _onboardingStepUpdated.value = false
    }

    fun sendPasswordResetEmail(onSuccess: () -> Unit) {
        val user = auth.currentUser
        if (user?.email == null) {
            _uiState.value = AuthUiState(error = "Nenhum usuário logado ou e-mail associado.")
            return
        }
        _uiState.value = AuthUiState(isLoading = true)
        auth.sendPasswordResetEmail(user.email!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("PasswordReset", "E-mail de redefinição enviado com sucesso.")
                    _uiState.value = AuthUiState(isLoading = false)
                    onSuccess()
                } else {
                    Log.w("PasswordReset", "Falha ao enviar e-mail.", task.exception)
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        error = "Falha ao enviar e-mail: ${task.exception?.message}"
                    )
                }
            }
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    suspend fun getCurrentUserFromRoom(): User? {
        return auth.currentUser?.uid?.let { userDao.getUserById(it) }
    }
}