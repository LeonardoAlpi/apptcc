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

    private val _userProfile = MutableLiveData<User?>()
    val userProfile: LiveData<User?> = _userProfile

    private val _topHabits = MutableLiveData<List<Habito>>()
    val topHabits: LiveData<List<Habito>> = _topHabits

    private val _topBlocos = MutableLiveData<List<Bloco>>()
    val topBlocos: LiveData<List<Bloco>> = _topBlocos

    private val _operationStatus = MutableLiveData<Event<String>>()
    val operationStatus: LiveData<Event<String>> = _operationStatus

    fun loadInitialData() {
        val user = auth.currentUser
        if (user == null) {
            _userProfile.postValue(null)
            return
        }

        carregarHabitosFavoritados()
        carregarBlocosFavoritados()

        viewModelScope.launch {
            val profile = userDao.getUserById(user.uid)
            _userProfile.postValue(profile)
        }
    }

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
                    // Criamos um objeto Habito simples apenas com os dados necessários para a UI
                    Habito(
                        firestoreId = doc.id, // Usamos o campo firestoreId que já existe
                        nome = doc.getString("nome") ?: "",
                        userOwnerId = doc.getString("userOwnerId") ?: ""
                        // Outros campos não são necessários aqui
                    )
                } ?: emptyList()

                _topHabits.postValue(habitosList)
            }
    }

    private fun carregarBlocosFavoritados() {
        val usuarioId = auth.currentUser?.uid ?: return

        firestore.collection("usuarios").document(usuarioId).collection("blocos")
            // REMOVEMOS O FILTRO DAQUI, VAMOS FAZER MANUALMENTE
            .limit(10) // Pega os 10 mais recentes, por exemplo
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    _operationStatus.postValue(Event("Erro ao carregar blocos."))
                    return@addSnapshotListener
                }

                val blocosList = snapshots?.documents?.mapNotNull { doc ->
                    // Tenta converter para o objeto Bloco
                    val bloco = doc.toObject(Bloco::class.java)?.copy(id = doc.id)
                    bloco?.apply {
                        // Lógica de migração: se o campo 'favorito' não existir,
                        // tenta ler o campo antigo 'isFavorito'.
                        if (!doc.contains("favorito")) {
                            this.favorito = doc.getBoolean("isFavorito") ?: false
                        }
                    }
                } ?: emptyList()

                // Agora, filtramos a lista em memória
                val blocosFavoritados = blocosList.filter { it.favorito }.take(3)

                _topBlocos.postValue(blocosFavoritados)
            }
    }

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

    fun markHabitAsDone(habito: Habito) {
        viewModelScope.launch {
            val hoje = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            // Esta função precisa ser implementada com mais detalhes se for usada
            // habitoDao.insertProgresso(HabitoProgresso(habitoId = habito.id, data = hoje))
            _operationStatus.postValue(Event("Progresso adicionado para \"${removerEmoji(habito.nome)}\"!"))
        }
    }

    private fun removerEmoji(texto: String): String {
        return texto.replaceFirst(Regex("^\\p{So}\\s*"), "")
    }
}