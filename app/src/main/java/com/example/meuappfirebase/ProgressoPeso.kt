package com.example.meuappfirebase

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.apol.myapplication.data.model.WeightEntry
import com.example.meuappfirebase.databinding.ActivityProgressoPesoBinding
import java.text.SimpleDateFormat
import java.util.*

class ProgressoPeso : AppCompatActivity() {

    private lateinit var binding: ActivityProgressoPesoBinding
    private val viewModel: WeightProgressViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProgressoPesoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observarViewModel()
    }

    override fun onResume(){
        super.onResume()
        // Pede ao ViewModel para carregar os dados sempre que a tela é exibida
        viewModel.loadData()
    }

    private fun observarViewModel() {
        // Observa o perfil do usuário para mostrar na tela
        viewModel.userProfile.observe(this) { user ->
            user?.let {
                binding.textPesoAtual.text = "Seu peso atual: ${it.peso} kg"
                binding.textAlturaAtual.text = "Sua altura: ${"%.2f".format(it.altura)} m"
            }
        }

        // Observa o resultado do IMC já calculado
        viewModel.imcResult.observe(this) { result ->
            if (result != null) {
                binding.textResultadoImcNumero.text = result.value
                binding.textResultadoImcClassificacao.text = result.classification
                binding.textResultadoImcClassificacao.setTextColor(result.color)
                binding.cardResultadoImc.visibility = View.VISIBLE
            } else {
                binding.cardResultadoImc.visibility = View.GONE
            }
        }

        // Observa o histórico de peso para atualizar o gráfico
        viewModel.weightHistory.observe(this) { history ->
            updateChartData(history)
        }

        // Observa mensagens de status (ex: "Peso salvo!")
        viewModel.operationStatus.observe(this) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        binding.btnCalcularImc.setOnClickListener {
            // Apenas recarrega os dados, o cálculo é automático
            viewModel.loadData()
        }
        binding.btnAddWeight.setOnClickListener {
            showAddWeightDialog()
        }
        binding.btnVoltarImc.setOnClickListener {
            finish()
        }
    }

    private fun updateChartData(history: List<WeightEntry>) {
        if (history.isEmpty()) {
            binding.weightChart.setData(emptyList(), emptyList())
            return
        }
        val points = history.map { it.weight.toInt() }
        val labels = history.map {
            SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(it.date))
        }
        binding.weightChart.setData(points, labels)
    }

    private fun showAddWeightDialog() {
        val editText = EditText(this).apply {
            hint = "Digite seu peso atual (ex: 75.5)"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        AlertDialog.Builder(this)
            .setTitle("Registrar Novo Peso")
            .setView(editText)
            .setPositiveButton("Salvar") { _, _ ->
                val weightStr = editText.text.toString().replace(',', '.')
                val weight = weightStr.toFloatOrNull()

                if (weight != null) {
                    // Chama o ViewModel para salvar o novo peso
                    viewModel.addWeightEntry(weight)
                } else {
                    Toast.makeText(this, "Valor inválido.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}