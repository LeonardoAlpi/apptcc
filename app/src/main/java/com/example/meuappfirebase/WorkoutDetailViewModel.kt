package com.example.meuappfirebase

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.apol.myapplication.AppDatabase
import com.apol.myapplication.data.model.DivisaoTreino
import com.apol.myapplication.data.model.TipoDivisao
import com.apol.myapplication.data.model.TreinoEntity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class WorkoutDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = Firebase.auth
    private val treinoDao = AppDatabase.getDatabase(application).treinoDao()

    // LiveData para expor o treino atual para a Activity
    private val _workout = MutableLiveData<TreinoEntity?>()
    val workout: LiveData<TreinoEntity?> = _workout

    // LiveData para expor a lista de divisões
    private val _divisions = MutableLiveData<List<DivisaoTreino>>()
    val divisions: LiveData<List<DivisaoTreino>> = _divisions

    /**
     * Carrega o treino e suas divisões do banco de dados Room.
     */
    fun loadInitialData(treinoId: Long) {
        viewModelScope.launch {
            val treino = treinoDao.getTreinoById(treinoId)
            _workout.postValue(treino)
            if (treino != null) {
                val divisoes = treinoDao.getDivisoesByTreinoId(treino.id)
                _divisions.postValue(divisoes)
            }
        }
    }

    /**
     * Configura o tipo de divisão do treino e cria as divisões iniciais.
     */
    fun setWorkoutType(treino: TreinoEntity, tipo: TipoDivisao) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            val treinoAtualizado = treino.copy(tipoDivisao = tipo)
            treinoDao.updateTreino(treinoAtualizado)

            if (tipo == TipoDivisao.DIAS_DA_SEMANA) {
                val dias = listOf("Domingo", "Segunda-feira", "Terça-feira", "Quarta-feira", "Quinta-feira", "Sexta-feira", "Sábado")
                dias.forEachIndexed { index, nome ->
                    treinoDao.insertDivisao(DivisaoTreino(userOwnerId = user.uid, treinoId = treino.id, nome = nome, ordem = index))
                }
            } else { // LETRAS
                val letras = listOf("Treino A", "Treino B", "Treino C")
                letras.forEachIndexed { index, nome ->
                    treinoDao.insertDivisao(DivisaoTreino(userOwnerId = user.uid, treinoId = treino.id, nome = nome, ordem = index))
                }
            }
            // Recarrega os dados para atualizar a UI
            loadInitialData(treino.id)
        }
    }

    /**
     * Adiciona uma nova divisão de treino baseada em letra.
     */
    fun addLetterDivision() {
        val user = auth.currentUser ?: return
        val treino = _workout.value ?: return
        val currentDivisions = _divisions.value ?: emptyList()

        val proximaLetraChar = ('A' + currentDivisions.size).toChar()
        val novaDivisao = DivisaoTreino(
            userOwnerId = user.uid,
            treinoId = treino.id,
            nome = "Treino $proximaLetraChar",
            ordem = currentDivisions.size
        )
        viewModelScope.launch {
            treinoDao.insertDivisao(novaDivisao)
            loadInitialData(treino.id) // Recarrega
        }
    }

    /**
     * Renomeia uma divisão de treino.
     */
    fun renameDivision(divisao: DivisaoTreino, newName: String) {
        viewModelScope.launch {
            val divisaoAtualizada = divisao.copy(nome = newName)
            treinoDao.updateDivisao(divisaoAtualizada)
            loadInitialData(divisao.treinoId) // Recarrega
        }
    }

    /**
     * Deleta uma lista de divisões de treino.
     */
    fun deleteDivisions(divisionsToDelete: List<DivisaoTreino>) {
        if (divisionsToDelete.isEmpty()) return
        viewModelScope.launch {
            treinoDao.deleteDivisoes(divisionsToDelete)
            loadInitialData(divisionsToDelete.first().treinoId) // Recarrega
        }
    }
}