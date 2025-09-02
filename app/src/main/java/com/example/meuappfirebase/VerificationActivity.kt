package com.example.meuappfirebase

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.meuappfirebase.databinding.ActivityVerificationBinding

/**
 * Esta Activity é exibida após o usuário se cadastrar com sucesso.
 * Sua única função é informar que um e-mail de verificação foi enviado
 * e fornecer um botão para retornar à tela de login.
 */
class VerificationActivity : AppCompatActivity() { // NOME CORRIGIDO para a convenção PascalCase

    // Declara o ViewBinding para interagir com o layout XML de forma segura e eficiente.
    private lateinit var binding: ActivityVerificationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Infla o layout XML (activity_verification.xml) usando o ViewBinding.
        binding = ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configura a ação de clique do botão "OK, IR PARA LOGIN".
        binding.buttonIrParaLogin.setOnClickListener {
            // Cria uma intenção (Intent) para abrir a MainActivity (nossa tela de login).
            val intent = Intent(this, MainActivity::class.java)

            // IMPORTANTE: Estas "flags" limpam o histórico de navegação.
            // Isso impede que o usuário aperte o botão "voltar" do celular e retorne para
            // as telas de cadastro ou verificação após ter completado o processo.
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            // Inicia a MainActivity, levando o usuário para a tela de login.
            startActivity(intent)
        }
    }
}