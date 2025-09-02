package com.example.meuappfirebase // Pacote do nosso projeto

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.meuappfirebase.databinding.ActivityRegistroBinding // Importa o ViewBinding gerado a partir do seu XML
import kotlinx.coroutines.launch

class RegistroActivity : AppCompatActivity() {

    // Declara o ViewBinding. Ele vai nos dar acesso a todos os componentes do seu XML.
    private lateinit var binding: ActivityRegistroBinding

    // Injeta o ViewModel que tem a lógica de cadastro do Firebase.
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Esta linha infla (cria) a sua tela a partir do seu activity_registro.xml
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        // E esta linha a exibe.
        setContentView(binding.root)

        configurarBotoes()
        observarEstado()
    }

    /**
     * Configura os cliques dos botões do seu layout.
     */
    private fun configurarBotoes() {
        // Conecta ao botão com id "buttonRegistrar" do seu XML.
        binding.buttonRegistrar.setOnClickListener {
            // Pega os textos dos campos de e-mail e senha do seu XML.
            val email = binding.editTextEmail.text.toString().trim()
            val senha = binding.editTextPassword.text.toString().trim()

            // Validações
            if (!isEmailValid(email)) {
                Toast.makeText(this, "Por favor, insira um e-mail válido.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (senha.length < 6) {
                Toast.makeText(this, "A senha deve ter no mínimo 6 caracteres.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Chama a função de cadastro do Firebase no ViewModel.
            viewModel.signUp(email, senha) {
                // Quando o cadastro for bem-sucedido, navega para a tela de verificação.
                navegarParaVerificacao()
            }
        }

        // Conecta ao botão com id "buttonVoltar" do seu XML.
        binding.buttonVoltar.setOnClickListener {
            finish() // Fecha a tela de registro e volta para a de login.
        }
    }

    /**
     * Observa o ViewModel para mostrar mensagens de erro vindas do Firebase.
     */
    private fun observarEstado() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // Se você adicionar um ProgressBar ao seu XML, pode controlá-lo aqui.
                // binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE

                state.error?.let {
                    Toast.makeText(this@RegistroActivity, it, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /**
     * Navega para a tela de verificação de e-mail.
     */
    private fun navegarParaVerificacao() {
        val intent = Intent(this, VerificationActivity::class.java)
        startActivity(intent)
    }

    /**
     * Função para validar o formato do e-mail.
     */
    private fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}