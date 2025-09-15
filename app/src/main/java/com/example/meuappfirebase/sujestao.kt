package com.example.meuappfirebase

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.meuappfirebase.databinding.ActivitySujestaoBinding
import kotlinx.coroutines.launch

class sujestao : AppCompatActivity() {

    private lateinit var binding: ActivitySujestaoBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySujestaoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Verificação redundante em onCreate foi REMOVIDA.

        configurarCheckBoxes()
        configurarBotaoAvancar()
        observarEstado() // <-- Adicionado para o fluxo correto
    }

    private fun configurarBotaoAvancar() {
        binding.buttonavancarsujestao.setOnClickListener {
            val interessesSelecionados = mutableListOf<String>()
            binding.apply {
                if (checkBox5nenhumaatividade.isChecked) {
                    interessesSelecionados.add(checkBox5nenhumaatividade.text.toString())
                } else {
                    if (checkBoxrespiracao.isChecked) interessesSelecionados.add(checkBoxrespiracao.text.toString())
                    if (checkBox2meditacao.isChecked) interessesSelecionados.add(checkBox2meditacao.text.toString())
                    if (checkBox3podcasts.isChecked) interessesSelecionados.add(checkBox3podcasts.text.toString())
                    if (checkBox4exerciciomentais.isChecked) interessesSelecionados.add(checkBox4exerciciomentais.text.toString())
                }
            }

            if (interessesSelecionados.isEmpty()) {
                Toast.makeText(this, "Por favor, selecione ao menos uma opção!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // <-- MUDANÇA PRINCIPAL
            // Chama a nova função no ViewModel para salvar os dados e FINALIZAR o onboarding
            viewModel.salvarDadosEtapa5(interessesSelecionados)
        }
    }

    private fun observarEstado() {
        // Observa o sucesso da atualização para navegar de volta ao Roteador
        lifecycleScope.launch {
            viewModel.onboardingStepUpdated.collect { success ->
                if (success) {
                    Toast.makeText(this@sujestao, "Cadastro finalizado!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@sujestao, RoteadorActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }

        // Observa possíveis erros
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                state.error?.let { Toast.makeText(this@sujestao, it, Toast.LENGTH_LONG).show() }
            }
        }
    }

    private fun configurarCheckBoxes() {
        val checkBoxesAtividades = listOf(
            binding.checkBoxrespiracao,
            binding.checkBox2meditacao,
            binding.checkBox3podcasts,
            binding.checkBox4exerciciomentais
        )

        binding.apply {
            cardRespiracao.setOnClickListener { checkBoxrespiracao.isChecked = !checkBoxrespiracao.isChecked }
            cardMeditacao.setOnClickListener { checkBox2meditacao.isChecked = !checkBox2meditacao.isChecked }
            cardPodcasts.setOnClickListener { checkBox3podcasts.isChecked = !checkBox3podcasts.isChecked }
            cardExerciciosMentais.setOnClickListener { checkBox4exerciciomentais.isChecked = !checkBox4exerciciomentais.isChecked }
            cardNenhuma.setOnClickListener { checkBox5nenhumaatividade.isChecked = !checkBox5nenhumaatividade.isChecked }

            checkBox5nenhumaatividade.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    checkBoxesAtividades.forEach { checkBox ->
                        checkBox.isChecked = false
                        checkBox.isEnabled = false
                    }
                } else {
                    checkBoxesAtividades.forEach { checkBox ->
                        checkBox.isEnabled = true
                    }
                }
            }

            checkBoxesAtividades.forEach { checkBox ->
                checkBox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) checkBox5nenhumaatividade.isChecked = false
                }
            }
        }
    }
}