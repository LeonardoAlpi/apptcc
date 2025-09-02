package com.apol.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.apol.myapplication.data.model.UserDao
import com.example.meuappfirebase.MainActivity
import com.example.meuappfirebase.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class bemvindo : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao
    private lateinit var textViewSaudacao: TextView
    private lateinit var textViewAltura: TextView
    private lateinit var textViewPeso: TextView
    private lateinit var textViewIdade: TextView
    private lateinit var botaoLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bemvindo)


        textViewSaudacao = findViewById(R.id.textView22)
        textViewAltura = findViewById(R.id.textView23altura)
        textViewPeso = findViewById(R.id.textView24peso)
        textViewIdade = findViewById(R.id.textView26idade)
        botaoLogout = findViewById(R.id.botaoLogout)

        // Inicialização do banco
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "app_database"
        ).build()
        userDao = db.userDao()

        // Firebase UID
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        Log.d("BEMVINDO", "UID atual: $userId")

        // Buscar dados do usuário
        lifecycleScope.launch {
            val user = userId?.let { userDao.getUserById(it) }
            Log.d("BEMVINDO", "Usuário encontrado: ${user?.nome}, gênero: ${user?.genero}")

            user?.let {
                val nome = it.nome
                val genero = it.genero
                val idade = it.idade
                val altura = it.altura
                val peso = it.peso

                val saudacao = if (genero.equals("Feminino", ignoreCase = true)) {
                    "Bem-vinda, $nome"
                } else {
                    "Bem-vindo, $nome"
                }

                runOnUiThread {
                    textViewSaudacao.text = saudacao
                    textViewIdade.text = "Idade: $idade anos"
                    textViewAltura.text = "Altura: ${"%.2f".format(altura)} m"
                    textViewPeso.text = "Peso: $peso kg"
                }
            } ?: runOnUiThread {
                textViewSaudacao.text = "Bem-vindo(a)!"
                textViewIdade.text = "Idade: -"
                textViewAltura.text = "Altura: -"
                textViewPeso.text = "Peso: -"
            }
        }

        // Lógica do botão de logout
        botaoLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this@bemvindo, MainActivity::class.java) // Troque se seu login tiver outro nome
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
