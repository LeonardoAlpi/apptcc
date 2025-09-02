package com.example.meuappfirebase

import android.content.Intent
import android.os.Bundle
import android.util.Log // Importe a classe Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.meuappfirebase.databinding.ActivityRoteadorBinding
import kotlinx.coroutines.launch

class RoteadorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoteadorBinding
    private val viewModel: AuthViewModel by viewModels()

    // Crie uma TAG para filtrar nossas mensagens no Logcat
    private val TAG = "RoteadorDebug"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoteadorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        rotearUsuario()
    }

    private fun rotearUsuario() {
        lifecycleScope.launch {
            // 1. Verificando se a função de roteamento começou
            Log.d(TAG, "Iniciando roteamento...")

            val user = viewModel.getCurrentUserFromRoom()

            // 2. Verificando o que veio do banco de dados local
            Log.d(TAG, "Dados do usuário no Room: $user")

            if (user == null) {
                // 3. Verificando se o usuário é nulo
                Log.w(TAG, "Usuário NULO no Room! Navegando para a MainActivity.")
                navegarPara(MainActivity::class.java)
                return@launch
            }

            val proximaTela = when {
                user.nome.isNullOrEmpty() -> {
                    Log.d(TAG, "Decisão: nome está vazio. Indo para infousuario.")
                    infousuario::class.java
                }
                user.temHabitoLeitura == null || user.segueDieta == null -> {
                    Log.d(TAG, "Decisão: hábitos de leitura/dieta nulos. Indo para livro.")
                    livro::class.java
                }
                user.habitosNegativos.isNullOrEmpty() || user.problemasEmocionais.isNullOrEmpty() -> {
                    Log.d(TAG, "Decisão: saúde mental vazia. Indo para saudemental.")
                    saudemental::class.java
                }
                user.praticaAtividade.isNullOrEmpty() -> {
                    Log.d(TAG, "Decisão: atividade física vazia. Indo para pergunta01.")
                    pergunta01::class.java
                }
                user.sugestoesInteresse.isNullOrEmpty() -> {
                    Log.d(TAG, "Decisão: sugestões de interesse vazias. Indo para sujestao.")
                    sujestao::class.java
                }
                else -> {
                    Log.d(TAG, "Decisão: Tudo preenchido! Indo para a tela principal Bemvindouser.")
                    Bemvindouser::class.java
                }
            }

            navegarPara(proximaTela)
        }
    }

    private fun <T> navegarPara(proximaTela: Class<T>) {
        // 4. Verificando para qual tela estamos finalmente navegando
        Log.i(TAG, "Navegando para: ${proximaTela.simpleName}")

        val intent = Intent(this, proximaTela)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}