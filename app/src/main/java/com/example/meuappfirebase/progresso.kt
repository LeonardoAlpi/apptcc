package com.apol.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.meuappfirebase.Bemvindouser
import com.example.meuappfirebase.R
import com.example.meuappfirebase.anotacoes
import com.example.meuappfirebase.configuracoes
import com.example.meuappfirebase.treinos
import com.example.meuappfirebase.HabitosActivity

class progresso : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_progresso)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navBar = findViewById<LinearLayout>(R.id.navigation_bar)

        navBar.findViewById<LinearLayout>(R.id.botao_inicio).setOnClickListener {
            startActivity(Intent(this, Bemvindouser::class.java))
        }

        navBar.findViewById<LinearLayout>(R.id.botao_anotacoes).setOnClickListener {
            startActivity(Intent(this, anotacoes::class.java))
        }

        navBar.findViewById<LinearLayout>(R.id.botao_habitos).setOnClickListener {
            startActivity(Intent(this, HabitosActivity::class.java))
        }

        navBar.findViewById<LinearLayout>(R.id.botao_treinos).setOnClickListener {
            startActivity(Intent(this, treinos::class.java))
        }

        navBar.findViewById<LinearLayout>(R.id.botao_cronometro).setOnClickListener {
            // Já está na tela cronometro, não faz nada
        }

        navBar.findViewById<LinearLayout>(R.id.botao_sugestoes).setOnClickListener {
            startActivity(Intent(this, configuracoes::class.java))
        }
    }
}