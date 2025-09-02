package com.example.meuappfirebase

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.apol.myapplication.AppDatabase
import com.apol.myapplication.data.model.HabitoAgendamento
import com.apol.myapplication.data.model.HabitoProgresso
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Garanta que esta data class está correta, usando List<Int>
data class HabitStats(
    val habitName: String,
    val currentStreak: Int,
    val weeklyChartData: Pair<List<Int>, List<String>>,
    val monthlyChartData: Pair<List<Int>, List<String>>,
    val yearlyChartData: Pair<List<Int>, List<String>>,
    val weeklyConsistency: Int,
    val monthlyConsistency: Int,
    val yearlyConsistency: Int
)

class HabitProgressViewModel(application: Application) : AndroidViewModel(application) {

    private val habitoDao = AppDatabase.getDatabase(application).habitoDao()

    private val _habitStats = MutableLiveData<HabitStats?>()
    val habitStats: LiveData<HabitStats?> = _habitStats

    fun loadHabitStats(habitId: Long) {
        viewModelScope.launch {
            val habito = habitoDao.getHabitoById(habitId)
            if (habito == null) {
                _habitStats.postValue(null)
                return@launch
            }

            val progresso = habitoDao.getProgressoForHabito(habitId)
            val agendamentos = habitoDao.getAgendamentosParaHabito(habitId)

            val stats = HabitStats(
                habitName = habito.nome,
                currentStreak = calcularSequenciaAtual(progresso),
                weeklyChartData = gerarDadosDoGrafico(progresso, agendamentos, 7),
                monthlyChartData = gerarDadosDoGrafico(progresso, agendamentos, 30),
                yearlyChartData = gerarDadosDoGrafico(progresso, agendamentos, 365),
                weeklyConsistency = calcularConstancia(progresso, agendamentos, 7),
                monthlyConsistency = calcularConstancia(progresso, agendamentos, 30),
                yearlyConsistency = calcularConstancia(progresso, agendamentos, 365)
            )
            _habitStats.postValue(stats)
        }
    }

    private fun calcularSequenciaAtual(progresso: List<HabitoProgresso>): Int {
        if (progresso.isEmpty()) return 0
        val datasConcluidas = progresso.map { it.data }.toSet()
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        var sequencia = 0
        val calendar = Calendar.getInstance()
        while (datasConcluidas.contains(sdf.format(calendar.time))) {
            sequencia++
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        return sequencia
    }

    // Garanta que esta função retorna Pair<List<Int>, List<String>>
    private fun gerarDadosDoGrafico(progresso: List<HabitoProgresso>, agendamentos: List<HabitoAgendamento>, diasParaTras: Int): Pair<List<Int>, List<String>> {
        val datasConcluidas = progresso.map { it.data }.toSet()
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val labelFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
        val hojeStr = sdf.format(Date())
        val pontos = mutableListOf<Int>() // Lista de Int
        val legendas = mutableListOf<String>()
        var saldo = 0

        for (i in (diasParaTras - 1) downTo 0) {
            val diaDoLoop = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val dataFormatada = sdf.format(diaDoLoop.time)
            if (getAgendamentoParaData(diaDoLoop, agendamentos).contains(getDayOfWeekString(diaDoLoop))) {
                if (datasConcluidas.contains(dataFormatada)) saldo++ else if (dataFormatada < hojeStr) saldo--
            }
            saldo = saldo.coerceAtLeast(0)
            pontos.add(saldo) // Adiciona o Int diretamente
            legendas.add(labelFormat.format(diaDoLoop.time))
        }
        return Pair(pontos, legendas)
    }

    private fun calcularConstancia(progresso: List<HabitoProgresso>, agendamentos: List<HabitoAgendamento>, diasParaTras: Int): Int {
        val datasConcluidas = progresso.map { it.data }.toSet()
        var diasFeitos = 0
        var diasProgramadosConsiderados = 0
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val hojeStr = sdf.format(Date())

        for (i in 0 until diasParaTras) {
            val dia = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            if (sdf.format(dia.time) <= hojeStr) {
                if (getAgendamentoParaData(dia, agendamentos).contains(getDayOfWeekString(dia))) {
                    diasProgramadosConsiderados++
                    if (datasConcluidas.contains(sdf.format(dia.time))) diasFeitos++
                }
            }
        }
        if (diasProgramadosConsiderados == 0) return 100
        return (diasFeitos * 100) / diasProgramadosConsiderados
    }

    private fun getAgendamentoParaData(data: Calendar, agendamentos: List<HabitoAgendamento>): Set<String> {
        val dataFormatada = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(data.time)
        val agendamentoCorreto = agendamentos.sortedByDescending { it.dataDeInicio }.find { it.dataDeInicio <= dataFormatada }
        return agendamentoCorreto?.diasProgramados?.split(',')?.toSet() ?: emptySet()
    }

    private fun getDayOfWeekString(calendar: Calendar): String {
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "SUN"; Calendar.MONDAY -> "MON"; Calendar.TUESDAY -> "TUE"
            Calendar.WEDNESDAY -> "WED"; Calendar.THURSDAY -> "THU"; Calendar.FRIDAY -> "FRI"
            Calendar.SATURDAY -> "SAT"; else -> ""
        }
    }
}