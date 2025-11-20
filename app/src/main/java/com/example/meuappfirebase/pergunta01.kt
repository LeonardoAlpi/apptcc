package com.example.meuappfirebase

// import android.R // << IMPORT REMOVIDO: Evita conflitos com seus recursos
// import android.app.ActivityOptions // << IMPORT REMOVIDO: Não é mais necessário
import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.meuappfirebase.databinding.ActivityPergunta01Binding
import kotlinx.coroutines.launch

class pergunta01 : AppCompatActivity() {

    private lateinit var binding: ActivityPergunta01Binding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPergunta01Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- INÍCIO DA MUDANÇA (1) ---
        configurarBotaoVoltar() // Adiciona o listener para o botão "Voltar"
        // --- FIM DA MUDANÇA (1) ---

        configurarBotaoAvancar()
        observarEstado()
    }

    // --- INÍCIO DA MUDANÇA (2) ---
    // Função inteira adicionada para o novo botão "Voltar"
    private fun configurarBotaoVoltar() {
        binding.buttonVoltaratividades.setOnClickListener {
            // Apenas "fecha" esta Activity (pergunta01)
            // O Android automaticamente vai mostrar a que estava embaixo (saudemental)
            finish()
        }
    }
    // --- FIM DA MUDANÇA (2) ---

    private fun configurarBotaoAvancar() {
        binding.buttonavancaratividades.setOnClickListener {
            val idPratica = binding.radioGroupPraticaAtividade.checkedRadioButtonId
            val idTempo = binding.radioGroupTempo.checkedRadioButtonId

            if (idPratica == -1 || idTempo == -1) {
                Toast.makeText(this, "Por favor, responda todas as perguntas.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val espacos = mutableListOf<String>()
            if (binding.checkBoxcasa.isChecked) espacos.add("Casa")
            if (binding.checkBox2academia.isChecked) espacos.add("Academia")
            if (binding.checkBox3parque.isChecked) espacos.add("Parque")

            if (espacos.isEmpty()) {
                Toast.makeText(this, "Selecione pelo menos um espaço disponível.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val praticaAtividade = findViewById<RadioButton>(idPratica).text.toString()
            val tempoDisponivel = findViewById<RadioButton>(idTempo).text.toString()

            viewModel.salvarDadosEtapa4(praticaAtividade, tempoDisponivel, espacos)
        }
    }

    private fun observarEstado() {
        lifecycleScope.launch {
            viewModel.onboardingStepUpdated.collect { success ->
                if (success) {
                    // --- INÍCIO DA MUDANÇA (3) ---

                    // 1. Reseta o gatilho
                    viewModel.resetOnboardingStepUpdated()

                    // 2. Navega DIRETAMENTE para a próxima tela (Step 5),
                    //    conforme visto no RoteadorActivity.
                    val intent = Intent(this@pergunta01, sujestao::class.java)

                    // 3. Lógica de ActivityOptions REMOVIDA
                    // val options = ActivityOptions.makeCustomAnimation(...)

                    // 4. Chamada de 'startActivity' SIMPLIFICADA
                    // startActivity(intent, options.toBundle())
                    startActivity(intent)

                    // 5. REMOVEMOS o finish() para corrigir o "piscar"
                    // finishAfterTransition()

                    // --- FIM DA MUDANÇA (3) ---
                }
            }
        }

        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                state.error?.let { Toast.makeText(this@pergunta01, it, Toast.LENGTH_LONG).show() }
            }
        }
    }
}