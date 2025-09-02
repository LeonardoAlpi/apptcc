package com.example.meuappfirebase

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.apol.myapplication.AppDatabase
import com.apol.myapplication.data.model.TreinoNota
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class WorkoutSplitDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = Firebase.auth
    private val treinoDao = AppDatabase.getDatabase(application).treinoDao()

    // LiveData para expor a lista de notas para a Activity
    private val _notes = MutableLiveData<List<TreinoNota>>()
    val notes: LiveData<List<TreinoNota>> = _notes

    /**
     * Carrega as notas de uma divisão específica do banco de dados Room.
     */
    fun loadNotes(divisaoId: Long) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            val notesFromDb = treinoDao.getNotasByDivisaoId(divisaoId, user.uid)
            _notes.postValue(notesFromDb)
        }
    }

    /**
     * Adiciona uma nova nota de treino ao banco.
     */
    fun addNote(divisaoId: Long, title: String) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            val newNote = TreinoNota(userOwnerId = user.uid, divisaoId = divisaoId, titulo = title)
            treinoDao.insertTreinoNota(newNote)
            loadNotes(divisaoId) // Recarrega a lista
        }
    }

    /**
     * Atualiza uma nota de treino existente.
     */
    fun updateNote(note: TreinoNota) {
        viewModelScope.launch {
            treinoDao.updateTreinoNota(note)
            loadNotes(note.divisaoId) // Recarrega a lista
        }
    }

    /**
     * Deleta uma lista de notas de treino.
     */
    fun deleteNotes(notesToDelete: List<TreinoNota>) {
        if (notesToDelete.isEmpty()) return
        viewModelScope.launch {
            treinoDao.deleteTreinoNotas(notesToDelete)
            loadNotes(notesToDelete.first().divisaoId) // Recarrega a lista
        }
    }
}