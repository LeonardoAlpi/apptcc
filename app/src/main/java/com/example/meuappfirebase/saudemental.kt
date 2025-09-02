package com.example.meuappfirebase

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.meuappfirebase.databinding.ActivitySaudementalBinding
import kotlinx.coroutines.launch

class saudemental : AppCompatActivity() {

    private lateinit var binding: ActivitySaudementalBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySaudementalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Verifica se o usuário já respondeu a tela de saúde mental
        lifecycleScope.launch {
            val user = viewModel.getCurrentUserFromRoom()
            if (user != null &&
                !user.espacosDisponiveis.isNullOrEmpty() && // Flag de perguntas iniciais respondidas
                !user.praticaAtividade.isNullOrEmpty()
            ) {
                // Usuário já respondeu, vai direto para a próxima tela
                startActivity(Intent(this@saudemental, pergunta01::class.java))
                finish()
                return@launch
            }
        }

        configurarCheckBoxes()
        configurarBotaoAvancar()
        observarEstado()
    }

    private fun configurarCheckBoxes() {
        val listaHabitosChecks = listOf(
            binding.checkBoxfumar,
            binding.checkBox2beber,
            binding.checkBox3sonoruim,
            binding.checkBox4procastinacao,
            binding.checkBox5usoexcessivodocelular
        )

        val listaEmocionalChecks = listOf(
            binding.checkBox6ansiedade,
            binding.checkBox7depressao,
            binding.checkBox8estresse,
            binding.checkBox9faltademotivacao
        )

        binding.checkBoxNenhumHabito.setOnCheckedChangeListener { _, isChecked ->
            listaHabitosChecks.forEach { checkBox ->
                checkBox.isEnabled = !isChecked
                if (isChecked) checkBox.isChecked = false
            }
        }

        binding.checkBoxSemProblema.setOnCheckedChangeListener { _, isChecked ->
            listaEmocionalChecks.forEach { checkBox ->
                checkBox.isEnabled = !isChecked
                if (isChecked) checkBox.isChecked = false
            }
        }
    }

    private fun configurarBotaoAvancar() {
        binding.buttonavancarsaudemental.setOnClickListener {
            val habitosSelecionados = mutableListOf<String>()
            binding.apply {
                if (checkBoxNenhumHabito.isChecked) {
                    habitosSelecionados.add(checkBoxNenhumHabito.text.toString())
                } else {
                    if (checkBoxfumar.isChecked) habitosSelecionados.add(checkBoxfumar.text.toString())
                    if (checkBox2beber.isChecked) habitosSelecionados.add(checkBox2beber.text.toString())
                    if (checkBox3sonoruim.isChecked) habitosSelecionados.add(checkBox3sonoruim.text.toString())
                    if (checkBox4procastinacao.isChecked) habitosSelecionados.add(checkBox4procastinacao.text.toString())
                    if (checkBox5usoexcessivodocelular.isChecked) habitosSelecionados.add(checkBox5usoexcessivodocelular.text.toString())
                }
            }

            val problemasEmocionaisSelecionados = mutableListOf<String>()
            binding.apply {
                if (checkBoxSemProblema.isChecked) {
                    problemasEmocionaisSelecionados.add(checkBoxSemProblema.text.toString())
                } else {
                    if (checkBox6ansiedade.isChecked) problemasEmocionaisSelecionados.add(checkBox6ansiedade.text.toString())
                    if (checkBox7depressao.isChecked) problemasEmocionaisSelecionados.add(checkBox7depressao.text.toString())
                    if (checkBox8estresse.isChecked) problemasEmocionaisSelecionados.add(checkBox8estresse.text.toString())
                    if (checkBox9faltademotivacao.isChecked) problemasEmocionaisSelecionados.add(checkBox9faltademotivacao.text.toString())
                }
            }

            if (habitosSelecionados.isEmpty() || problemasEmocionaisSelecionados.isEmpty()) {
                Toast.makeText(this, "Por favor, selecione ao menos uma opção em cada categoria.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Salva no Room e Firebase
            viewModel.saveMentalHealthAndCreateLocalHabits(habitosSelecionados, problemasEmocionaisSelecionados) {
                // Navega para a próxima tela após o sucesso
                startActivity(Intent(this, pergunta01::class.java))
                finish()
            }
        }
    }

    private fun observarEstado() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                state.error?.let { Toast.makeText(this@saudemental, it, Toast.LENGTH_LONG).show() }
            }
        }
    }
}
