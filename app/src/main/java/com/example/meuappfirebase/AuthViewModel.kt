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
import java.text.SimpleDateFormat
import java.util.*

/**
 * Classe de dados para representar o estado da UI (Carregando, Erro, Sucesso).
 * Usada para comunicar o status das operações do ViewModel para as Activities.
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel principal que gerencia toda a lógica de autenticação e perfil de usuário.
 * Ele conversa com o Firebase (Auth e Firestore) e também com o banco de dados local (Room).
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = Firebase.auth
    private val firestore = Firebase.firestore
    // Acesso ao DB Room completo
    private val appDb = AppDatabase.getDatabase(application)
    private val userDao = appDb.userDao()
    private val habitoDao = appDb.habitoDao()

    // O mapa de hábitos agora vive aqui, junto com a lógica de negócio.
    private val mapaDeHabitosRuins = mapOf(
        "Fumar" to "🚭 Fumar Menos",
        "Beber" to "🚱 Não Beber",
        "Sono ruim" to "😴 Dormir Melhor",
        "Procrastinação" to "✅ Não Procrastinar",
        "Uso excessivo do celular" to "📵 Usar Menos o Celular"
    )

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /**
     * Registra um novo usuário no Firebase Auth e envia um e-mail de verificação.
     */
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

    /**
     * Faz o login de um usuário e verifica no Firestore se o perfil dele está completo.
     */
    fun login(
        email: String,
        pass: String,
        onLoginSuccess: (profileIsComplete: Boolean) -> Unit
    ) {
        _uiState.value = AuthUiState(isLoading = true)
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        firestore.collection("usuarios").document(user.uid).get()
                            .addOnSuccessListener { document ->
                                val profileIsComplete = document != null && document.exists() && !document.getString("nome").isNullOrEmpty()
                                onLoginSuccess(profileIsComplete)
                                _uiState.value = AuthUiState(isLoading = false)
                            }
                            .addOnFailureListener {
                                onLoginSuccess(false)
                                _uiState.value = AuthUiState(isLoading = false)
                            }
                    } else {
                        auth.signOut()
                        _uiState.value = AuthUiState(isLoading = false, error = "Por favor, verifique seu e-mail antes de fazer login.")
                    }
                } else {
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        error = task.exception?.message ?: "E-mail ou senha inválidos."
                    )
                }
            }
    }

    /**
     * Salva o perfil do usuário (tela infousuario) no Firestore e no Room.
     */
    fun saveUserProfileToFirestoreAndRoom(
        firestoreProfile: UserProfile,
        roomUser: User,
        onSuccess: () -> Unit
    ) {
        _uiState.value = AuthUiState(isLoading = true)
        firestore.collection("usuarios").document(firestoreProfile.uid)
            .set(firestoreProfile)
            .addOnSuccessListener {
                Log.d("Sync", "Perfil salvo com sucesso no Firestore.")
                viewModelScope.launch {
                    userDao.insertUser(roomUser)
                    Log.d("Sync", "Perfil salvo com sucesso no Room DB.")
                    _uiState.value = AuthUiState(isLoading = false)
                    onSuccess()
                }
            }
            .addOnFailureListener { e ->
                _uiState.value = AuthUiState(
                    isLoading = false,
                    error = "Erro ao salvar perfil na nuvem: ${e.message}"
                )
            }
    }

    /**
     * Salva os hábitos iniciais do usuário (tela livro) no Firestore e no Room.
     */
    fun saveUserInitialHabits(
        ler: Boolean,
        dieta: Boolean,
        seguirDieta: Boolean,
        onSuccess: () -> Unit
    ) {
        _uiState.value = AuthUiState(isLoading = true)
        val user = auth.currentUser
        if (user == null) {
            _uiState.value = AuthUiState(isLoading = false, error = "Nenhum usuário logado.")
            return
        }
        val habitsMap = mapOf(
            "temHabitoLeitura" to ler,
            "segueDieta" to dieta,
            "gostariaSeguirDieta" to seguirDieta
        )
        firestore.collection("usuarios").document(user.uid)
            .update(habitsMap)
            .addOnSuccessListener {
                Log.d("Sync", "Hábitos iniciais salvos no Firestore.")
                viewModelScope.launch {
                    val roomUser = userDao.getUserById(user.uid)
                    roomUser?.let {
                        it.temHabitoLeitura = ler
                        it.segueDieta = dieta
                        it.gostariaSeguirDieta = seguirDieta
                        userDao.updateUser(it)
                        Log.d("Sync", "Hábitos iniciais atualizados no Room.")
                    }
                    _uiState.value = AuthUiState(isLoading = false)
                    onSuccess()
                }
            }
            .addOnFailureListener { e ->
                _uiState.value = AuthUiState(
                    isLoading = false,
                    error = "Erro ao salvar hábitos: ${e.message}"
                )
            }
    }

    /**
     * CORRIGIDO: Salva as respostas de saúde mental (tela saudemental) no perfil do usuário
     * E CRIA os hábitos correspondentes diretamente na coleção 'habitos' do Firestore.
     */
    fun saveMentalHealthAndCreateLocalHabits(
        habitosParaMudar: List<String>,
        problemasEmocionais: List<String>,
        onSuccess: () -> Unit
    ) {
        _uiState.value = AuthUiState(isLoading = true)
        val user = auth.currentUser
        if (user == null) {
            _uiState.value = AuthUiState(isLoading = false, error = "Nenhum usuário logado.")
            return
        }
        val mentalHealthMap = mapOf(
            "habitosParaMudar" to habitosParaMudar,
            "problemasEmocionais" to problemasEmocionais
        )
        // Primeiro, atualiza o perfil do usuário com as respostas
        firestore.collection("usuarios").document(user.uid)
            .update(mentalHealthMap)
            .addOnSuccessListener {
                Log.d("Sync", "Dados de saúde mental salvos no Firestore.")
                // Em seguida, cria os hábitos na coleção 'habitos', que é a fonte de dados da sua tela de hábitos.
                viewModelScope.launch {
                    val novasMetas = habitosParaMudar.mapNotNull { mapaDeHabitosRuins[it] }
                    if (novasMetas.isEmpty()) {
                        Log.d("Sync", "Nenhum novo hábito para criar a partir do questionário.")
                        _uiState.value = AuthUiState(isLoading = false)
                        onSuccess()
                        return@launch
                    }

                    // Usamos um WriteBatch para salvar todos os novos hábitos de uma só vez de forma eficiente
                    val batch = firestore.batch()
                    val allDays = listOf("SUN","MON","TUE","WED","THU","FRI","SAT")

                    novasMetas.forEach { nomeDoHabito ->
                        // Cria uma referência para um novo documento com ID automático na coleção 'habitos'
                        val novoHabitoDocRef = firestore.collection("habitos").document()
                        val habitoData = hashMapOf(
                            "userOwnerId" to user.uid,
                            "nome" to nomeDoHabito,
                            "isFavorito" to false,
                            "isGoodHabit" to false, // Os hábitos a serem mudados são "ruins" (a meta é superá-los)
                            "diasProgramados" to allDays,
                            "progresso" to emptyList<String>()
                        )
                        batch.set(novoHabitoDocRef, habitoData)
                    }

                    // Envia todas as operações para o Firestore de uma vez
                    batch.commit()
                        .addOnSuccessListener {
                            Log.d("Sync", "${novasMetas.size} novos hábitos criados com sucesso no Firestore.")
                            _uiState.value = AuthUiState(isLoading = false)
                            onSuccess()
                        }
                        .addOnFailureListener { e ->
                            _uiState.value = AuthUiState(isLoading = false, error = "Erro ao criar hábitos no Firestore: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                _uiState.value = AuthUiState(
                    isLoading = false,
                    error = "Erro ao salvar dados de saúde mental na nuvem: ${e.message}"
                )
            }
    }

    /**
     * Salva as preferências de treino do usuário (tela pergunta01) no Firestore e no Room.
     */
    fun saveWorkoutPreferences(
        praticaAtividade: String,
        tempoDisponivel: String,
        espacos: List<String>,
        onSuccess: () -> Unit
    ) {
        _uiState.value = AuthUiState(isLoading = true)
        val user = auth.currentUser
        if (user == null) {
            _uiState.value = AuthUiState(isLoading = false, error = "Nenhum usuário logado.")
            return
        }
        val preferencesMap = mapOf(
            "praticaAtividade" to praticaAtividade,
            "tempoDisponivel" to tempoDisponivel,
            "espacosDisponiveis" to espacos
        )
        firestore.collection("usuarios").document(user.uid)
            .update(preferencesMap)
            .addOnSuccessListener {
                Log.d("Sync", "Preferências de treino salvas no Firestore.")
                viewModelScope.launch {
                    val roomUser = userDao.getUserById(user.uid)
                    roomUser?.let {
                        it.praticaAtividade = praticaAtividade
                        it.tempoDisponivel = tempoDisponivel
                        it.espacosDisponiveis = espacos
                        userDao.updateUser(it)
                        Log.d("Sync", "Preferências de treino atualizadas no Room.")
                    }
                    _uiState.value = AuthUiState(isLoading = false)
                    onSuccess()
                }
            }
            .addOnFailureListener { e ->
                _uiState.value = AuthUiState(
                    isLoading = false,
                    error = "Erro ao salvar preferências: ${e.message}"
                )
            }
    }

    /**
     * Salva as preferências de sugestão do usuário (tela sujestao) no Firestore e no Room.
     */
    fun saveSuggestionPreferences(
        interesses: List<String>,
        onSuccess: () -> Unit
    ) {
        _uiState.value = AuthUiState(isLoading = true)
        val user = auth.currentUser
        if (user == null) {
            _uiState.value = AuthUiState(isLoading = false, error = "Nenhum usuário logado.")
            return
        }
        val preferencesMap = mapOf("sugestoesInteresse" to interesses)
        firestore.collection("usuarios").document(user.uid)
            .update(preferencesMap)
            .addOnSuccessListener {
                Log.d("Sync", "Preferências de sugestão salvas no Firestore.")
                viewModelScope.launch {
                    val roomUser = userDao.getUserById(user.uid)
                    roomUser?.let {
                        it.sugestoesInteresse = interesses
                        userDao.updateUser(it)
                        Log.d("Sync", "Preferências de sugestão atualizadas no Room.")
                    }
                    _uiState.value = AuthUiState(isLoading = false)
                    onSuccess()
                }
            }
            .addOnFailureListener { e ->
                _uiState.value = AuthUiState(
                    isLoading = false,
                    error = "Erro ao salvar preferências: ${e.message}"
                )
            }
    }

    /**
     * Envia um e-mail de redefinição de senha para o usuário logado.
     */
    fun sendPasswordResetEmail(onSuccess: () -> Unit) {
        val user = auth.currentUser
        if (user?.email == null) {
            _uiState.value = AuthUiState(error = "Nenhum usuário logado ou e-mail associado.")
            return
        }
        auth.sendPasswordResetEmail(user.email!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    _uiState.value = AuthUiState(error = "Falha ao enviar e-mail: ${task.exception?.message}")
                }
            }
    }

    /**
     * Faz o logout do usuário no Firebase.
     */
    fun logout() {
        auth.signOut()
    }

    /**
     * Retorna o usuário atualmente logado no Firebase.
     */
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    /**
     * Retorna o usuário atualmente salvo no Room.
     */
    suspend fun getCurrentUserFromRoom(): User? {
        return userDao.getCurrentUser()
    }
}