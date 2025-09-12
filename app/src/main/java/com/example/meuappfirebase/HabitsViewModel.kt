package com.example.meuappfirebase

import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*
import com.apol.myapplication.AppDatabase
import com.apol.myapplication.HabitAlarmScheduler
import com.apol.myapplication.data.model.Habito
import com.apol.myapplication.data.model.HabitoProgresso
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class HabitosViewModel(application: Application) : AndroidViewModel(application) {

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth
    private val usuarioLogadoId = auth.currentUser?.uid

    // Adicionamos a referência ao DAO para poder salvar no Room
    private val habitoDao = AppDatabase.getDatabase(application).habitoDao()

    private val _habitsList = MutableStateFlow<List<HabitUI>>(emptyList())
    val habitsList = _habitsList.asStateFlow()

    private val _mostrandoHabitosBons = MutableStateFlow(true)
    val mostrandoHabitosBons = _mostrandoHabitosBons.asStateFlow()

    private val _operationStatus = MutableStateFlow<String?>(null)
    val operationStatus = _operationStatus.asStateFlow()

    private var allHabitsCache = listOf<HabitUI>()
    private val _permissionEvent = MutableSharedFlow<Unit>()
    val permissionEvent = _permissionEvent.asSharedFlow()
    private val alarmManager = application.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    init {
        carregarHabitosEmTempoReal()
    }

    fun tryToScheduleHabitReminders() {
        viewModelScope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    // Permissão OK, pode agendar
                    HabitAlarmScheduler.scheduleDailyReminder(getApplication())
                } else {
                    // Permissão faltando, avisa a Activity
                    _permissionEvent.emit(Unit)
                }
            } else {
                // Versões antigas do Android não precisam dessa permissão
                HabitAlarmScheduler.scheduleDailyReminder(getApplication())
            }
        }
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

        val novoHabitoFirestore = hashMapOf(
            "userOwnerId" to usuarioLogadoId, "nome" to nome, "isFavorito" to false,
            "isGoodHabit" to isGoodHabit, "diasProgramados" to diasProgramados.toList(),
            "progresso" to emptyList<String>()
        )

        firestore.collection("habitos").add(novoHabitoFirestore)
            .addOnSuccessListener { documentReference ->
                val firestoreId = documentReference.id
                viewModelScope.launch {
                    val novoHabitoRoom = Habito(
                        firestoreId = firestoreId,
                        userOwnerId = usuarioLogadoId,
                        nome = nome,
                        isGoodHabit = isGoodHabit
                    )
                    habitoDao.insertHabito(novoHabitoRoom)
                    _operationStatus.value = "Hábito '$nome' criado!"
                }
            }
            .addOnFailureListener { e ->
                Log.e("HabitosViewModel", "Erro ao adicionar hábito", e)
                _operationStatus.value = "Erro ao criar hábito."
            }
    }

    // FUNÇÃO SIMPLIFICADA E CORRIGIDA
    fun marcarHabito(habitIdFirestore: String, concluir: Boolean) {
        val docRef = firestore.collection("habitos").document(habitIdFirestore)
        val hoje = getHojeString()

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val progresso = snapshot.get("progresso") as? MutableList<String> ?: mutableListOf()
            if (concluir) {
                if (!progresso.contains(hoje)) progresso.add(hoje)
            } else {
                progresso.remove(hoje)
            }
            transaction.update(docRef, "progresso", progresso)
        }.addOnFailureListener { e ->
            Log.e("HabitosViewModel", "Erro ao marcar hábito no Firestore", e)
        }

        viewModelScope.launch {
            val habitoRoom = habitoDao.getHabitoByFirestoreId(habitIdFirestore)
            if (habitoRoom != null) {
                if (concluir) {
                    habitoDao.insertProgresso(HabitoProgresso(habitoId = habitoRoom.id, data = hoje))
                } else {
                    habitoDao.deleteProgresso(habitoId = habitoRoom.id, data = hoje)
                }
            } else {
                Log.w("HabitosViewModel", "Não foi possível encontrar o hábito no Room para o firestoreId: $habitIdFirestore")
            }
        }
    }

    fun toggleFavorito(habit: HabitUI) {
        // Verifica se já atingiu o limite de 3 favoritos
        if (!habit.isFavorited) {
            val totalFavoritos = allHabitsCache.count { it.isFavorited }
            if (totalFavoritos >= 3) {
                _operationStatus.value = "Você pode favoritar no máximo 3 hábitos."
                return // Para a execução aqui
            }
        }

        // Se não atingiu o limite, ou se está desfavoritando,
        // ele manda o comando para o Firestore.
        firestore.collection("habitos").document(habit.id)
            .update("isFavorito", !habit.isFavorited) // A mágica está aqui!
            .addOnFailureListener { e ->
                // Se der erro, avisa no Logcat. É bom ficar de olho aqui!
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
            .addOnSuccessListener { _operationStatus.value = "${habitosParaApagar.size} hábito(s) apagado(s)." } // ALTERADO
            .addOnFailureListener { _operationStatus.value = "Erro ao apagar hábitos." } // ALTERADO
    }

    fun updateHabit(habitId: String, newName: String, newDays: Set<String>) {
        val updateData = mapOf(
            "nome" to newName,
            "diasProgramados" to newDays.toList()
        )
        firestore.collection("habitos").document(habitId).update(updateData)
            .addOnSuccessListener { _operationStatus.value = "Hábito atualizado!" } // ALTERADO
            .addOnFailureListener { _operationStatus.value = "Erro ao atualizar o hábito." } // ALTERADO
    }

    /**
     * NOVA FUNÇÃO: Limpa a mensagem de status para que o Toast não apareça novamente.
     */
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
            calendar.add(Calendar.DAY_OF_YEAR, -1)
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
                isGoodHabit = getBoolean("isGoodHabit") ?: true
            )
        } catch (e: Exception) {
            Log.e("HabitosViewModel", "Erro ao converter documento ${this.id} para HabitUI", e)
            null
        }
    }
}