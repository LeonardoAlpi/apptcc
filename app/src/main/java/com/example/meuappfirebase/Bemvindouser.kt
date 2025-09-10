package com.example.meuappfirebase

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.apol.myapplication.data.model.Bloco
import com.apol.myapplication.data.model.Habito
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.meuappfirebase.databinding.ActivityBemvindouserBinding
import java.text.SimpleDateFormat
import java.util.*

class Bemvindouser : AppCompatActivity() {

    private lateinit var binding: ActivityBemvindouserBinding
    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBemvindouserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        atualizarDataComSimbolo()
        configurarNavBar()
        configurarBotoesAcao()
        observarViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadInitialData()
    }

    override fun onBackPressed() {
        finishAffinity()
    }


    private fun observarViewModel() {
        viewModel.userProfile.observe(this) { user ->
            if (user != null) {
                val saudacao = if (user.genero.equals("Feminino", ignoreCase = true)) "Bem-vinda" else "Bem-vindo"
                binding.welcomeText.text = "$saudacao, ${user.nome}!"

                if (!user.profilePicUri.isNullOrEmpty()) {
                    Glide.with(this).load(user.profilePicUri).apply(RequestOptions.circleCropTransform()).into(binding.ivProfilePicture)
                } else {
                    val initials = user.nome.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("")
                    val placeholder = InitialsDrawable(initials, getColor(R.color.roxo)) // Adapte a cor se necessário
                    binding.ivProfilePicture.setImageDrawable(placeholder)
                }
            } else {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        viewModel.topHabits.observe(this) { habitos ->
            atualizarWidgetHabitos(habitos)
        }

        viewModel.topBlocos.observe(this) { blocos ->
            atualizarWidgetBlocos(blocos)
        }

        viewModel.operationStatus.observe(this) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun atualizarWidgetHabitos(habitosFavoritados: List<Habito>) {
        val widgetHabitos = binding.widgetHabitos.root
        val emptyStateTextView = widgetHabitos.findViewById<TextView>(R.id.tv_empty_habits_widget)

        emptyStateTextView.isVisible = habitosFavoritados.isEmpty()

        val slots = listOf(
            Triple(widgetHabitos.findViewById<View>(R.id.habito_slot_1), widgetHabitos.findViewById<ImageView>(R.id.habito_icon_1), widgetHabitos.findViewById<TextView>(R.id.habito_text_1)),
            Triple(widgetHabitos.findViewById<View>(R.id.habito_slot_2), widgetHabitos.findViewById<ImageView>(R.id.habito_icon_2), widgetHabitos.findViewById<TextView>(R.id.habito_text_2)),
            Triple(widgetHabitos.findViewById<View>(R.id.habito_slot_3), widgetHabitos.findViewById<ImageView>(R.id.habito_icon_3), widgetHabitos.findViewById<TextView>(R.id.habito_text_3))
        )

        for (i in slots.indices) {
            val (slotView, iconView, textView) = slots[i]
            val habito = habitosFavoritados.getOrNull(i)

            if (habito != null) {
                slotView.isVisible = true

                // --- MUDANÇA PARA SEPARAR ID E NOME ---
                val partes = habito.nome.split(";;;")
                val habitId = partes[0]
                val habitName = if (partes.size > 1) partes[1] else ""

                val emoji = extrairEmoji(habitName)
                textView.text = removerEmoji(habitName)

                if (emoji.isNotEmpty()) {
                    iconView.setImageDrawable(TextDrawable(this, emoji))
                } else {
                    iconView.setImageResource(R.drawable.ic_habits)
                }

                // --- MUDANÇA PRINCIPAL NO CLIQUE ---
                slotView.setOnClickListener {
                    val intent = Intent(this, ActivityProgressoHabito::class.java)
                    // Agora envia a CHAVE e o TIPO corretos que a tela de progresso espera
                    intent.putExtra("habit_id_string", habitId)
                    startActivity(intent)
                }
            } else {
                slotView.isVisible = false
            }
        }
    }

    private fun atualizarWidgetBlocos(blocosFavoritados: List<Bloco>) {
        val widgetBlocos = binding.widgetBlocos.root
        val emptyStateTextView = widgetBlocos.findViewById<TextView>(R.id.tv_empty_blocos_widget)

        emptyStateTextView.isVisible = blocosFavoritados.isEmpty()

        // Lista dos slots do widget
        val slots = listOf(
            Triple(widgetBlocos.findViewById<View>(R.id.bloco_slot_1), widgetBlocos.findViewById<ImageView>(R.id.bloco_icon_1), widgetBlocos.findViewById<TextView>(R.id.bloco_text_1)),
            Triple(widgetBlocos.findViewById<View>(R.id.bloco_slot_2), widgetBlocos.findViewById<ImageView>(R.id.bloco_icon_2), widgetBlocos.findViewById<TextView>(R.id.bloco_text_2)),
            Triple(widgetBlocos.findViewById<View>(R.id.bloco_slot_3), widgetBlocos.findViewById<ImageView>(R.id.bloco_icon_3), widgetBlocos.findViewById<TextView>(R.id.bloco_text_3))
        )

        for (i in slots.indices) {
            val (slotView, iconView, textView) = slots[i]
            val bloco = blocosFavoritados.getOrNull(i)

            if (bloco != null) {
                slotView.isVisible = true
                textView.text = bloco.nome

                // Aqui você pode definir um ícone específico se quiser, ou um padrão
                iconView.setImageResource(R.drawable.ic_block)

                slotView.setOnClickListener {
                    val intent = Intent(this, anotacoes::class.java).apply {
                        // Abre a tela de anotações já no modo blocos e pode até abrir o bloco específico
                        putExtra("modo_blocos_ativo", true)
                        putExtra("abrir_bloco_id", bloco.id)
                    }
                    startActivity(intent)
                }
            } else {
                slotView.isVisible = false
            }
        }
    }

    private fun configurarBotoesAcao() {
        binding.btnProfileSettings.setOnClickListener {
            startActivity(Intent(this, configuracoes::class.java))
        }

        binding.buttonAddThought.setOnClickListener {
            val texto = binding.edittextThought.text.toString().trim()
            if (texto.isNotEmpty()) {
                viewModel.addQuickNote(texto)
                binding.edittextThought.text.clear()
            } else {
                Toast.makeText(this, "Digite algo primeiro!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.widgetHabitos.root.findViewById<View>(R.id.btnNovoHabito).setOnClickListener {
            val intent = Intent(this, HabitosActivity::class.java)
            intent.putExtra("abrir_dialogo_novo_habito", true)
            startActivity(intent)
        }

        binding.widgetBlocos.root.findViewById<View>(R.id.btnNovoBloco).setOnClickListener {
            val intent = Intent(this, anotacoes::class.java)
            intent.putExtra("modo_blocos_ativo", true)
            intent.putExtra("abrir_dialogo_novo_bloco", true)
            startActivity(intent)
        }
    }

    private fun atualizarDataComSimbolo() {
        val calendar = Calendar.getInstance()
        val hora = calendar.get(Calendar.HOUR_OF_DAY)
        val simbolo = if (hora in 6..17) "☀️" else "🌙"
        val formato = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("pt", "BR"))
        val dataFormatada = formato.format(calendar.time).replaceFirstChar { it.uppercase() }
        binding.dateText.text = "$dataFormatada  $simbolo"
    }

    private fun configurarNavBar() {
        val navBar = binding.navigationBar
        navBar.botaoInicio.setOnClickListener {}
        navBar.botaoAnotacoes.setOnClickListener { startActivity(Intent(this, anotacoes::class.java)) }
        navBar.botaoHabitos.setOnClickListener { startActivity(Intent(this, HabitosActivity::class.java)) }
        navBar.botaoTreinos.setOnClickListener { startActivity(Intent(this, treinos::class.java)) }
        navBar.botaoCronometro.setOnClickListener { startActivity(Intent(this, CronometroActivity::class.java)) }
        navBar.botaoSugestoes.setOnClickListener { startActivity(Intent(this, SugestaoUser::class.java)) }
    }

    // Suas funções helper de UI continuam aqui
    private fun extrairEmoji(texto: String): String {
        val regex = Regex("^\\p{So}")
        return regex.find(texto)?.value ?: ""
    }
    private fun removerEmoji(texto: String): String {
        return texto.replaceFirst(Regex("^\\p{So}\\s*"), "")
    }
    private fun TextDrawable(context: Context, text: String): Drawable {
        return object : Drawable() {
            private val paint = Paint()
            init {
                paint.color = Color.WHITE; paint.textSize = 38f; paint.isAntiAlias = true
                paint.textAlign = Paint.Align.CENTER; paint.typeface = Typeface.DEFAULT
            }
            override fun draw(canvas: Canvas) {
                val bounds = bounds
                val x = bounds.centerX().toFloat()
                val y = bounds.centerY() - (paint.descent() + paint.ascent()) / 2
                canvas.drawText(text, x, y, paint)
            }
            override fun setAlpha(alpha: Int) { paint.alpha = alpha }
            override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
            override fun setColorFilter(colorFilter: ColorFilter?) { paint.colorFilter = colorFilter }
        }
    }
}