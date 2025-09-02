package com.example.meuappfirebase

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

        // Verifica se o usuário já respondeu esta tela
        lifecycleScope.launch {
            val user = viewModel.getCurrentUserFromRoom()
            if (user != null &&
                !user.praticaAtividade.isNullOrEmpty() &&
                !user.tempoDisponivel.isNullOrEmpty() &&
                !user.espacosDisponiveis.isNullOrEmpty()
            ) {
                // Usuário já respondeu, vai direto para a próxima tela
                startActivity(Intent(this@pergunta01, sujestao::class.java))
                finish()
                return@launch
            }
        }

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

            // Pega os textos das respostas
            val praticaAtividade = findViewById<RadioButton>(idPratica).text.toString()
            val tempoDisponivel = findViewById<RadioButton>(idTempo).text.toString()

            // Salva os dados no ViewModel
            viewModel.saveWorkoutPreferences(praticaAtividade, tempoDisponivel, espacos) {
                // Navega para a próxima tela após salvar
                startActivity(Intent(this, sujestao::class.java))
                finish()
            }
        }
    }
}
