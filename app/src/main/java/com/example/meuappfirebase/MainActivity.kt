package com.example.meuappfirebase

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.meuappfirebase.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

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

    /**
     * Verifica se já existe um usuário logado ao abrir o app.
     */
    private fun verificarSessaoAtiva() {
        if (viewModel.getCurrentUser() != null && viewModel.getCurrentUser()!!.isEmailVerified) {
            Log.d(TAG, "Sessão ativa encontrada. Iniciando sincronização...")
            // Esconde a UI de login e mostra um carregamento
            binding.root.visibility = View.INVISIBLE
            // Se já há uma sessão, sincronizamos e então navegamos.
            iniciarSincronizacaoENavegacao()
        } else {
            Log.d(TAG, "Nenhuma sessão ativa. Exibindo tela de login.")
            binding.root.visibility = View.VISIBLE
        }
    }

    /**
     * Configura os cliques dos botões de login e registro.
     */
    private fun configurarBotoes() {
        binding.buttonavancarinfousuario.setOnClickListener {
            val email = binding.editTextusuario.text.toString().trim()
            val senha = binding.editTextsenha.text.toString().trim()

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 1. Tenta fazer o login
            viewModel.login(email, senha) {
                // 2. Se o login for bem-sucedido, inicia a sincronização
                Log.d(TAG, "Login bem-sucedido. Iniciando sincronização...")
                iniciarSincronizacaoENavegacao()
            }
        }

        binding.textView8.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }
        binding.textView5.setOnClickListener {
            Toast.makeText(this, "Tela de Recuperar Senha a ser implementada", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * // NOVO E ESSENCIAL
     * Esta função centraliza a chamada de sincronização e garante que a navegação
     * para o Roteador SÓ aconteça depois que a sincronização terminar.
     */
    private fun iniciarSincronizacaoENavegacao() {
        viewModel.syncUserProfileOnLogin {
            // Este código dentro do lambda só será executado QUANDO a sincronização terminar.
            Log.d(TAG, "Sincronização completa. Navegando para o Roteador.")
            val intent = Intent(this, RoteadorActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    /**
     * Observa o estado da UI para mostrar erros ou loading.
     */
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
}