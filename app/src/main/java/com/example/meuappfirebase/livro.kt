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

        // Verifica se o usuário já respondeu as perguntas de hábitos iniciais
        lifecycleScope.launch {
            val user = viewModel.getCurrentUserFromRoom()
            if (user != null && user.temHabitoLeitura != null &&
                user.segueDieta != null && user.gostariaSeguirDieta != null) {
                // Usuário já respondeu, vai direto para a próxima tela
                startActivity(Intent(this@livro, saudemental::class.java))
                finish()
                return@launch
            }
        }

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

            // Salva no Room e Firebase
            viewModel.saveUserInitialHabits(temHabitoLeitura, segueDieta, gostariaSeguirDieta) {
                Toast.makeText(this, "Preferências salvas!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, saudemental::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun observarEstado() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                state.error?.let {
                    Toast.makeText(this@livro, it, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
