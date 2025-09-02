package com.example.meuappfirebase

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.apol.myapplication.AppDatabase
import com.apol.myapplication.PredefinedWorkout
import com.example.meuappfirebase.R
import com.apol.myapplication.WorkoutTemplateRepository
import com.apol.myapplication.data.model.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class WorkoutsViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = Firebase.auth
    private val appDb = AppDatabase.getDatabase(application)
    private val treinoDao = appDb.treinoDao()
    private val userDao = appDb.userDao()

    // LiveData para expor a lista de treinos para a Activity
    private val _workouts = MutableLiveData<List<TreinoEntity>>()
    val workouts: LiveData<List<TreinoEntity>> = _workouts

    // LiveData para enviar mensagens de status (sucesso, erro) para a Activity
    private val _operationStatus = MutableLiveData<Event<String>>()
    val operationStatus: LiveData<Event<String>> = _operationStatus

    /**
     * Carrega todos os treinos do usuário logado a partir do banco de dados Room.
     */
    fun loadWorkouts() {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            _workouts.postValue(treinoDao.getAllTreinos(user.uid))
        }
    }

    /**
     * Deleta os treinos selecionados do banco de dados Room.
     */
    fun deleteWorkouts(workoutsToDelete: List<TreinoEntity>) {
        viewModelScope.launch {
            val ids = workoutsToDelete.map { it.id }
            if (ids.isNotEmpty()) {
                treinoDao.deleteTreinosByIds(ids)
                _operationStatus.postValue(Event("${ids.size} treino(s) apagado(s)."))
                loadWorkouts() // Recarrega a lista
            }
        }
    }

    /**
     * Adiciona um novo treino genérico criado pelo usuário no banco de dados Room.
     */
    fun addWorkout(nome: String, iconeResId: Int, tipo: TipoTreino) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            val novoTreino = TreinoEntity(
                userOwnerId = user.uid,
                nome = nome,
                iconeResId = iconeResId,
                tipoDeTreino = tipo
            )
            treinoDao.insertTreino(novoTreino)
            loadWorkouts()
        }
    }

    /**
     * Verifica as respostas do onboarding e cria um treino sugerido se necessário.
     * Esta é a sua lógica do "motor de sugestões", agora dentro do ViewModel.
     */
    fun checkAndCreateSuggestedWorkout() {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            val treinosExistentes = treinoDao.getAllTreinos(user.uid)
            if (treinosExistentes.any { it.nome.contains("Iniciante") || it.nome.contains("HIIT") || it.nome.contains("Híbrido") }) {
                return@launch // Já existe um treino sugerido, não faz nada.
            }

            val userProfile = userDao.getUserById(user.uid) ?: return@launch
            val praticaAtividade = userProfile.praticaAtividade
            val tempoDisponivel = userProfile.tempoDisponivel
            val espacos = userProfile.espacosDisponiveis ?: emptyList()

            val workoutSugerido = when {
                espacos.contains("Academia") -> null
                praticaAtividade == "Sim" && tempoDisponivel == "Menos de 30 minutos" && espacos.contains("Casa") -> WorkoutTemplateRepository.hiitCasa
                praticaAtividade == "Sim" && tempoDisponivel == "Mais de 1 hora" && espacos.contains("Parque") -> WorkoutTemplateRepository.hibridoParque
                praticaAtividade == "Sim" && espacos.contains("Casa") -> WorkoutTemplateRepository.calisteniaCasaIntermediario
                praticaAtividade == "Não" && tempoDisponivel == "Menos de 30 minutos" && espacos.contains("Parque") -> WorkoutTemplateRepository.pularCordaIniciante
                praticaAtividade == "Não" && espacos.contains("Casa") -> WorkoutTemplateRepository.corpoInteiroCasaIniciante
                else -> null
            }

            workoutSugerido?.let {
                createPredefinedWorkout(it)
            }
        }
    }

    /**
     * Cria um treino completo a partir de um template, com suas divisões e notas.
     */
    private suspend fun createPredefinedWorkout(workout: PredefinedWorkout) {
        val user = auth.currentUser ?: return
        val novoTreino = TreinoEntity(
            userOwnerId = user.uid, nome = workout.nome,
            iconeResId = workout.iconeResId, tipoDeTreino = workout.tipoTreino,
            tipoDivisao = workout.tipoDivisao
        )
        val treinoId = treinoDao.insertTreino(novoTreino)

        for (divisaoPredefinida in workout.divisions) {
            val novaDivisao = DivisaoTreino(
                userOwnerId = user.uid, treinoId = treinoId,
                nome = divisaoPredefinida.nome, ordem = workout.divisions.indexOf(divisaoPredefinida)
            )
            val divisaoId = treinoDao.insertDivisao(novaDivisao)

            for (notaPredefinida in divisaoPredefinida.notas) {
                val novaNota = TreinoNota(
                    userOwnerId = user.uid, divisaoId = divisaoId,
                    titulo = notaPredefinida.titulo, conteudo = notaPredefinida.conteudo
                )
                treinoDao.insertTreinoNota(novaNota)
            }
        }
        loadWorkouts() // Recarrega a lista para mostrar o novo treino
    }
}

/**
 * Classe wrapper para eventos de LiveData. Garante que uma mensagem (como um Toast)
 * seja exibida apenas uma vez, mesmo após a rotação da tela.
 */
