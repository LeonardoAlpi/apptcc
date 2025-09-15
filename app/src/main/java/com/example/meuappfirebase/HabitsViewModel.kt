package com.example.meuappfirebase

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.apol.myapplication.data.model.Habito
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HabitosViewModel(application: Application) : AndroidViewModel(application) {

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth
    private val usuarioLogadoId = auth.currentUser?.uid

    private val _habitsList = MutableStateFlow<List<HabitUI>>(emptyList())
    val habitsList = _habitsList.asStateFlow()

    private val _mostrandoHabitosBons = MutableStateFlow(true)
    val mostrandoHabitosBons = _mostrandoHabitosBons.asStateFlow()

    private val _operationStatus = MutableStateFlow<String?>(null)
    val operationStatus = _operationStatus.asStateFlow()

    private var allHabitsCache = listOf<HabitUI>()

    private val reminderScheduler = HabitReminderScheduler(application)
    private val _permissionEvent = MutableSharedFlow<Unit>()
    val permissionEvent = _permissionEvent.asSharedFlow()

    init {
        carregarHabitosEmTempoReal()
    }

    private fun carregarHabitosEmTempoReal() {
        if (usuarioLogadoId == null) {
            Log.w("HabitosViewModel", "Usuário nulo, não é possível carregar hábitos.")
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

    fun toggleTipoHabito() {
        _mostrandoHabitosBons.value = !_mostrandoHabitosBons.value
        filtrarHabitos()
    }

    fun adicionarHabito(nome: String, diasProgramados: Set<String>, isGoodHabit: Boolean) {
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
            .addOnSuccessListener {
                _operationStatus.value = "Hábito '$nome' criado!"
                tryToScheduleHabitReminders()
            }
            .addOnFailureListener { e ->
                Log.e("HabitosViewModel", "Erro ao adicionar hábito", e)
                _operationStatus.value = "Erro ao criar hábito."
            }
    }

    fun marcarHabito(habitId: String, concluir: Boolean) {
        val docRef = firestore.collection("habitos").document(habitId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
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
        val batch = firestore.batch()
        habitosParaApagar.forEach { habit ->
            batch.delete(firestore.collection("habitos").document(habit.id))
        }
        batch.commit()
            .addOnSuccessListener {
                _operationStatus.value = "${habitosParaApagar.size} hábito(s) apagado(s)."
                tryToScheduleHabitReminders()
            }
            .addOnFailureListener { _operationStatus.value = "Erro ao apagar hábitos." }
    }

    fun updateHabit(habitId: String, newName: String, newDays: Set<String>) {
        val updateData = mapOf(
            "nome" to newName,
            "diasProgramados" to newDays.toList()
        )
        firestore.collection("habitos").document(habitId).update(updateData)
            .addOnSuccessListener {
                _operationStatus.value = "Hábito atualizado!"
                tryToScheduleHabitReminders()
            }
            .addOnFailureListener { _operationStatus.value = "Erro ao atualizar o hábito." }
    }

    fun clearOperationStatus() {
        _operationStatus.value = null
    }

    fun tryToScheduleHabitReminders() {
        if (usuarioLogadoId == null) return
        viewModelScope.launch {
            if (!reminderScheduler.canScheduleExactAlarms()) {
                _permissionEvent.emit(Unit)
                Log.d("HabitReminders", "Permissão de alarme exato não concedida. Solicitando...")
                return@launch
            }

            firestore.collection("habitos")
                .whereEqualTo("userOwnerId", usuarioLogadoId)
                .get()
                .addOnSuccessListener { snapshots ->
                    val habitsForScheduling = snapshots.toObjects(Habito::class.java)
                    reminderScheduler.cancelAllReminders(habitsForScheduling)
                    reminderScheduler.scheduleAllHabitReminders(habitsForScheduling)
                    Log.d("HabitReminders", "${habitsForScheduling.size} hábitos foram re-agendados.")
                }
                .addOnFailureListener { e ->
                    Log.e("HabitReminders", "Erro ao buscar hábitos para agendamento.", e)
                }
        }
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
            val userOwnerId = getString("userOwnerId") ?: "" // Pega o ID do dono
            val progresso = get("progresso") as? List<String> ?: emptyList()
            val concluidoHoje = progresso.contains(getHojeString())
            val streak = calcularSequencia(progresso)

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
                userOwnerId = userOwnerId // Passa o valor para o construtor
            )
        } catch (e: Exception) {
            Log.e("HabitosViewModel", "Erro ao converter documento ${this.id} para HabitUI", e)
            null
        }
    }
}