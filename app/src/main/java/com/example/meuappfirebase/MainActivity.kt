package com.example.meuappfirebase

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.meuappfirebase.databinding.ActivityMainBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: AuthViewModel by viewModels()
    private val firestore = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        verificarSessaoFirebase()
        configurarBotoes()
        observarEstado()
    }

    private fun verificarSessaoFirebase() {
        val currentUser = viewModel.getCurrentUser()
        if (currentUser != null && currentUser.isEmailVerified) {
            // Usuário já logado, escondemos a tela de login
            binding.root.visibility = View.INVISIBLE

            // Pegamos os dados do Firestore para decidir a tela
            firestore.collection("usuarios").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    val profileIsComplete = document != null &&
                            document.exists() &&
                            !document.getString("nome").isNullOrEmpty()
                    navegarAposLogin(profileIsComplete)
                }
                .addOnFailureListener {
                    // Em caso de falha, mostramos a tela de login
                    binding.root.visibility = View.VISIBLE
                }

        } else {
            // Nenhum usuário logado, mostramos a tela de login
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

            viewModel.login(email, senha) { profileIsComplete ->
                navegarAposLogin(profileIsComplete)
            }
        }

        binding.textView8.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }

        binding.textView5.setOnClickListener {
            Toast.makeText(this, "Tela de Recuperar Senha a ser implementada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observarEstado() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // Aqui você pode ligar/desligar um ProgressBar, se tiver
                // binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE

                state.error?.let {
                    Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun navegarAposLogin(profileIsComplete: Boolean) {
        val proximaTelaIntent = if (profileIsComplete) {
            Intent(this, livro::class.java)
        } else {
            Intent(this, infousuario::class.java)
        }
        startActivity(proximaTelaIntent)
        finish()
    }
}
