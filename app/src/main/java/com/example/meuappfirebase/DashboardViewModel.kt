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
    fun loadInitialData() {
        val user = auth.currentUser
        if (user == null) {
            _userProfile.postValue(null) // Sinaliza que não há usuário
            return
        }

        viewModelScope.launch {
            // Carrega o perfil do usuário do Room
            val profile = userDao.getUserById(user.uid)
            _userProfile.postValue(profile)

            // Carrega os 3 hábitos favoritados do Room
            val habits = habitoDao.getFavoritedHabitsByUser(user.uid)
            _topHabits.postValue(habits)

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
}