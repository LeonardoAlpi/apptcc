package com.example.meuappfirebase

import android.app.ActivityOptions // << IMPORT ADICIONADO
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
    private val authViewModel: AuthViewModel by viewModels()
    private val suggestionsViewModel: SuggestionsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySujestaoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarCheckBoxes()
        configurarBotaoAvancar()
        observarEstado()
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
            authViewModel.salvarDadosEtapa5(interessesSelecionados)
        }
    }

    private fun observarEstado() {
        lifecycleScope.launch {
            authViewModel.onboardingStepUpdated.collect { success ->
                if (success) {
                    authViewModel.resetOnboardingStepUpdated()

                    Toast.makeText(this@sujestao, "Preparando suas primeiras sugestões...", Toast.LENGTH_LONG).show()
                    suggestionsViewModel.gerarEcarregarSugestoes()

                    val intent = Intent(this@sujestao, RoteadorActivity::class.java)

                    // --- INÍCIO DA MUDANÇA ---
                    // 1. Cria o pacote de opções com as animações
                    val options = ActivityOptions.makeCustomAnimation(
                        this@sujestao,
                        R.anim.fade_in,
                        R.anim.fade_out
                    )

                    // 2. Inicia a activity passando as opções de animação
                    startActivity(intent, options.toBundle())
                    // --- FIM DA MUDANÇA ---

                    finishAfterTransition()
                }
            }
        }

        lifecycleScope.launch {
            suggestionsViewModel.statusMessage.observe(this@sujestao) { event ->
                event.getContentIfNotHandled()?.let { message ->
                    Toast.makeText(this@sujestao, message, Toast.LENGTH_SHORT).show()
                }
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