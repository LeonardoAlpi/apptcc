package com.example.meuappfirebase

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.apol.myapplication.AppDatabase
import com.apol.myapplication.data.model.Bloco
import com.apol.myapplication.data.model.Habito
import com.apol.myapplication.data.model.HabitoProgresso
import com.apol.myapplication.data.model.Note
import com.apol.myapplication.data.model.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = Firebase.auth
    private val db = AppDatabase.getDatabase(application)
    private val userDao = db.userDao()
    private val habitoDao = db.habitoDao()
    private val notesDao = db.notesDao()



    // LiveData para expor os dados do usuário para a Activity
    private val _userProfile = MutableLiveData<User?>()
    val userProfile: LiveData<User?> = _userProfile

    // LiveData para a lista de hábitos favoritados
    private val _topHabits = MutableLiveData<List<Habito>>()
    val topHabits: LiveData<List<Habito>> = _topHabits

    // LiveData para a lista de blocos
    private val _topBlocos = MutableLiveData<List<Bloco>>()
    val topBlocos: LiveData<List<Bloco>> = _topBlocos

    private val _operationStatus = MutableLiveData<Event<String>>()
    val operationStatus: LiveData<Event<String>> = _operationStatus

    /**
     * Carrega todos os dados iniciais necessários para o dashboard a partir do Room.
     */
    // DENTRO DE DashboardViewModel.kt

    fun loadInitialData() {
        // 1. A chamada para carregar do Firestore continua aqui.
        carregarHabitosFavoritados()

        val user = auth.currentUser
        if (user == null) {
            _userProfile.postValue(null)
            return
        }

        viewModelScope.launch {
            // Carrega o perfil do usuário do Room
            val profile = userDao.getUserById(user.uid)
            _userProfile.postValue(profile)

            // 2. REMOVEMOS A BUSCA DE HÁBITOS DO ROOM DAQUI.
            // A função carregarHabitosFavoritados() já cuida disso.

            // Carrega os 3 primeiros blocos do Room
            val blocos = notesDao.getBlocosByUserNow(user.uid).take(3)
            _topBlocos.postValue(blocos)
        }
    }

    /**
     * Adiciona uma anotação rápida no banco de dados Room.
     */
    fun addQuickNote(text: String) {
        val user = auth.currentUser
        if (user == null) {
            _operationStatus.postValue(Event("Erro: Sessão inválida."))
            return
        }
        viewModelScope.launch {
            val quickNote = Note(userOwnerId = user.uid, text = text)
            notesDao.insertNote(quickNote)
            _operationStatus.postValue(Event("Anotação salva!"))
        }
    }

    /**
     * Marca um hábito como concluído para o dia de hoje.
     */
    fun markHabitAsDone(habito: Habito) {
        viewModelScope.launch {
            val hoje = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            habitoDao.insertProgresso(HabitoProgresso(habitoId = habito.id, data = hoje))
            _operationStatus.postValue(Event("Progresso adicionado para \"${removerEmoji(habito.nome)}\"!"))
        }
    }

    private fun removerEmoji(texto: String): String {
        return texto.replaceFirst(Regex("^\\p{So}\\s*"), "")
    }

    private fun carregarHabitosFavoritados() {
        val usuarioId = Firebase.auth.currentUser?.uid ?: return

        Firebase.firestore.collection("habitos")
            .whereEqualTo("userOwnerId", usuarioId)
            .whereEqualTo("isFavorito", true)
            .limit(3)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    // Lidar com o erro
                    return@addSnapshotListener
                }

                // --- MUDANÇA PRINCIPAL AQUI ---
                // Em vez de converter para a classe 'Habito' inteira, vamos pegar
                // apenas os dados que precisamos: o ID do documento e o nome.
                val habitosList = snapshots?.mapNotNull { doc ->
                    // Usamos o 'Habito' que já existe, mas preenchemos manualmente
                    // para garantir que temos o ID do Firestore (doc.id)
                    Habito(
                        id = 0, // ID do Room não é relevante aqui
                        nome = doc.getString("nome") ?: "",
                        userOwnerId = doc.getString("userOwnerId") ?: "",
                        isFavorito = doc.getBoolean("isFavorito") ?: false,
                        isGoodHabit = doc.getBoolean("isGoodHabit") ?: true
                    ).apply {
                        // Criamos uma forma de "anexar" o ID do Firestore ao objeto
                        // Isso é um truque, e o ideal seria criar uma classe de UI separada.
                        // Para simplificar, vamos passar o ID junto com o nome temporariamente.
                        // Formato: "ID_DO_FIRESTORE;;;NOME_DO_HABITO"
                        this.nome = "${doc.id};;;${this.nome}"
                    }
                } ?: emptyList()

                _topHabits.value = habitosList
            }
    }
}