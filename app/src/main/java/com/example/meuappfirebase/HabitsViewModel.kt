package com.example.meuappfirebase

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

class HabitosViewModel : ViewModel() {

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    private val _habitsList = MutableStateFlow<List<HabitUI>>(emptyList())
    val habitsList = _habitsList.asStateFlow()

    private val _mostrandoHabitosBons = MutableStateFlow(true)
    val mostrandoHabitosBons = _mostrandoHabitosBons.asStateFlow()

    private val _operationStatus = MutableStateFlow<String?>(null)
    val operationStatus = _operationStatus.asStateFlow()

    private var allHabitsCache = listOf<HabitUI>()

    init {
        carregarHabitosEmTempoReal()
    }

    private fun carregarHabitosEmTempoReal() {
        val usuarioLogadoId = auth.currentUser?.uid
        if (usuarioLogadoId == null) {
            Log.w("HabitosViewModel", "Usuário nulo, não é possível carregar hábitos.")
            _habitsList.value = emptyList()
            return
        }

        firestore.collection("habitos")
            .whereEqualTo("userOwnerId", usuarioLogadoId)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.w("HabitosViewModel", "Erro ao ouvir as mudanças nos hábitos.", error)
                    _operationStatus.value = "Erro ao carregar hábitos."
                    return@addSnapshotListener
                }

                allHabitsCache = snapshots?.documents?.mapNotNull { doc ->
                    doc.toHabitUI()
                } ?: emptyList()

                filtrarHabitos()
            }
    }

    private fun filtrarHabitos() {
        _habitsList.value = allHabitsCache.filter { it.isGoodHabit == _mostrandoHabitosBons.value }
    }

    fun setTipoHabito(isBons: Boolean) {
        if (_mostrandoHabitosBons.value != isBons) {
            _mostrandoHabitosBons.value = isBons
            filtrarHabitos()
        }
    }

    fun toggleTipoHabito() {
        _mostrandoHabitosBons.value = !_mostrandoHabitosBons.value
        filtrarHabitos()
    }

    fun adicionarHabito(nome: String, diasProgramados: Set<String>, isGoodHabit: Boolean) {
        val usuarioLogadoId = auth.currentUser?.uid
        if (usuarioLogadoId == null) return

        val novoHabito = hashMapOf(
            "userOwnerId" to usuarioLogadoId,
            "nome" to nome,
            "isFavorito" to false,
            "isGoodHabit" to isGoodHabit,
            "diasProgramados" to diasProgramados.toList(),
            "progresso" to emptyList<String>()
        )
        firestore.collection("habitos").add(novoHabito)
            .addOnSuccessListener { _operationStatus.value = "Hábito '$nome' criado!" }
            .addOnFailureListener { e ->
                Log.e("HabitosViewModel", "Erro ao adicionar hábito", e)
                _operationStatus.value = "Erro ao criar hábito."
            }
    }

    fun marcarHabito(habitId: String, concluir: Boolean) {
        val usuarioLogadoId = auth.currentUser?.uid
        if (usuarioLogadoId == null) {
            _operationStatus.value = "Usuário não logado."
            return
        }

        val docRef = firestore.collection("habitos").document(habitId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            if (snapshot.getString("userOwnerId") != usuarioLogadoId) {
                throw Exception("Permissão negada: O hábito não pertence a este usuário.")
            }
            val progresso = snapshot.get("progresso") as? MutableList<String> ?: mutableListOf()
            val hoje = getHojeString()

            if (concluir) {
                if (!progresso.contains(hoje)) progresso.add(hoje)
            } else {
                progresso.remove(hoje)
            }
            transaction.update(docRef, "progresso", progresso)
        }.addOnFailureListener { e ->
            Log.e("HabitosViewModel", "Erro ao marcar hábito", e)
            _operationStatus.value = "Não foi possível atualizar o hábito."
        }
    }

    fun toggleFavorito(habit: HabitUI) {
        val usuarioLogadoId = auth.currentUser?.uid
        if (usuarioLogadoId == null) {
            _operationStatus.value = "Usuário não logado."
            return
        }

        if (habit.userOwnerId != usuarioLogadoId) {
            _operationStatus.value = "Permissão negada para favoritar este hábito."
            return
        }

        if (!habit.isFavorited) {
            val totalFavoritos = allHabitsCache.count { it.isFavorited }
            if (totalFavoritos >= 3) {
                _operationStatus.value = "Você pode favoritar no máximo 3 hábitos."
                return
            }
        }

        firestore.collection("habitos").document(habit.id)
            .update("isFavorito", !habit.isFavorited)
            .addOnFailureListener { e ->
                Log.e("HabitosViewModel", "Erro ao favoritar hábito", e)
                _operationStatus.value = "Erro ao favoritar o hábito."
            }
    }

    fun executarExclusao(habitosParaApagar: List<HabitUI>) {
        val usuarioLogadoId = auth.currentUser?.uid
        if (usuarioLogadoId == null) {
            _operationStatus.value = "Usuário não logado."
            return
        }

        val batch = firestore.batch()
        habitosParaApagar.forEach { habit ->
            if (habit.userOwnerId == usuarioLogadoId) {
                batch.delete(firestore.collection("habitos").document(habit.id))
            } else {
                Log.e("HabitosViewModel", "Tentativa de excluir hábito de outro usuário: ${habit.id}")
            }
        }
        batch.commit()
            .addOnSuccessListener { _operationStatus.value = "${habitosParaApagar.size} hábito(s) apagado(s)." }
            .addOnFailureListener { _operationStatus.value = "Erro ao apagar hábitos." }
    }

    fun updateHabit(habitId: String, newName: String, newDays: Set<String>) {
        val usuarioLogadoId = auth.currentUser?.uid
        if (usuarioLogadoId == null) {
            _operationStatus.value = "Usuário não logado."
            return
        }

        // Adiciona uma verificação extra para evitar que a UI envie uma solicitação para um hábito de outro usuário
        val habitToUpdate = allHabitsCache.firstOrNull { it.id == habitId }
        if (habitToUpdate?.userOwnerId != usuarioLogadoId) {
            _operationStatus.value = "Permissão negada para editar este hábito."
            return
        }

        val updateData = mapOf(
            "nome" to newName,
            "diasProgramados" to newDays.toList()
        )
        firestore.collection("habitos").document(habitId).update(updateData)
            .addOnSuccessListener { _operationStatus.value = "Hábito atualizado!" }
            .addOnFailureListener { _operationStatus.value = "Erro ao atualizar o hábito." }
    }

    fun clearOperationStatus() {
        _operationStatus.value = null
    }

    private fun getHojeString(): String = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

    private fun calcularSequencia(progressos: List<String>): Int {
        if (progressos.isEmpty()) return 0
        val datasConcluidas = progressos.toSet()
        var sequencia = 0
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        if (!datasConcluidas.contains(sdf.format(calendar.time))) {
            return 0
        }
        while (datasConcluidas.contains(sdf.format(calendar.time))) {
            sequencia++
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        return sequencia
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toHabitUI(): HabitUI? {
        return try {
            val nome = getString("nome") ?: return null
            val progresso = get("progresso") as? List<String> ?: emptyList()
            val concluidoHoje = progresso.contains(getHojeString())
            val streak = calcularSequencia(progresso)
            val userOwnerId = getString("userOwnerId") ?: ""

            HabitUI(
                id = this.id,
                name = nome,
                streakDays = streak,
                message = when {
                    streak == 0 && !concluidoHoje -> "Comece hoje! Você consegue!"
                    streak == 0 && concluidoHoje -> "Isso! Primeiro dia concluído!"
                    streak == 1 -> "1 dia! Continue assim."
                    streak in 2..6 -> "$streak dias! Mantenha o ritmo."
                    streak >= 7 -> "Uau! ${streak} dias! Incrível!"
                    else -> "Continue firme!"
                },
                count = if (concluidoHoje) 1 else 0,
                isFavorited = getBoolean("isFavorito") ?: false,
                isGoodHabit = getBoolean("isGoodHabit") ?: true,
                userOwnerId = userOwnerId
            )
        } catch (e: Exception) {
            Log.e("HabitosViewModel", "Erro ao converter documento ${this.id} para HabitUI", e)
            null
        }
    }
}