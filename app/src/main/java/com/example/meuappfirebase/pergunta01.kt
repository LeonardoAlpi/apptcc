package com.example.meuappfirebase

// import android.R // << IMPORT REMOVIDO: Evita conflitos com seus recursos
import android.app.ActivityOptions // << IMPORT ADICIONADO: Necessário para a nova animação
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

        configurarBotaoAvancar()
        observarEstado()
    }

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
                    val intent = Intent(this@pergunta01, RoteadorActivity::class.java)

                    // --- INÍCIO DA MUDANÇA ---
                    // 1. Cria o pacote de opções com as animações de fade
                    val options = ActivityOptions.makeCustomAnimation(
                        this@pergunta01,
                        R.anim.fade_in, // Animação para a nova tela que entra
                        R.anim.fade_out  // Animação para a tela antiga que sai
                    )

                    // 2. Inicia a nova activity passando o pacote de opções
                    startActivity(intent, options.toBundle())
                    // --- FIM DA MUDANÇA ---

                    // O overridePendingTransition foi removido daqui
                    finishAfterTransition()
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