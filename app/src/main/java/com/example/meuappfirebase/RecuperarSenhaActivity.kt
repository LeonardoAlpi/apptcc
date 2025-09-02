package com.example.meuappfirebase // Pacote corrigido para o nosso projeto

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.meuappfirebase.databinding.ActivityRecuperarSenhaBinding // Importe o ViewBinding do pacote correto
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * Esta Activity permite que o usuário recupere sua senha.
 * Ela pede o e-mail e usa o Firebase Auth para enviar um link de redefinição.
 */
class RecuperarSenhaActivity : AppCompatActivity() {

    // Declara o ViewBinding para interagir com o XML.
    private lateinit var binding: ActivityRecuperarSenhaBinding

    // Pega a instância do Firebase Auth para usar suas funcionalidades.
    private val auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Infla o layout XML e o define como o conteúdo da tela.
        binding = ActivityRecuperarSenhaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configura o clique do botão "Enviar Link".
        // A lógica de salvar nova senha e código foi removida, pois o Firebase cuida disso.
        binding.btnEnviarLink.setOnClickListener {
            val email = binding.editTextEmailRecuperar.text.toString().trim()
            if (email.isNotEmpty()) {
                enviarEmailDeRecuperacao(email)
            } else {
                Toast.makeText(this, "Por favor, insira seu e-mail.", Toast.LENGTH_SHORT).show()
            }
        }

        // Configura o botão para voltar à tela anterior (Login).
        binding.buttonVoltarLogin.setOnClickListener {
            finish()
        }
    }

    /**
     * Esta função chama o método do Firebase para enviar o e-mail de redefinição de senha.
     * O Firebase cuida de todo o resto (gerar o link, a página web para a nova senha, etc.).
     *
     * @param email O e-mail do usuário para onde o link será enviado.
     */
    private fun enviarEmailDeRecuperacao(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Informa ao usuário que o e-mail foi enviado e fecha a tela.
                    Toast.makeText(this, "Link de recuperação enviado para o seu e-mail.", Toast.LENGTH_LONG).show()
                    finish() // Fecha a tela e retorna para a tela de Login.
                } else {
                    // Informa ao usuário que houve uma falha.
                    Toast.makeText(this, "Falha ao enviar e-mail: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}