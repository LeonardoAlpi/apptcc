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

    // --- INÍCIO DA MUDANÇA (ETAPA 3) ---
    fun salvarDadosEtapa3(habitos: List<String>, problemas: List<String>) {
        viewModelScope.launch {
            try {
                val user = getCurrentUserFromRoom()
                user?.let {
                    // 1. Atualiza os dados no objeto User
                    it.habitosNegativos = habitos
                    it.problemasEmocionais = problemas
                    it.onboardingStep = 4
                    updateUser(it) // 2. Salva o objeto User (isso sobrescreve, está CORRETO)

                    val userId = it.userId
                    if (userId == null) {
                        _onboardingStepUpdated.value = true
                        return@launch
                    }

                    // 3. Deleta os hábitos antigos criados pelo questionário
                    deleteQuestionnaireHabits(userId) {
                        // 4. Somente após deletar, cria os novos hábitos
                        createHabitsFromQuestionnaire(habitos) {
                            // 5. Sinaliza que a etapa terminou
                            _onboardingStepUpdated.value = true
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState(error = "Erro ao salvar: ${e.message}")
            }
        }
    }
    // --- FIM DA MUDANÇA (ETAPA 3) ---

    // --- INÍCIO DA MUDANÇA (ETAPA 4) ---
    fun salvarDadosEtapa4(pratica: String, tempo: String, espacos: List<String>) {
        viewModelScope.launch {
            try {
                val user = getCurrentUserFromRoom()
                user?.let {
                    // 1. Atualiza os dados no objeto User
                    it.praticaAtividade = pratica
                    it.tempoDisponivel = tempo
                    it.espacosDisponiveis = espacos
                    it.onboardingStep = 5

                    updateUser(it) // 2. Salva o objeto User (sobrescreve, CORRETO)

                    val userId = it.userId
                    if (userId == null) {
                        Log.e("WorkoutGenerator", "FALHA: UserID nulo.")
                        _onboardingStepUpdated.value = true // Ainda avança
                        return@launch
                    }

                    // 3. VERIFICA se os treinos já foram gerados
                    firestore.collection("treinos")
                        .whereEqualTo("userOwnerId", userId)
                        .limit(1) // Só precisamos saber se existe pelo menos 1
                        .get()
                        .addOnSuccessListener { snapshot ->
                            if (snapshot.isEmpty) {
                                // 4A. NÃO TEM TREINOS: Roda o gerador pela primeira vez
                                Log.d("WorkoutGenerator", "Nenhum treino encontrado. Gerando...")
                                viewModelScope.launch {
                                    gerarTreinos(it) // Chama a nova função extraída
                                    _onboardingStepUpdated.value = true // Avança após gerar
                                }
                            } else {
                                // 4B. JÁ TEM TREINOS: Pula a geração
                                Log.d("WorkoutGenerator", "Treinos já existem. Geração pulada.")
                                _onboardingStepUpdated.value = true // Avança direto
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("WorkoutGenerator", "Falha ao checar treinos. Pulando.", e)
                            _onboardingStepUpdated.value = true // Avança mesmo com falha
                        }
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState(error = "Erro ao salvar: ${e.message}")
            }
        }
    }
    // --- FIM DA MUDANÇA (ETAPA 4) ---

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

    // --- INÍCIO DA NOVA FUNÇÃO (ETAPA 3) ---
    /**
     * Deleta todos os hábitos do questionário (isGoodHabit == false)
     * antes de criar os novos, para evitar duplicatas.
     */
    private fun deleteQuestionnaireHabits(userId: String, onSuccess: () -> Unit) {
        firestore.collection("habitos")
            .whereEqualTo("userOwnerId", userId)
            .whereEqualTo("isGoodHabit", false) // Deleta APENAS os hábitos ruins gerados aqui
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    Log.d("DeleteHabits", "Nenhum hábito antigo para deletar.")
                    onSuccess() // Nada para deletar, continua
                    return@addOnSuccessListener
                }

                val batch = firestore.batch()
                snapshot.documents.forEach { doc ->
                    batch.delete(doc.reference) // Adiciona a exclusão ao batch
                }

                batch.commit()
                    .addOnSuccessListener {
                        Log.d("DeleteHabits", "${snapshot.size()} hábitos antigos deletados.")
                        onSuccess() // Continua após deletar
                    }
                    .addOnFailureListener { e ->
                        Log.e("DeleteHabits", "Falha ao deletar hábitos antigos.", e)
                        onSuccess() // Continua mesmo com falha para não travar o usuário
                    }
            }
            .addOnFailureListener { e ->
                Log.e("DeleteHabits", "Falha ao buscar hábitos antigos.", e)
                onSuccess() // Continua mesmo com falha para não travar o usuário
            }
    }
    // --- FIM DA NOVA FUNÇÃO (ETAPA 3) ---

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
                    onSuccess() // Continua mesmo com falha
                }
        }
    }

    // --- INÍCIO DA NOVA FUNÇÃO (ETAPA 4) ---
    /**
     * Lógica de geração de treino, extraída da Etapa 4 para ser
     * chamada condicionalmente, evitando duplicatas.
     */
    private suspend fun gerarTreinos(user: User) {
        try {
            val peso = user.peso ?: 0f
            val altura = user.altura ?: 0f
            val imc = if (altura > 0) peso / (altura * altura) else 0f
            val pratica = user.praticaAtividade ?: "Não"
            val tempo = user.tempoDisponivel ?: "Menos de 30 minutos"
            val espacos = user.espacosDisponiveis ?: listOf("Casa")
            val userId = user.userId!! // Já foi checado na Etapa 4

            val generator = WorkoutGenerator()
            val treinosCompletos = generator.gerarTreinos(
                pratica = pratica,
                tempo = tempo,
                espacos = espacos,
                imc = imc,
                userId = userId
            )

            val treinoDao = appDb.treinoDao()
            val batch = firestore.batch()
            Log.d("WorkoutGeneratorSync", "Iniciando batch de sincronização para Firestore...")

            treinosCompletos.forEach { generatedWorkout ->
                val treinoParaSalvarRoom = generatedWorkout.treino.copy(userOwnerId = userId)
                val novoTreinoId = treinoDao.insertTreino(treinoParaSalvarRoom)
                val treinoParaSalvarFirestore = treinoParaSalvarRoom.copy(id = novoTreinoId)
                val treinoDocRef = firestore.collection("treinos").document(novoTreinoId.toString())
                batch.set(treinoDocRef, treinoParaSalvarFirestore)

                generatedWorkout.divisoes.forEach { generatedDivision ->
                    val divisaoParaSalvarRoom = generatedDivision.divisao.copy(
                        treinoId = novoTreinoId,
                        userOwnerId = userId
                    )
                    val novaDivisaoId = treinoDao.insertDivisao(divisaoParaSalvarRoom)
                    val divisaoParaSalvarFirestore = divisaoParaSalvarRoom.copy(id = novaDivisaoId)
                    val divisaoDocRef = firestore.collection("divisoes_treino").document(novaDivisaoId.toString())
                    batch.set(divisaoDocRef, divisaoParaSalvarFirestore)

                    generatedDivision.notas.forEach { nota ->
                        val notaParaSalvarRoom = nota.copy(
                            divisaoId = novaDivisaoId,
                            userOwnerId = userId
                        )
                        val novaNotaId = treinoDao.insertTreinoNota(notaParaSalvarRoom)
                        val notaParaSalvarFirestore = notaParaSalvarRoom.copy(id = novaNotaId)
                        val notaDocRef = firestore.collection("treino_notas").document(novaNotaId.toString())
                        batch.set(notaDocRef, notaParaSalvarFirestore)
                    }
                }
            }

            batch.commit()
                .addOnSuccessListener {
                    Log.d("WorkoutGeneratorSync", "SUCESSO: Batch de treinos gerados salvo no Firestore.")
                }
                .addOnFailureListener { e ->
                    Log.e("WorkoutGeneratorSync", "FALHA: Erro ao salvar batch de treinos no Firestore.", e)
                    _uiState.value = AuthUiState(error = "Treinos salvos localmente, mas falha ao sincronizar: ${e.message}")
                }

            Log.d("WorkoutGenerator", "SUCESSO: ${treinosCompletos.size} treinos completos salvos no Room.")
        } catch (e: Exception) {
            Log.e("WorkoutGenerator", "Erro catastrófico ao gerar treinos.", e)
            _uiState.value = AuthUiState(error = "Erro ao gerar treinos: ${e.message}")
        }
    }
    // --- FIM DA NOVA FUNÇÃO (ETAPA 4) ---

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