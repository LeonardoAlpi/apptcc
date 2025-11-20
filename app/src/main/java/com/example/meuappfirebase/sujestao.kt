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
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySujestaoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarBotaoVoltar()
        configurarCheckBoxes()
        configurarBotaoAvancar()
        observarEstado()
    }

    private fun configurarBotaoVoltar() {
        binding.buttonVoltarsujestao.setOnClickListener {
            finish()
        }
    }

    private fun configurarBotaoAvancar() {
        binding.buttonavancarsujestao.setOnClickListener {
            val interessesSelecionados = mutableListOf<String>()
            binding.apply {
                if (checkBox5nenhumaatividade.isChecked) {
                    interessesSelecionados.add("Nenhuma das opções")
                } else {
                    if (checkBoxLivros.isChecked) interessesSelecionados.add("Sugestões de Livros")
                    if (checkBoxDietas.isChecked) interessesSelecionados.add("Dicas de Dieta")
                    if (checkBoxrespiracao.isChecked) interessesSelecionados.add("Respiração Guiada")
                    if (checkBox2meditacao.isChecked) interessesSelecionados.add("Meditação")
                    if (checkBox3podcasts.isChecked) interessesSelecionados.add("Podcasts Relaxantes")
                    if (checkBox4exerciciomentais.isChecked) interessesSelecionados.add("Exercícios Mentais")
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

                    Toast.makeText(this@sujestao, "Cadastro finalizado!", Toast.LENGTH_LONG).show()

                    // --- INÍCIO DA MUDANÇA (TENTATIVA 3 - A MAIS FORTE) ---

                    // 1. Navega DIRETAMENTE para 'Bemvindouser' (pulando o Roteador)
                    val intent = Intent(this@sujestao, Bemvindouser::class.java)

                    // 2. REMOVEMOS AS FLAGS. Elas são a causa do "piscar".
                    // intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)

                    // 3. Forçamos a transição a ser INSTANTÂNEA (sem animação).
                    overridePendingTransition(0, 0)

                    // 4. USAMOS finishAffinity(). Isso "limpa a pilha" do quiz
                    //    DEPOIS que a nova tela já foi iniciada.
                    finishAffinity()

                    // --- FIM DA MUDANÇA ---
                }
            }
        }
    }

    private fun configurarCheckBoxes() {
        val checkBoxesAtividades = listOf(
            binding.checkBoxrespiracao,
            binding.checkBox2meditacao,
            binding.checkBox3podcasts,
            binding.checkBox4exerciciomentais,
            binding.checkBoxLivros,
            binding.checkBoxDietas
        )

        binding.apply {
            cardRespiracao.setOnClickListener { checkBoxrespiracao.isChecked = !checkBoxrespiracao.isChecked }
            cardMeditacao.setOnClickListener { checkBox2meditacao.isChecked = !checkBox2meditacao.isChecked }
            cardPodcasts.setOnClickListener { checkBox3podcasts.isChecked = !checkBox3podcasts.isChecked }
            cardExerciciosMentais.setOnClickListener { checkBox4exerciciomentais.isChecked = !checkBox4exerciciomentais.isChecked }
            cardLivros.setOnClickListener { checkBoxLivros.isChecked = !checkBoxLivros.isChecked }
            cardDietas.setOnClickListener { checkBoxDietas.isChecked = !checkBoxDietas.isChecked }
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