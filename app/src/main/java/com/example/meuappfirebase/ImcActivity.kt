package com.example.meuappfirebase // Pacote corrigido

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.meuappfirebase.databinding.ActivityImcBinding

class ImcActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImcBinding
    private val viewModel: ImcViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImcBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observarViewModel()

        // Pede ao ViewModel para carregar os dados
        viewModel.loadUserData()
    }

    private fun observarViewModel() {
        // Observa os dados do perfil para mostrar na tela
        viewModel.userProfile.observe(this) { user ->
            user?.let {
                binding.textPesoAtual.text = "Seu peso atual: ${it.peso} kg"
                binding.textAlturaAtual.text = "Sua altura: ${it.altura} m"
            }
        }

        // Observa o resultado do IMC já calculado
        viewModel.imcResult.observe(this) { result ->
            if (result != null) {
                binding.textResultadoImcNumero.text = result.value
                binding.textResultadoImcClassificacao.text = result.classification
                binding.textResultadoImcClassificacao.setTextColor(result.color)
                binding.cardResultadoImc.visibility = View.VISIBLE
            } else {
                binding.cardResultadoImc.visibility = View.GONE
            }
        }
    }

    private fun setupListeners() {
        // O botão calcular agora apenas recarrega os dados,
        // pois o cálculo é feito automaticamente quando os dados são carregados.
        binding.btnCalcularImc.setOnClickListener {
            viewModel.loadUserData()
        }

        binding.btnVoltarImc.setOnClickListener {
            finish()
        }
    }
}