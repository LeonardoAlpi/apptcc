package com.example.meuappfirebase

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.meuappfirebase.databinding.ActivityConfiguracoesBinding
import com.apol.myapplication.data.model.User
import kotlinx.coroutines.launch

class configuracoes : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1
    private lateinit var binding: ActivityConfiguracoesBinding
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfiguracoesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarBotoes()
        observarViewModels()
        configurarNavBar()
    }

    override fun onResume() {
        super.onResume()
        settingsViewModel.loadUserProfile()
    }

    private fun observarViewModels() {
        // Observa o perfil do usuário
        settingsViewModel.userProfile.observe(this) { user ->
            if (user != null) {
                binding.nomeUsuarioText.text = user.nome
                if (!user.profilePicUri.isNullOrEmpty()) {
                    Glide.with(this).load(user.profilePicUri).circleCrop()
                        .into(binding.imagemPerfil)
                } else {
                    binding.imagemPerfil.setImageResource(R.drawable.ic_person_placeholder)
                }
            } else {
                Toast.makeText(this, "Sessão inválida, por favor, faça login.", Toast.LENGTH_SHORT)
                    .show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        // Observa mensagens de atualização
        settingsViewModel.updateStatus.observe(this) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }

        // Observa erros vindos do AuthViewModel (StateFlow)
        lifecycleScope.launch {
            authViewModel.uiState.collect { state ->
                state.error?.let {
                    Toast.makeText(this@configuracoes, it, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun configurarBotoes() {
        binding.imagemPerfil.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        binding.blocoTituloPerfil.setOnClickListener {
            settingsViewModel.userProfile.value?.let { user ->
                exibirDialogoEditarPerfil(user)
            } ?: Toast.makeText(this, "Aguarde os dados do perfil carregarem.", Toast.LENGTH_SHORT)
                .show()
        }

        binding.opcaoCalcularImc.setOnClickListener {
            startActivity(Intent(this, ProgressoPeso::class.java))
        }

        binding.buttonSair.setOnClickListener {
            authViewModel.logout()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        binding.opcaoAlterarSenha.setOnClickListener {
            authViewModel.sendPasswordResetEmail {
                Toast.makeText(
                    this,
                    "Enviamos um link para o seu e-mail para redefinir a senha.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        binding.sobreApp.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_sobre_app, null)
            val alertDialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create()
            alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialogView.findViewById<Button>(R.id.btn_fechar_sobre).setOnClickListener {
                alertDialog.dismiss()
            }
            alertDialog.show()
        }
    }

    private fun exibirDialogoEditarPerfil(currentUser: User) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_editar_perfil, null)
        val alertDialog = AlertDialog.Builder(this).setView(dialogView).create()
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val editNome = dialogView.findViewById<EditText>(R.id.edit_nome_dialog)
        val editIdade = dialogView.findViewById<EditText>(R.id.edit_idade_dialog)
        val editPeso = dialogView.findViewById<EditText>(R.id.edit_peso_dialog)
        val editAltura = dialogView.findViewById<EditText>(R.id.edit_altura_dialog)
        val radioFeminino = dialogView.findViewById<RadioButton>(R.id.radioFemininoDialog)
        val btnSalvar = dialogView.findViewById<Button>(R.id.btn_salvar)

        editNome.setText(currentUser.nome)
        editIdade.setText(currentUser.idade.toString())
        editPeso.setText(currentUser.peso.toString())
        editAltura.setText(currentUser.altura.toString())
        if (currentUser.genero.equals("Feminino", ignoreCase = true)) {
            radioFeminino.isChecked = true
        } else {
            dialogView.findViewById<RadioButton>(R.id.radioMasculinoDialog).isChecked = true
        }

        btnSalvar.setOnClickListener {
            val nome = editNome.text.toString().trim()
            val idade = editIdade.text.toString().toIntOrNull() ?: currentUser.idade
            val peso = editPeso.text.toString().toFloatOrNull() ?: currentUser.peso
            val altura = editAltura.text.toString().toFloatOrNull() ?: currentUser.altura
            val genero = if (radioFeminino.isChecked) "Feminino" else "Masculino"

            if (nome.isNotEmpty()) {
                val updatedUser =
                    currentUser.copy(nome = nome, idade = idade, peso = peso, altura = altura, genero = genero)
                settingsViewModel.updateUserProfile(updatedUser)
                alertDialog.dismiss()
            } else {
                Toast.makeText(this, "O nome não pode ser vazio.", Toast.LENGTH_SHORT).show()
            }
        }

        dialogView.findViewById<Button>(R.id.btn_fechar).setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data?.data != null) {
            val selectedImageUri: Uri = data.data!!
            settingsViewModel.uploadProfilePicture(selectedImageUri)
        }
    }

    private fun configurarNavBar() {
        val navBar = binding.navigationBar
        navBar.botaoInicio.setOnClickListener { startActivity(Intent(this, Bemvindouser::class.java)) }
        navBar.botaoAnotacoes.setOnClickListener { startActivity(Intent(this, anotacoes::class.java)) }
        navBar.botaoHabitos.setOnClickListener { startActivity(Intent(this, HabitosActivity::class.java)) }
        navBar.botaoTreinos.setOnClickListener { startActivity(Intent(this, treinos::class.java)) }
        navBar.botaoCronometro.setOnClickListener { startActivity(Intent(this, CronometroActivity::class.java)) }
        navBar.botaoSugestoes.setOnClickListener { /* lógica sugestões */ }
    }
}
