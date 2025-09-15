package com.example.meuappfirebase

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.meuappfirebase.databinding.ActivityLivroBinding
import kotlinx.coroutines.launch

class livro : AppCompatActivity() {

    private lateinit var binding: ActivityLivroBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLivroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // A verificação que existia aqui foi removida. O RoteadorActivity
        // já é responsável por garantir que o usuário chegue na tela certa.
        // Isso simplifica o código e centraliza a lógica de navegação.

        configurarBotaoAvancar()
        observarEstado()
    }

    private fun configurarBotaoAvancar() {
        binding.buttonavancarlivro.setOnClickListener {
            val temHabitoLeitura = when (binding.radioGroup02.checkedRadioButtonId) {
                binding.radioButtonsimler.id -> true
                binding.radioButton2nOler.id -> false
                else -> null
            }
            val segueDieta = when (binding.RadioGroup1.checkedRadioButtonId) {
                binding.radioButton3simdieta.id -> true
                binding.radioButton4naodieta.id -> false
                else -> null
            }
            val gostariaSeguirDieta = when (binding.radioGroup3.checkedRadioButtonId) {
                binding.radioButton5simseguir.id -> true
                binding.radioButton6nOseguir.id -> false
                else -> null
            }

            if (temHabitoLeitura == null || segueDieta == null || gostariaSeguirDieta == null) {
                Toast.makeText(this, "Por favor, responda a todas as perguntas.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // <-- MUDANÇA PRINCIPAL
            // Chama a nova função no ViewModel para salvar os dados e ATUALIZAR O PASSO
            viewModel.salvarDadosEtapa2(temHabitoLeitura, segueDieta, gostariaSeguirDieta)
        }
    }

    private fun observarEstado() {
        lifecycleScope.launch {
            // Observa o estado para saber quando a atualização terminou
            viewModel.onboardingStepUpdated.collect { success ->
                if (success) {
                    // Dados salvos com sucesso, volta para o Roteador
                    val intent = Intent(this@livro, RoteadorActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                state.error?.let {
                    Toast.makeText(this@livro, it, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}