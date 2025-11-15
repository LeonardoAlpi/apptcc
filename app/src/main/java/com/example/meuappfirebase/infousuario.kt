package com.example.meuappfirebase

import android.app.ActivityOptions // << IMPORT ADICIONADO
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
            // 1. Limpa erros anteriores
            binding.layoutNome.error = null
            binding.layoutIdade.error = null
            binding.layoutPeso.error = null
            binding.layoutAltura.error = null

            // 2. Coleta os dados dos campos
            val nome = binding.editTextTextnome.text.toString().trim()
            val idadeStr = binding.editTextNumberidade.text.toString()
            val pesoStr = binding.editTextNumber2peso.text.toString()
            val alturaStr = binding.editTextNumberDecimalaltura.text.toString()
            val checkedGeneroId = binding.radioGroupGenero.checkedRadioButtonId
            val genero = if (checkedGeneroId != -1) findViewById<RadioButton>(checkedGeneroId).text.toString() else ""

            var hasError = false // Flag para controlar se encontramos algum erro

            // 3. Validação de campos vazios
            if (nome.isEmpty()) {
                binding.layoutNome.error = "Campo obrigatório"
                hasError = true
            }
            if (idadeStr.isEmpty()) {
                binding.layoutIdade.error = "Campo obrigatório"
                hasError = true
            }
            if (pesoStr.isEmpty()) {
                binding.layoutPeso.error = "Campo obrigatório"
                hasError = true
            }
            if (alturaStr.isEmpty()) {
                binding.layoutAltura.error = "Campo obrigatório"
                hasError = true
            }
            if (genero.isEmpty()) {
                // Para o RadioGroup, um Toast ainda é a melhor opção
                Toast.makeText(this, "Por favor, selecione o gênero.", Toast.LENGTH_SHORT).show()
                hasError = true
            }

            // 4. Se houver campos vazios, não continua para a validação de intervalo
            if (hasError) {
                return@setOnClickListener
            }

            // 5. Conversão e Validação de Intervalos (só acontece se não estiverem vazios)

            // IDADE (14-99)
            val idade = idadeStr.toIntOrNull()
            if (idade == null || idade !in 14..99) {
                binding.layoutIdade.error = "Idade deve ser entre 14 e 99"
                hasError = true
            }

            // PESO (30-300)
            val peso = pesoStr.toFloatOrNull()
            if (peso == null || peso !in 30f..300f) {
                binding.layoutPeso.error = "Peso deve ser entre 30 e 300 kg"
                hasError = true
            }

            // ALTURA (1.30-2.30)
            val altura = alturaStr.toFloatOrNull()
            if (altura == null || altura !in 1.30f..2.30f) {
                binding.layoutAltura.error = "Altura deve ser entre 1.30 e 2.30 m"
                hasError = true
            }

            // 6. Se houver algum erro de intervalo, para aqui
            if (hasError) {
                return@setOnClickListener
            }

            // 7. Se passou em todas as validações, salva os dados
            // Usamos !! (non-null assertion) porque já garantimos que não são nulos
            viewModel.salvarDadosEtapa1(nome, idade!!, peso!!, altura!!, genero)
        }
    }

    private fun observarEstado() {
        lifecycleScope.launch {
            viewModel.onboardingStepUpdated.collect { success ->
                if (success) {
                    viewModel.resetOnboardingStepUpdated()

                    Toast.makeText(this@infousuario, "Perfil salvo com sucesso!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@infousuario, RoteadorActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                    // --- INÍCIO DA MUDANÇA ---
                    // 1. Cria o pacote de opções com as animações de fade
                    val options = ActivityOptions.makeCustomAnimation(
                        this@infousuario,
                        R.anim.fade_in,
                        R.anim.fade_out
                    )

                    // 2. Inicia a nova activity passando as opções
                    startActivity(intent, options.toBundle())
                    // --- FIM DA MUDANÇA ---

                    finishAfterTransition()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                state.error?.let {
                    Toast.makeText(this@infousuario, it, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}