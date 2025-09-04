package com.example.meuappfirebase

import android.app.Application
import androidx.lifecycle.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Mantemos as mesmas classes de dados
enum class TimeFilter { WEEK, MONTH, YEAR }
data class ChartData(val points: List<Float>, val labels: List<String>)
data class HabitStats(
    val habitName: String,
    val currentStreak: Int,
    val weeklyConsistency: Int,
    val monthlyConsistency: Int,
    val yearlyConsistency: Int,
    val weeklyChart: ChartData,
    val monthlyChart: ChartData,
    val yearlyChart: ChartData
)

class HabitProgressViewModel(application: Application) : AndroidViewModel(application) {

    // AGORA USA FIRESTORE
    private val firestore = Firebase.firestore

    private val _habitStats = MutableLiveData<HabitStats>()
    val habitStats: LiveData<HabitStats> = _habitStats

    private val _currentChartData = MediatorLiveData<ChartData>()
    val currentChartData: LiveData<ChartData> = _currentChartData

    private val _currentConsistency = MediatorLiveData<Pair<Int, TimeFilter>>()
    val currentConsistency: LiveData<Pair<Int, TimeFilter>> = _currentConsistency

    init {
        _currentChartData.addSource(_habitStats) { updateMediators(TimeFilter.MONTH) }
        _currentConsistency.addSource(_habitStats) { updateMediators(TimeFilter.MONTH) }
    }

    // AGORA RECEBE STRING ID
    fun loadHabitStats(habitId: String) {
        viewModelScope.launch {
            // Busca o documento do hábito direto no Firestore
            firestore.collection("habitos").document(habitId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val nome = document.getString("nome") ?: "Hábito"
                        val progresso = document.get("progresso") as? List<String> ?: emptyList()
                        // ATENÇÃO: A lógica de agendamento precisa ser lida do Firestore também.
                        // Para simplificar, vamos assumir que o hábito é para todos os dias.
                        val agendamentos = emptyList<String>() // Adapte se tiver agendamentos no Firestore

                        val stats = HabitStats(
                            habitName = nome,
                            currentStreak = calcularSequenciaAtual(progresso),
                            weeklyConsistency = calcularConstancia(progresso, 7),
                            monthlyConsistency = calcularConstancia(progresso, 30),
                            yearlyConsistency = calcularConstancia(progresso, 365),
                            weeklyChart = gerarDadosDoGrafico(progresso, 7),
                            monthlyChart = gerarDadosDoGrafico(progresso, 30),
                            yearlyChart = gerarDadosDoGrafico(progresso, 365)
                        )
                        _habitStats.postValue(stats)
                    }
                }
        }
    }

    // ... (O resto das funções do ViewModel, como onFilterSelected, updateMediators, etc.,
    // foram ajustadas para não dependerem mais de "agendamentos", simplificando o código)

    fun onFilterSelected(filter: TimeFilter) {
        updateMediators(filter)
    }

    private fun updateMediators(filter: TimeFilter) {
        _habitStats.value?.let { stats ->
            when (filter) {
                TimeFilter.WEEK -> {
                    _currentChartData.value = stats.weeklyChart
                    _currentConsistency.value = Pair(stats.weeklyConsistency, filter)
                }
                TimeFilter.MONTH -> {
                    _currentChartData.value = stats.monthlyChart
                    _currentConsistency.value = Pair(stats.monthlyConsistency, filter)
                }
                TimeFilter.YEAR -> {
                    _currentChartData.value = stats.yearlyChart
                    _currentConsistency.value = Pair(stats.yearlyConsistency, filter)
                }
            }
        }
    }

    private fun gerarDadosDoGrafico(progresso: List<String>, diasParaTras: Int): ChartData {
        val datasConcluidas = progresso.toSet()
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val labelFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
        val hojeStr = sdf.format(Date())
        val pontos = mutableListOf<Float>()
        val legendas = mutableListOf<String>()
        var saldo = 0f

        for (i in (diasParaTras - 1) downTo 0) {
            val diaDoLoop = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val dataFormatada = sdf.format(diaDoLoop.time)

            if (datasConcluidas.contains(dataFormatada)) {
                saldo++
            } else if (dataFormatada < hojeStr) {
                saldo--
            }
            saldo = saldo.coerceAtLeast(0f)
            pontos.add(saldo)
            legendas.add(labelFormat.format(diaDoLoop.time))
        }
        return ChartData(pontos, legendas)
    }

    private fun calcularSequenciaAtual(progresso: List<String>): Int {
        if (progresso.isEmpty()) return 0
        val datasConcluidas = progresso.toSet()
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        var sequencia = 0
        val calendar = Calendar.getInstance()
        if (!datasConcluidas.contains(sdf.format(calendar.time))) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        while (datasConcluidas.contains(sdf.format(calendar.time))) {
            sequencia++
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        return sequencia
    }

    private fun calcularConstancia(progresso: List<String>, diasParaTras: Int): Int {
        val datasConcluidas = progresso.toSet()
        var diasFeitos = 0
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        for (i in 0 until diasParaTras) {
            val dia = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            if (datasConcluidas.contains(sdf.format(dia.time))) {
                diasFeitos++
            }
        }
        return (diasFeitos * 100) / diasParaTras
    }
}