package com.example.meuappfirebase

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.meuappfirebase.databinding.ActivityHabitosBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.apol.myapplication.DailyCheckupScheduler
import java.util.*

class HabitosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHabitosBinding
    private val viewModel: HabitosViewModel by viewModels()
    private lateinit var habitsAdapter: HabitsAdapter
    private var modoExclusaoAtivo = false

    private val allDays = setOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Permissão concedida! Lembretes serão agendados.", Toast.LENGTH_SHORT).show()
            viewModel.tryToScheduleHabitReminders()
        } else {
            Toast.makeText(this, "Permissão negada. Os lembretes de hábitos não funcionarão.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHabitosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupListeners()
        configurarNavBar()
        observarViewModel()

        pedirPermissaoDeNotificacao()
        viewModel.tryToScheduleHabitReminders()

        DailyCheckupScheduler.scheduleNextCheckup(this.applicationContext)

        val abrirDialogo = intent.getBooleanExtra("ABRIR_DIALOGO_NOVO_HABITO", false)
        if (abrirDialogo) mostrarDialogoNovoHabito()
    }

    private fun pedirPermissaoDeNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun observarViewModel() {
        lifecycleScope.launch {
            viewModel.habitsList.collect { listaHabitos ->
                habitsAdapter.submitList(listaHabitos)
            }
        }

        lifecycleScope.launch {
            viewModel.mostrandoHabitosBons.collect { isBons ->
                if (isBons) {
                    binding.habitsTitle.text = "Bons Hábitos"
                    binding.buttonToggleMode.text = "Ruim" // Quando em "Bons", o botão mostra "Ruim"
                } else {
                    binding.habitsTitle.text = "Hábitos a Mudar"
                    // --- MUDANÇA AQUI ---
                    binding.buttonToggleMode.text = "Bom" // Quando em "Mudar", o botão mostra "Bom" (singular)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.operationStatus.collect { message ->
                message?.let {
                    Toast.makeText(this@HabitosActivity, it, Toast.LENGTH_SHORT).show()
                    viewModel.clearOperationStatus()
                }
            }
        }
        lifecycleScope.launch {
            viewModel.permissionEvent.collectLatest {
                abrirTelaDePermissaoDeAlarme()
            }
        }
    }

    private fun abrirTelaDePermissaoDeAlarme() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Toast.makeText(this, "Para lembretes funcionarem, ative esta permissão.", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        habitsAdapter = HabitsAdapter(
            onItemClick = { habit ->
                if (modoExclusaoAtivo) toggleSelecao(habit) else mostrarOpcoesHabito(habit)
            },
            onItemLongClick = { habit ->
                if (!modoExclusaoAtivo) ativarModoExclusao(habit)
            },
            onMarkDone = { habit ->
                viewModel.marcarHabito(habit.id, true)
            },
            onUndoDone = { habit ->
                viewModel.marcarHabito(habit.id, false)
            },
            onToggleFavorite = { habit ->
                viewModel.toggleFavorito(habit)
            },
            emojiExtractor = this::extrairEmoji,
            emojiRemover = this::removerEmoji,
            textDrawableFactory = this::TextDrawable
        )

        binding.recyclerViewHabits.adapter = habitsAdapter
        binding.recyclerViewHabits.layoutManager = LinearLayoutManager(this)
    }

    private fun setupListeners() {
        binding.fabAddHabit.setOnClickListener {
            if (!modoExclusaoAtivo) mostrarDialogoNovoHabito()
        }

        binding.btnDeleteSelected.setOnClickListener {
            val selecionados = habitsAdapter.getSelecionados()
            if (selecionados.isNotEmpty()) confirmarExclusao(selecionados)
        }

        binding.clickOutsideView.setOnClickListener { desativarModoExclusao() }

        binding.buttonToggleMode.setOnClickListener {
            if (modoExclusaoAtivo) desativarModoExclusao()
            viewModel.toggleTipoHabito()
        }
    }

    private fun configurarNavBar() {
        binding.navigationBar.botaoInicio.setOnClickListener {
            startActivity(Intent(this, Bemvindouser::class.java)); finish()
        }
        binding.navigationBar.botaoAnotacoes.setOnClickListener {
            startActivity(Intent(this, anotacoes::class.java)); finish()
        }
        binding.navigationBar.botaoHabitos.setOnClickListener { /* tela atual */ }
        binding.navigationBar.botaoTreinos.setOnClickListener {
            startActivity(Intent(this, treinos::class.java)); finish()
        }
        binding.navigationBar.botaoCronometro.setOnClickListener {
            startActivity(Intent(this, CronometroActivity::class.java)); finish()
        }
        binding.navigationBar.botaoSugestoes.setOnClickListener {
            startActivity(Intent(this, SugestaoUser::class.java)); finish()
        }
    }

    private fun mostrarDialogoNovoHabito() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_novo_habito, null)
        val dialog =
            AlertDialog.Builder(this, R.style.Theme_HAM_Dialog_Transparent).setView(dialogView).create()

        val etHabitName = dialogView.findViewById<EditText>(R.id.et_habit_name)
        val btnAdicionar = dialogView.findViewById<Button>(R.id.btn_adicionar_habito)
        val toggles = getTogglesFromDialog(dialogView)

        btnAdicionar.setOnClickListener {
            val nome = etHabitName.text.toString().trim()
            if (nome.isEmpty()) {
                Toast.makeText(this, "O nome do hábito não pode ser vazio.", Toast.LENGTH_SHORT).show()
            } else {
                val diasSelecionados = toggles.filter { it.value.isChecked }.keys
                val diasParaSalvar = if (diasSelecionados.isEmpty()) allDays else diasSelecionados
                viewModel.adicionarHabito(nome, diasParaSalvar, viewModel.mostrandoHabitosBons.value)
                dialog.dismiss()
            }
        }

        dialogView.findViewById<Button>(R.id.btn_cancelar_habito)
            .setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun mostrarDialogoEditarHabito(habit: HabitUI) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_novo_habito, null)
        val dialog =
            AlertDialog.Builder(this, R.style.Theme_HAM_Dialog_Transparent).setView(dialogView).create()

        val title = dialogView.findViewById<TextView>(R.id.dialog_title)
        val etHabitName = dialogView.findViewById<EditText>(R.id.et_habit_name)
        val btnSalvar = dialogView.findViewById<Button>(R.id.btn_adicionar_habito)
        val toggles = getTogglesFromDialog(dialogView)

        title.text = "Editar Hábito"
        btnSalvar.text = "Salvar"
        etHabitName.setText(habit.name)

        // ✅ Marca automaticamente os dias já salvos
        habit.daysOfWeek.forEach { day ->
            toggles[day]?.isChecked = true
        }

        btnSalvar.setOnClickListener {
            val novoNome = etHabitName.text.toString().trim()
            if (novoNome.isEmpty()) {
                Toast.makeText(this, "O nome não pode ser vazio.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val novosDias = toggles.filter { it.value.isChecked }.keys
            val diasParaSalvar = if (novosDias.isEmpty()) allDays else novosDias

            viewModel.updateHabit(habit.id, novoNome, diasParaSalvar)
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btn_cancelar_habito)
            .setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun mostrarOpcoesHabito(habit: HabitUI) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_opcoes_habito, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val title = dialogView.findViewById<TextView>(R.id.dialog_options_title)
        val btnProgresso = dialogView.findViewById<Button>(R.id.btn_ver_progresso)
        val btnEditar = dialogView.findViewById<Button>(R.id.btn_editar_habito)

        title.text = removerEmoji(habit.name)

        btnProgresso.setOnClickListener {
            val intent = Intent(this, ActivityProgressoHabito::class.java)
            intent.putExtra("habit_id_string", habit.id)
            startActivity(intent)
            dialog.dismiss()
        }

        btnEditar.setOnClickListener {
            mostrarDialogoEditarHabito(habit)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun ativarModoExclusao(habit: HabitUI) {
        modoExclusaoAtivo = true
        habitsAdapter.setModoExclusao(true)
        binding.fabAddHabit.visibility = View.GONE
        binding.btnDeleteSelected.visibility = View.VISIBLE
        binding.clickOutsideView.visibility = View.VISIBLE
        toggleSelecao(habit)
    }

    private fun desativarModoExclusao() {
        modoExclusaoAtivo = false
        habitsAdapter.setModoExclusao(false)
        habitsAdapter.limparSelecao()
        binding.fabAddHabit.visibility = View.VISIBLE
        binding.btnDeleteSelected.visibility = View.GONE
        binding.clickOutsideView.visibility = View.GONE
    }

    private fun toggleSelecao(habit: HabitUI) {
        habitsAdapter.toggleSelecao(habit)
        if (habitsAdapter.getSelecionados().isEmpty() && modoExclusaoAtivo) desativarModoExclusao()
    }

    private fun confirmarExclusao(habitosParaApagar: List<HabitUI>) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Hábitos")
            .setMessage("Tem certeza que deseja apagar ${habitosParaApagar.size} hábito(s)?")
            .setPositiveButton("Excluir") { _, _ ->
                viewModel.executarExclusao(habitosParaApagar)
                desativarModoExclusao()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun getTogglesFromDialog(dialogView: View): Map<String, ToggleButton> = mapOf(
        "SUN" to dialogView.findViewById(R.id.toggle_dom),
        "MON" to dialogView.findViewById(R.id.toggle_seg),
        "TUE" to dialogView.findViewById(R.id.toggle_ter),
        "WED" to dialogView.findViewById(R.id.toggle_qua),
        "THU" to dialogView.findViewById(R.id.toggle_qui),
        "FRI" to dialogView.findViewById(R.id.toggle_sex),
        "SAT" to dialogView.findViewById(R.id.toggle_sab)
    )

    fun extrairEmoji(texto: String): String = Regex("^\\p{So}").find(texto)?.value ?: ""
    fun removerEmoji(texto: String): String = texto.replaceFirst(Regex("^\\p{So}\\s*"), "")

    fun TextDrawable(context: Context, text: String): Drawable {
        return object : Drawable() {
            private val paint = Paint().apply {
                color = Color.WHITE // ✅ Ícone/texto branco
                textSize = 64f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
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
