package com.example.meuappfirebase

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.meuappfirebase.databinding.ActivityRoteadorBinding
import kotlinx.coroutines.launch

class RoteadorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoteadorBinding
    private val viewModel: AuthViewModel by viewModels()

    private val TAG = "RoteadorDebug"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoteadorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        iniciarSincronizacaoENavegacao()
    }

    private fun iniciarSincronizacaoENavegacao() {
        Log.d(TAG, "Sincronização iniciada...")
        viewModel.syncUserProfileOnLogin {
            Log.d(TAG, "Sincronização completa. Roteando...")
            rotearUsuario()
        }
    }

    // A LÓGICA DE ROTEAMENTO FOI ATUALIZADA AQUI
    private fun rotearUsuario() {
        lifecycleScope.launch {
            Log.d(TAG, "Iniciando roteamento...")

            val user = viewModel.getCurrentUserFromRoom()
            Log.d(TAG, "Dados do usuário no Room: $user")

            if (user == null) {
                Log.w(TAG, "Usuário NULO no Room! Navegando para a MainActivity.")
                navegarPara(MainActivity::class.java)
                return@launch
            }

            // A nova lógica usa o 'onboardingStep' para decidir a próxima tela
            val proximaTela = when (user.onboardingStep) {
                1 -> {
                    Log.d(TAG, "Decisão: onboardingStep = 1. Indo para infousuario.")
                    infousuario::class.java
                }
                2 -> {
                    Log.d(TAG, "Decisão: onboardingStep = 2. Indo para livro.")
                    livro::class.java
                }
                3 -> {
                    Log.d(TAG, "Decisão: onboardingStep = 3. Indo para saudemental.")
                    saudemental::class.java
                }
                4 -> {
                    Log.d(TAG, "Decisão: onboardingStep = 4. Indo para pergunta01.")
                    pergunta01::class.java
                }
                5 -> {
                    Log.d(TAG, "Decisão: onboardingStep = 5. Indo para sujestao.")
                    sujestao::class.java
                }
                else -> { // Se for 6 ou qualquer outro número, o onboarding terminou
                    Log.d(TAG, "Decisão: Onboarding concluído! Indo para a tela principal Bemvindouser.")
                    Bemvindouser::class.java
                }
            }

            navegarPara(proximaTela)
        }
    }

    private fun <T> navegarPara(proximaTela: Class<T>) {
        Log.i(TAG, "Navegando para: ${proximaTela.simpleName}")

        val intent = Intent(this, proximaTela)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}