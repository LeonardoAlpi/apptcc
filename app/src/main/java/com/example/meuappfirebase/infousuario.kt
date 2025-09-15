package com.example.meuappfirebase

import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.meuappfirebase.databinding.ActivityInfousuarioBinding
import kotlinx.coroutines.launch

class infousuario : AppCompatActivity() {

    private lateinit var binding: ActivityInfousuarioBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfousuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarBotaoAvancar()
        observarEstado()
    }

    private fun configurarBotaoAvancar() {
        binding.buttonavancarinfousuario.setOnClickListener {
            // Coleta os dados dos campos do XML.
            val nome = binding.editTextTextnome.text.toString().trim()
            val idadeStr = binding.editTextNumberidade.text.toString()
            val pesoStr = binding.editTextNumber2peso.text.toString()
            val alturaStr = binding.editTextNumberDecimalaltura.text.toString()

            val checkedGeneroId = binding.radioGroupGenero.checkedRadioButtonId
            val genero = if (checkedGeneroId != -1) findViewById<RadioButton>(checkedGeneroId).text.toString() else ""

            if (nome.isEmpty() || idadeStr.isEmpty() || pesoStr.isEmpty() || alturaStr.isEmpty() || genero.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Converte os valores com segurança
            val idade = idadeStr.toIntOrNull() ?: 0
            val peso = pesoStr.toFloatOrNull() ?: 0f
            val altura = alturaStr.toFloatOrNull() ?: 0f

            // --- MUDANÇA PRINCIPAL ---
            // A criação de objetos foi removida daqui.
            // Chamamos diretamente a nova função da etapa 1.
            viewModel.salvarDadosEtapa1(nome, idade, peso, altura, genero)
        }
    }

    private fun observarEstado() {
        // Observa o sucesso da atualização para navegar de volta ao Roteador
        lifecycleScope.launch {
            viewModel.onboardingStepUpdated.collect { success ->
                if (success) {
                    // Reseta o estado para evitar navegação duplicada se a tela for recriada
                    viewModel.resetOnboardingStepUpdated()

                    Toast.makeText(this@infousuario, "Perfil salvo com sucesso!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@infousuario, RoteadorActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
        }

        // Observa possíveis erros
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                state.error?.let {
                    Toast.makeText(this@infousuario, it, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // A função navegarParaTelaPrincipal() foi removida pois a lógica agora está em observarEstado().
}