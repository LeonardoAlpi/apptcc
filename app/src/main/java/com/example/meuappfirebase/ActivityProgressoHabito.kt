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
    private var habitId: Long = -1L

    // Armazena os dados já calculados pelo ViewModel
    private var currentStats: HabitStats? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProgressoHabitoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        habitId = intent.getLongExtra("habit_id", -1L)
        if (habitId == -1L) {
            Toast.makeText(this, "Não foi possível carregar o hábito.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupListeners()
        observarViewModel()
    }

    override fun onResume() {
        super.onResume()
        // Pede ao ViewModel para carregar e calcular tudo
        viewModel.loadHabitStats(habitId)
    }

    private fun observarViewModel() {
        viewModel.habitStats.observe(this) { stats ->
            if (stats == null) {
                Toast.makeText(this, "Hábito não encontrado.", Toast.LENGTH_SHORT).show()
                finish()
                return@observe
            }

            currentStats = stats // Armazena os dados calculados

            binding.titleProgresso.text = stats.habitName
            binding.tvDiasSeguidos.text = "${stats.currentStreak} dias"

            // Define um filtro padrão se nenhum estiver selecionado
            if (binding.chipGroupFilters.checkedChipId == View.NO_ID) {
                binding.chipGroupFilters.check(R.id.chip_mes)
            } else {
                // Atualiza a UI com base no chip já selecionado
                atualizarGraficoEConstanciaPeloId(binding.chipGroupFilters.checkedChipId)
            }
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.chipGroupFilters.setOnCheckedChangeListener { _, checkedId ->
            atualizarGraficoEConstanciaPeloId(checkedId)
        }
    }

    private fun atualizarGraficoEConstanciaPeloId(checkedId: Int) {
        val stats = currentStats ?: return // Usa os dados já calculados do ViewModel

        when (checkedId) {
            R.id.chip_semana -> {
                  binding.simpleLineChart.setData(stats.weeklyChartData.first, stats.weeklyChartData.second)
                  binding.tvConstanciaGeral.text = "${stats.weeklyConsistency}%"
                  binding.tvConstanciaLabel.text = "Constância (Semana)"
            }
             R.id.chip_mes -> {
                   binding.simpleLineChart.setData(stats.monthlyChartData.first, stats.monthlyChartData.second)
                   binding.tvConstanciaGeral.text = "${stats.monthlyConsistency}%"
                    binding.tvConstanciaLabel.text = "Constância (Mês)"
            }
              R.id.chip_ano -> {
                   binding.simpleLineChart.setData(stats.yearlyChartData.first, stats.yearlyChartData.second)
                  binding.tvConstanciaGeral.text = "${stats.yearlyConsistency}%"
                 binding.tvConstanciaLabel.text = "Constância (Ano)"
            }
        }
    }
}