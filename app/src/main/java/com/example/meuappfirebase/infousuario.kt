package com.example.meuappfirebase

import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.apol.myapplication.data.model.User // Importe sua data class 'User' do Room
import com.example.meuappfirebase.databinding.ActivityInfousuarioBinding
import kotlinx.coroutines.launch

/**
 * Esta Activity é responsável por coletar as informações de perfil do usuário
 * após o primeiro login bem-sucedido. Os dados são salvos de forma sincronizada
 * no Cloud Firestore (nuvem) e no Room (local).
 */
class infousuario : AppCompatActivity() {

    // Declara o ViewBinding para interagir com o layout XML.
    private lateinit var binding: ActivityInfousuarioBinding

    // Injeta o AuthViewModel para termos acesso à lógica de salvar.
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfousuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarBotaoAvancar()
        observarEstado()
    }

    /**
     * Configura o clique do botão para coletar, validar e salvar os dados do usuário.
     */
    private fun configurarBotaoAvancar() {
        binding.buttonavancarinfousuario.setOnClickListener {
            val user = viewModel.getCurrentUser()
            if (user == null) {
                Toast.makeText(this, "Sessão expirada. Por favor, faça login novamente.", Toast.LENGTH_LONG).show()
                finish()
                return@setOnClickListener
            }

            // Coleta os dados dos campos do XML.
            val nome = binding.editTextTextnome.text.toString().trim()
            val idade = binding.editTextNumberidade.text.toString()
            val peso = binding.editTextNumber2peso.text.toString()
            val altura = binding.editTextNumberDecimalaltura.text.toString()
            val genero = findViewById<RadioButton>(binding.radioGroupGenero.checkedRadioButtonId)?.text.toString() ?: ""

            if (nome.isEmpty() || idade.isEmpty() || peso.isEmpty() || altura.isEmpty() || genero.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // --- CRIAÇÃO DOS DOIS OBJETOS PARA SINCRONIZAÇÃO ---
            // 1. Objeto para o Firestore (nosso backup na nuvem).
            val firestoreProfile = UserProfile(
                uid = user.uid,
                nome = nome,
                idade = idade.toIntOrNull() ?: 0,
                peso = peso.toDoubleOrNull() ?: 0.0,
                altura = altura.toDoubleOrNull() ?: 0.0,
                genero = genero
            )

            // 2. Objeto para o Room (nosso cache offline).
            // Lembrete: remova o campo 'password' da sua data class User por segurança!
            val roomUser = User(
                userId = user.uid,
                email = user.email ?: "",
                nome = nome,
                idade = idade.toIntOrNull() ?: 0,
                peso = peso.toIntOrNull() ?: 0,
                altura = altura.toFloatOrNull() ?: 0.0f,
                genero = genero
            )

            // --- CHAMADA DA NOVA FUNÇÃO DE SINCRONIZAÇÃO ---
            viewModel.saveUserProfileToFirestoreAndRoom(firestoreProfile, roomUser) {
                Toast.makeText(this, "Perfil salvo com sucesso!", Toast.LENGTH_SHORT).show()
                navegarParaTelaPrincipal()
            }
        }
    }

    /**
     * Observa o estado do ViewModel para mostrar feedback ao usuário (erros, etc.).
     */
    private fun observarEstado() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // Aqui você pode controlar um ProgressBar se adicionar um ao XML.
                // binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                state.error?.let {
                    Toast.makeText(this@infousuario, it, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /**
     * Navega para a tela principal do app (`livro`) após o perfil ser salvo.
     */
    private fun navegarParaTelaPrincipal() {
        val intent = Intent(this, livro::class.java)
        // Limpa o histórico de navegação para que o usuário não volte para a tela de perfil.
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}