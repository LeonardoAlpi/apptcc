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

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = Firebase.auth
    private val firestore = Firebase.firestore
    private val appDb = AppDatabase.getDatabase(application)
    private val userDao = appDb.userDao()
    private val habitoDao = appDb.habitoDao()

    private val mapaDeHabitosRuins = mapOf(
        "Fumar" to "🚭 Fumar Menos",
        "Beber" to "🚱 Não Beber",
        "Sono ruim" to "😴 Dormir Melhor",
        "Procrastinação" to "✅ Não Procrastinar",
        "Uso excessivo do celular" to "📵 Usar Menos o Celular"
    )

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // --- MÉTODOS DE AUTENTICAÇÃO E SINCRONIZAÇÃO ---

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
     * // MODIFICADO: A função de login agora é mais simples.
     * Apenas autentica o usuário. A lógica de sincronização e roteamento
     * será chamada separadamente pela Activity.
     */
    fun login(email: String, pass: String, onLoginSuccess: () -> Unit) {
        _uiState.value = AuthUiState(isLoading = true)
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        // Apenas sinaliza o sucesso. A Activity chamará a sincronização.
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

    /**
     * // NOVO: Coração da sincronização.
     * Busca o perfil completo do usuário no Firestore e o salva localmente no Room.
     */
    fun syncUserProfileOnLogin(onSyncComplete: () -> Unit) {
        val firebaseUser = auth.currentUser ?: return onSyncComplete()

        _uiState.value = AuthUiState(isLoading = true)
        firestore.collection("usuarios").document(firebaseUser.uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Converte o documento do Firestore para um objeto UserProfile
                    val firestoreProfile = document.toObject(UserProfile::class.java)
                    if (firestoreProfile != null) {
                        // Mapeia TODOS os campos do Firestore para o objeto User do Room
                        val roomUser = User(
                            userId = firestoreProfile.uid,
                            email = firebaseUser.email ?: "",
                            nome = firestoreProfile.nome,
                            idade = firestoreProfile.idade,
                            peso = firestoreProfile.peso.toInt(),
                            altura = firestoreProfile.altura.toFloat(),
                            genero = firestoreProfile.genero,
                            temHabitoLeitura = firestoreProfile.temHabitoLeitura,
                            segueDieta = firestoreProfile.segueDieta,
                            gostariaSeguirDieta = firestoreProfile.gostariaSeguirDieta,
                            habitosNegativos = firestoreProfile.habitosParaMudar, // Nome do campo no Firestore
                            problemasEmocionais = firestoreProfile.problemasEmocionais, // Nome do campo no Firestore
                            praticaAtividade = firestoreProfile.praticaAtividade,
                            tempoDisponivel = firestoreProfile.tempoDisponivel,
                            espacosDisponiveis = firestoreProfile.espacosDisponiveis,
                            sugestoesInteresse = firestoreProfile.sugestoesInteresse
                        )

                        // Salva o usuário completo no Room
                        viewModelScope.launch {
                            userDao.insertOrUpdateUser(roomUser) // Lembre-se de criar este método no DAO
                            Log.d("Sync", "Usuário sincronizado do Firestore para o Room com sucesso.")
                            _uiState.value = AuthUiState(isLoading = false)
                            onSyncComplete()
                        }
                    }
                } else {
                    // Usuário logado mas sem perfil no Firestore (primeiro acesso)
                    Log.d("Sync", "Nenhum perfil encontrado no Firestore para este usuário. Fluxo de primeiro acesso.")
                    _uiState.value = AuthUiState(isLoading = false)
                    onSyncComplete()
                }
            }
            .addOnFailureListener { e ->
                _uiState.value = AuthUiState(isLoading = false, error = "Falha ao buscar dados da nuvem: ${e.message}")
                onSyncComplete()
            }
    }


    // --- MÉTODOS DE SALVAMENTO DE DADOS DO QUESTIONÁRIO ---

    /**
     * Salva o perfil inicial (infousuario). Esta função já estava correta.
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
                _uiState.value = AuthUiState(isLoading = false, error = "Erro ao salvar perfil na nuvem: ${e.message}")
            }
    }

    /**
     * Salva os hábitos iniciais (livro).
     */
    fun saveUserInitialHabits(ler: Boolean, dieta: Boolean, seguirDieta: Boolean, onSuccess: () -> Unit) {
        val user = auth.currentUser ?: return
        val updates = mapOf(
            "temHabitoLeitura" to ler, "segueDieta" to dieta, "gostariaSeguirDieta" to seguirDieta
        )
        updateUserInRoomAndFirestore(user.uid, updates, onSuccess) { roomUser ->
            roomUser.temHabitoLeitura = ler
            roomUser.segueDieta = dieta
            roomUser.gostariaSeguirDieta = seguirDieta
        }
    }

    /**
     * // CORRIGIDO: Agora também atualiza o Room, além de criar os hábitos.
     */
    fun saveMentalHealthAndCreateLocalHabits(habitos: List<String>, emocionais: List<String>, onSuccess: () -> Unit) {
        val user = auth.currentUser ?: return
        val updates = mapOf(
            "habitosParaMudar" to habitos, "problemasEmocionais" to emocionais
        )

        // Primeiro, atualiza o perfil do usuário no Room e Firestore
        updateUserInRoomAndFirestore(user.uid, updates, {
            // Depois, cria os hábitos específicos (lógica que já existia)
            createHabitsFromQuestionnaire(habitos, onSuccess)
        }) { roomUser ->
            roomUser.habitosNegativos = habitos
            roomUser.problemasEmocionais = emocionais
        }
    }

    /**
     * Salva as preferências de treino (pergunta01).
     */
    fun saveWorkoutPreferences(pratica: String, tempo: String, espacos: List<String>, onSuccess: () -> Unit) {
        val user = auth.currentUser ?: return
        val updates = mapOf(
            "praticaAtividade" to pratica, "tempoDisponivel" to tempo, "espacosDisponiveis" to espacos
        )
        updateUserInRoomAndFirestore(user.uid, updates, onSuccess) { roomUser ->
            roomUser.praticaAtividade = pratica
            roomUser.tempoDisponivel = tempo
            roomUser.espacosDisponiveis = espacos
        }
    }
    // Cole esta função dentro da sua classe AuthViewModel

    fun sendPasswordResetEmail(onSuccess: () -> Unit) {
        val user = auth.currentUser
        if (user?.email == null) {
            _uiState.value = AuthUiState(error = "Nenhum usuário logado ou e-mail associado.")
            return
        }

        // Mostra o loading enquanto o e-mail está sendo enviado
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
    /**
     * Salva as preferências de sugestão (sujestao).
     */
    fun saveSuggestionPreferences(interesses: List<String>, onSuccess: () -> Unit) {
        val user = auth.currentUser ?: return
        val updates = mapOf("sugestoesInteresse" to interesses)
        updateUserInRoomAndFirestore(user.uid, updates, onSuccess) { roomUser ->
            roomUser.sugestoesInteresse = interesses
        }
    }

    // --- MÉTODOS AUXILIARES E OUTROS ---

    /**
     * // NOVO: Função auxiliar para evitar repetição de código.
     * Atualiza um usuário no Firestore e, em caso de sucesso, no Room.
     */
    private fun updateUserInRoomAndFirestore(
        uid: String,
        firestoreUpdates: Map<String, Any>,
        onSuccess: () -> Unit,
        updateRoomAction: (User) -> Unit
    ) {
        _uiState.value = AuthUiState(isLoading = true)
        firestore.collection("usuarios").document(uid)
            .update(firestoreUpdates)
            .addOnSuccessListener {
                viewModelScope.launch {
                    val roomUser = userDao.getUserById(uid)
                    if (roomUser != null) {
                        updateRoomAction(roomUser) // Aplica a atualização específica
                        userDao.updateUser(roomUser)
                        Log.d("Sync", "Room atualizado com sucesso para: ${firestoreUpdates.keys}")
                    }
                    _uiState.value = AuthUiState(isLoading = false)
                    onSuccess()
                }
            }
            .addOnFailureListener { e ->
                _uiState.value = AuthUiState(isLoading = false, error = "Erro ao salvar na nuvem: ${e.message}")
            }
    }

    /**
     * // NOVO: Lógica de criação de hábitos extraída para uma função separada.
     */
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

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    suspend fun getCurrentUserFromRoom(): User? {
        // Para maior segurança, o ideal é sempre buscar pelo ID,
        // mas para a RoteadorActivity, getCurrentUser() funciona.
        return userDao.getCurrentUser()
    }
}