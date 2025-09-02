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

        // Verifica se o usuário já respondeu as sugestões
        lifecycleScope.launch {
            val user = viewModel.getCurrentUserFromRoom()
            if (user != null && !user.sugestoesInteresse.isNullOrEmpty()) {
                // Usuário já respondeu, vai direto para a tela principal
                val intent = Intent(this@sujestao, Bemvindouser::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                return@launch
            }
        }

        configurarCheckBoxes()
        configurarBotaoAvancar()
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

            // Salva no Room e Firebase
            viewModel.saveSuggestionPreferences(interessesSelecionados) {
                Toast.makeText(this, "Cadastro finalizado!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, Bemvindouser::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
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
