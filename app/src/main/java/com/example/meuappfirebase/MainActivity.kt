package com.example.meuappfirebase

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import com.example.meuappfirebase.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: AuthViewModel by viewModels()
    private val TAG = "MainActivityDebug"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        verificarSessaoAtiva()
        configurarBotoes()
        observarEstado()
    }

    private fun verificarSessaoAtiva() {
        if (viewModel.getCurrentUser() != null && viewModel.getCurrentUser()!!.isEmailVerified) {

            binding.root.visibility = View.INVISIBLE

            // MUDANÇA: Agendamos a tarefa diária aqui
            scheduleDailySuggestionUpdate()

            iniciarSincronizacaoENavegacao()
        } else {

            binding.root.visibility = View.VISIBLE
        }
    }

    private fun configurarBotoes() {
        binding.buttonavancarinfousuario.setOnClickListener {
            val email = binding.editTextusuario.text.toString().trim()
            val senha = binding.editTextsenha.text.toString().trim()

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE

            viewModel.login(email, senha) {

                // MUDANÇA: Agendamos a tarefa diária aqui também, após um novo login
                scheduleDailySuggestionUpdate()
                iniciarSincronizacaoENavegacao()
            }
        }

        binding.textView8.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }
        binding.textView5.setOnClickListener {
            val intent = Intent(this, RecuperarSenhaActivity::class.java)
            startActivity(intent)
        }
    }

    private fun iniciarSincronizacaoENavegacao() {
        viewModel.syncUserProfileOnLogin {

            val intent = Intent(this, RoteadorActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun observarEstado() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                state.error?.let {
                    Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // --- FUNÇÃO ADICIONADA PARA AGENDAR A TAREFA DIÁRIA ---
    private fun scheduleDailySuggestionUpdate() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Só roda com internet
            .build()

        // Configura a tarefa para repetir a cada 24 horas
        val repeatingRequest = PeriodicWorkRequestBuilder<UpdateSuggestionsWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()

        // Envia a tarefa para o WorkManager, garantindo que não haverá duplicatas
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "dailySuggestionUpdate", // Nome único da tarefa
            ExistingPeriodicWorkPolicy.KEEP, // Se já estiver agendada, mantém a antiga.
            repeatingRequest
        )


    }
}