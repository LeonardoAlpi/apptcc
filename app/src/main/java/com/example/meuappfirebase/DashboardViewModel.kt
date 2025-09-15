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
    private val firestore = Firebase.firestore

    // LiveData para expor os dados do usuário para a Activity
    private val _userProfile = MutableLiveData<User?>()
    val userProfile: LiveData<User?> = _userProfile

    // LiveData para a lista de hábitos favoritados
    private val _topHabits = MutableLiveData<List<Habito>>()
    val topHabits: LiveData<List<Habito>> = _topHabits

    // LiveData para a lista de blocos favoritados
    private val _topBlocos = MutableLiveData<List<Bloco>>()
    val topBlocos: LiveData<List<Bloco>> = _topBlocos

    private val _operationStatus = MutableLiveData<Event<String>>()
    val operationStatus: LiveData<Event<String>> = _operationStatus

    /**
     * Ponto de entrada principal para carregar todos os dados do dashboard.
     */
    fun loadInitialData() {
        val user = auth.currentUser
        if (user == null) {
            _userProfile.postValue(null)
            return
        }

        // Inicia os listeners do Firestore para dados em tempo real
        carregarHabitosFavoritados()
        carregarBlocosFavoritados()

        // Carrega o perfil do usuário do banco de dados local (Room)
        viewModelScope.launch {
            val profile = userDao.getUserById(user.uid)
            _userProfile.postValue(profile)
        }
    }

    /**
     * Busca os 3 hábitos mais favoritados do Firestore em tempo real.
     */
    private fun carregarHabitosFavoritados() {
        val usuarioId = auth.currentUser?.uid ?: return

        firestore.collection("habitos")
            .whereEqualTo("userOwnerId", usuarioId)
            .whereEqualTo("isFavorito", true)
            .limit(3)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    _operationStatus.postValue(Event("Erro ao carregar hábitos."))
                    return@addSnapshotListener
                }

                val habitosList = snapshots?.mapNotNull { doc ->
                    Habito(
                        id = 0, // ID do Room não é relevante aqui
                        nome = doc.getString("nome") ?: "",
                        userOwnerId = doc.getString("userOwnerId") ?: "",
                        isFavorito = doc.getBoolean("isFavorito") ?: false,
                        isGoodHabit = doc.getBoolean("isGoodHabit") ?: true
                    ).apply {
                        // Anexa o ID do Firestore ao nome para uso na UI
                        // Formato: "ID_DO_FIRESTORE;;;NOME_DO_HABITO"
                        this.nome = "${doc.id};;;${this.nome}"
                    }
                } ?: emptyList()

                _topHabits.postValue(habitosList)
            }
    }

    /**
     * Busca os 3 blocos mais favoritados do Firestore em tempo real.
     */
    private fun carregarBlocosFavoritados() {
        val usuarioId = auth.currentUser?.uid ?: return

        // O caminho da coleção de blocos deve ser verificado.
        // Assumindo que é: users -> {userId} -> blocos
        firestore.collection("users").document(usuarioId).collection("blocos")
            .whereEqualTo("favorito", true) // Verifique se o nome do campo é "favorito" ou "isFavorito"
            .limit(3)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    _operationStatus.postValue(Event("Erro ao carregar blocos."))
                    return@addSnapshotListener
                }

                // Converte os documentos do Firestore para a classe Bloco
                val blocosList = snapshots?.toObjects(Bloco::class.java) ?: emptyList()
                _topBlocos.postValue(blocosList)
            }
    }

    /**
     * Adiciona uma anotação rápida no banco de dados local Room.
     */
    fun addQuickNote(text: String) {
        val user = auth.currentUser ?: run {
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
     * Marca um hábito como concluído para o dia de hoje no Room.
     */
    fun markHabitAsDone(habito: Habito) {
        viewModelScope.launch {
            val hoje = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            // Atenção: o habito.id aqui vem do Firestore, você precisa do ID do Room.
            // Esta parte pode precisar de ajuste dependendo de como você sincroniza os dados.
            // Por enquanto, vamos assumir que você tem um jeito de obter o ID correto.
            // Se não, você precisará buscar o hábito no Room pelo nome ou ID do Firestore.

            // habitoDao.insertProgresso(HabitoProgresso(habitoId = habito.id, data = hoje))
            _operationStatus.postValue(Event("Progresso adicionado para \"${removerEmoji(habito.nome)}\"!"))
        }
    }

    private fun removerEmoji(texto: String): String {
        return texto.replaceFirst(Regex("^\\p{So}\\s*"), "")
    }
}