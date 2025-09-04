package com.example.meuappfirebase

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.meuappfirebase.databinding.ActivityProgressoHabitoBinding

class ActivityProgressoHabito : AppCompatActivity() {

    private lateinit var binding: ActivityProgressoHabitoBinding
    private val viewModel: HabitProgressViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProgressoHabitoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- CÓDIGO CORRIGIDO AQUI ---
        // Agora ele espera uma String com a chave "habit_id_string"
        val habitId = intent.getStringExtra("habit_id_string")

        // Se o ID for nulo ou vazio, o hábito é inválido, então fechamos a tela
        if (habitId.isNullOrEmpty()) {
            Toast.makeText(this, "Erro: Hábito não encontrado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        setupListeners()
        observeViewModel()

        viewModel.loadHabitStats(habitId)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.chipGroupFilters.setOnCheckedChangeListener { group, checkedId ->
            val filter = when (checkedId) {
                R.id.chip_semana -> TimeFilter.WEEK
                R.id.chip_ano -> TimeFilter.YEAR
                else -> TimeFilter.MONTH
            }
            viewModel.onFilterSelected(filter)
        }
    }

    private fun observeViewModel() {
        viewModel.habitStats.observe(this) { stats ->
            binding.titleProgresso.text = stats.habitName
            binding.tvDiasSeguidos.text = "${stats.currentStreak} dias"
        }

        viewModel.currentChartData.observe(this) { chartData ->
            // Passa os dados para a sua View de gráfico customizada
            binding.simpleLineChart.setData(chartData.points, chartData.labels)
        }

        viewModel.currentConsistency.observe(this) { (consistency, filter) ->
            val label = when(filter) {
                TimeFilter.WEEK -> "🎯 Constância (Semana)"
                TimeFilter.MONTH -> "🎯 Constância (Mês)"
                TimeFilter.YEAR -> "🎯 Constância (Ano)"
            }
            binding.tvConstanciaLabel.text = label
            binding.tvConstanciaGeral.text = "$consistency%"
        }
    }
}