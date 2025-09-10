package com.example.meuappfirebase

import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.apol.myapplication.BlocosAdapter
import com.apol.myapplication.data.model.Bloco
import com.apol.myapplication.data.model.Note
import com.apol.myapplication.data.model.TipoLembrete
import com.example.meuappfirebase.databinding.ActivityAnotacoesBinding
import java.util.*

class anotacoes : AppCompatActivity() {

    private lateinit var binding: ActivityAnotacoesBinding
    private val viewModel: NotesViewModel by viewModels()
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var blocosAdapter: BlocosAdapter

    private var modoExclusaoAtivo = false
    private var modoExclusaoBlocosAtivo = false
    private var modoBlocosAtivo = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnotacoesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAdapters()
        setupListeners()
        observarViewModel()
        configurarNavBar()
        criarCanalNotificacao()

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        modoBlocosAtivo = prefs.getBoolean("modo_blocos_ativo", false)
        atualizarModoUI()

        if (intent.getBooleanExtra("abrir_dialogo_novo_bloco", false)) {
            mostrarDialogoCriarBloco()
        }
    }

    override fun onBackPressed() {
        finishAffinity()
    }

    private fun observarViewModel() {
        viewModel.blocos.observe(this) { listaDeBlocos ->
            // O ListAdapter é inteligente. Ele só vai redesenhar os itens que mudaram.
            blocosAdapter.submitList(listaDeBlocos)
        }
        viewModel.notes.observe(this) { notes -> notesAdapter.submitList(notes) }
        viewModel.blocos.observe(this) { blocos -> blocosAdapter.submitList(blocos) }
        viewModel.statusMessage.observe(this) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                if (message.contains("Permissão")) {
                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).also { startActivity(it) }
                }
            }
        }
    }

    private fun setupAdapters() {
        notesAdapter = NotesAdapter(
            onItemClick = { note -> if (modoExclusaoAtivo) toggleSelection(note) else openEditDialog(note) },
            onItemLongClick = { note -> if (!modoExclusaoAtivo) ativarModoExclusao(); toggleSelection(note) }
        )
        blocosAdapter = BlocosAdapter(
            onItemClick = { bloco -> if (modoExclusaoBlocosAtivo) toggleBlocoSelection(bloco) else abrirDialogEditarBloco(bloco) },
            onItemLongClick = { bloco -> if (!modoExclusaoBlocosAtivo) ativarModoExclusaoBlocos(); toggleBlocoSelection(bloco) },
            onFavoriteClick = { bloco -> viewModel.toggleFavoritoBloco(bloco) } // <<-- CONECTADO AQUI
        )
        binding.recyclerViewNotes.layoutManager = LinearLayoutManager(this)
    }

    private fun setupListeners() {
        binding.buttonToggleMode.setOnClickListener {
            modoBlocosAtivo = !modoBlocosAtivo
            getSharedPreferences("app_prefs", MODE_PRIVATE).edit().putBoolean("modo_blocos_ativo", modoBlocosAtivo).apply()
            atualizarModoUI()
        }
        binding.buttonDeleteSelected.setOnClickListener {
            if (modoBlocosAtivo) {
                val selecionados = blocosAdapter.getSelecionados()
                if (selecionados.isNotEmpty()) confirmarExclusao("bloco(s)", selecionados.size) { viewModel.deleteBlocos(selecionados); desativarModoExclusaoBlocos() }
            } else {
                val selecionados = notesAdapter.getSelecionados()
                if (selecionados.isNotEmpty()) confirmarExclusao("anotação(ões)", selecionados.size) { viewModel.deleteNotes(selecionados); desativarModoExclusao() }
            }
        }
        binding.clickOutsideView.setOnClickListener {
            if (modoExclusaoAtivo) desativarModoExclusao()
            if (modoExclusaoBlocosAtivo) desativarModoExclusaoBlocos()
        }
    }

    private fun atualizarModoUI() {
        if (modoBlocosAtivo) {
            binding.textView26.text = "Meus Blocos"
            binding.iconHeader.setImageResource(R.drawable.ic_block)
            binding.editNote.hint = "Clique para adicionar novo bloco"
            binding.editNote.isFocusable = false
            binding.editNote.isClickable = true
            binding.editNote.setOnClickListener { mostrarDialogoCriarBloco() }
            binding.buttonAddNote.setOnClickListener { mostrarDialogoCriarBloco() }
            binding.recyclerViewNotes.adapter = blocosAdapter
        } else {
            binding.textView26.text = "Minhas Anotações"
            binding.iconHeader.setImageResource(R.drawable.ic_notes)
            binding.editNote.hint = "O que deseja anotar?"
            binding.editNote.isFocusableInTouchMode = true
            binding.editNote.isClickable = false
            binding.editNote.setOnClickListener(null)
            binding.buttonAddNote.setOnClickListener {
                val text = binding.editNote.text.toString().trim()
                if (text.isNotEmpty()) {
                    viewModel.addNote(text)
                    binding.editNote.text.clear()
                }
            }
            binding.recyclerViewNotes.adapter = notesAdapter
        }
        desativarModoExclusao()
        desativarModoExclusaoBlocos()
    }

    private fun mostrarDialogoCriarBloco() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_selecionar_bloco, null)
        val dialog = AlertDialog.Builder(this, R.style.Theme_HAM_Dialog_Transparent).setView(dialogView).create()
        val adicionarBloco: (String) -> Unit = { nomeBloco -> viewModel.addBloco(nomeBloco); dialog.dismiss() }
        dialogView.findViewById<Button>(R.id.bt_financas).setOnClickListener { adicionarBloco("Finanças") }
        dialogView.findViewById<Button>(R.id.bt_estudos).setOnClickListener { adicionarBloco("Estudos") }
        dialogView.findViewById<Button>(R.id.bt_metas).setOnClickListener { adicionarBloco("Metas") }
        dialogView.findViewById<Button>(R.id.bt_trabalho).setOnClickListener { adicionarBloco("Trabalho") }
        dialogView.findViewById<Button>(R.id.bt_saude).setOnClickListener { adicionarBloco("Saúde") }
        dialog.show()
    }

    private fun abrirDialogEditarBloco(bloco: Bloco) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_editar_bloco, null)
        val dialog = AlertDialog.Builder(this, R.style.Theme_HAM_Dialog_Transparent).setView(dialogView).create()

        val tituloBloco = dialogView.findViewById<TextView>(R.id.titulo_bloco)
        val inputSubtitulo = dialogView.findViewById<EditText>(R.id.input_subtitulo)
        val inputAnotacoes = dialogView.findViewById<EditText>(R.id.input_anotacoes)
        val inputMensagemNotificacao = dialogView.findViewById<EditText>(R.id.input_mensagem_notificacao)
        val btnConfigurarLembrete = dialogView.findViewById<Button>(R.id.btn_configurar_lembrete)
        val btnSalvar = dialogView.findViewById<Button>(R.id.botao_salvar)

        val blocoTemporario = bloco.copy()
        tituloBloco.text = blocoTemporario.nome
        inputSubtitulo.setText(blocoTemporario.subtitulo)
        inputAnotacoes.setText(blocoTemporario.anotacao)
        inputMensagemNotificacao.setText(blocoTemporario.mensagemNotificacao)

        btnConfigurarLembrete.setOnClickListener {
            abrirDialogConfigurarLembrete(blocoTemporario) { configAlterada ->
                blocoTemporario.tipoLembrete = configAlterada.tipoLembrete
                blocoTemporario.diasLembrete = configAlterada.diasLembrete
                blocoTemporario.horariosLembrete = configAlterada.horariosLembrete
                blocoTemporario.segundosLembrete = configAlterada.segundosLembrete
            }
        }

        btnSalvar.setOnClickListener {
            val blocoAtualizado = bloco.copy(
                subtitulo = inputSubtitulo.text.toString().trim(),
                anotacao = inputAnotacoes.text.toString().trim(),
                mensagemNotificacao = inputMensagemNotificacao.text.toString().trim(),
                tipoLembrete = blocoTemporario.tipoLembrete,
                diasLembrete = blocoTemporario.diasLembrete,
                horariosLembrete = blocoTemporario.horariosLembrete,
                segundosLembrete = blocoTemporario.segundosLembrete
            )
            viewModel.updateBloco(blocoAtualizado)
            dialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.botao_cancelar).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun openEditDialog(note: Note) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_note, null)
        val dialog = AlertDialog.Builder(this, R.style.Theme_HAM_Dialog_Transparent).setView(dialogView).create()
        val editText = dialogView.findViewById<EditText>(R.id.editNoteDialog)
        val btnSalvar = dialogView.findViewById<Button>(R.id.btn_salvar_edit_note)
        editText.setText(note.text)
        btnSalvar.setOnClickListener {
            val newText = editText.text.toString().trim()
            if (newText.isNotEmpty()) {
                viewModel.updateNote(note.copy(text = newText))
            }
            dialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.btn_cancelar_edit_note).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun abrirDialogConfigurarLembrete(bloco: Bloco, onSave: (Bloco) -> Unit) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_configurar_lembrete, null)
        val dialog = AlertDialog.Builder(this, R.style.Theme_HAM_Dialog_Transparent).setView(dialogView).create()
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radio_group_tipo_lembrete)
        val radioNenhum = dialogView.findViewById<RadioButton>(R.id.radio_nenhum)
        val radioDiario = dialogView.findViewById<RadioButton>(R.id.radio_diario)
        val radioMensal = dialogView.findViewById<RadioButton>(R.id.radio_mensal)
        val layoutDia = dialogView.findViewById<View>(R.id.layout_seletor_dia)
        val textDia = dialogView.findViewById<TextView>(R.id.text_dia_selecionado)
        val layoutHora = dialogView.findViewById<View>(R.id.layout_seletor_hora)
        val textHora = dialogView.findViewById<TextView>(R.id.text_hora_selecionada)
        val btnSalvar = dialogView.findViewById<Button>(R.id.btn_salvar_lembrete)

        var diaSelecionado = bloco.diasLembrete.firstOrNull() ?: Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        var horaSelecionada = 0
        var minutoSelecionado = 0
        if (bloco.horariosLembrete.isNotEmpty()) {
            val parts = bloco.horariosLembrete.first().split(":")
            if (parts.size == 2) {
                horaSelecionada = parts[0].toInt()
                minutoSelecionado = parts[1].toInt()
            }
        }
        fun updateUi(tipo: TipoLembrete) {
            layoutDia.visibility = if (tipo == TipoLembrete.MENSAL) View.VISIBLE else View.GONE
            layoutHora.visibility = if (tipo == TipoLembrete.DIARIO || tipo == TipoLembrete.MENSAL) View.VISIBLE else View.GONE
        }
        textDia.text = "Dia $diaSelecionado"
        textHora.text = String.format(Locale.getDefault(), "%02d:%02d", horaSelecionada, minutoSelecionado)
        when (bloco.tipoLembrete) {
            TipoLembrete.NENHUM -> radioNenhum.isChecked = true
            TipoLembrete.DIARIO -> radioDiario.isChecked = true
            TipoLembrete.MENSAL -> radioMensal.isChecked = true
            else -> radioNenhum.isChecked = true
        }
        updateUi(bloco.tipoLembrete)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radio_nenhum -> updateUi(TipoLembrete.NENHUM)
                R.id.radio_diario -> updateUi(TipoLembrete.DIARIO)
                R.id.radio_mensal -> updateUi(TipoLembrete.MENSAL)
            }
        }
        textDia.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, R.style.AppTheme_Dialog_Picker, { _, _, _, dayOfMonth ->
                diaSelecionado = dayOfMonth
                textDia.text = "Dia $diaSelecionado"
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
        textHora.setOnClickListener {
            TimePickerDialog(this, R.style.AppTheme_TimePickerDialog, { _, hourOfDay, minute ->
                horaSelecionada = hourOfDay; minutoSelecionado = minute
                textHora.text = String.format(Locale.getDefault(), "%02d:%02d", horaSelecionada, minutoSelecionado)
            }, horaSelecionada, minutoSelecionado, true).show()
        }
        btnSalvar.setOnClickListener {
            val blocoConfigurado = bloco.copy()
            when (radioGroup.checkedRadioButtonId) {
                R.id.radio_diario -> {
                    blocoConfigurado.tipoLembrete = TipoLembrete.DIARIO
                    blocoConfigurado.diasLembrete = emptyList()
                    blocoConfigurado.horariosLembrete = listOf(String.format(Locale.getDefault(), "%02d:%02d", horaSelecionada, minutoSelecionado))
                }
                R.id.radio_mensal -> {
                    blocoConfigurado.tipoLembrete = TipoLembrete.MENSAL
                    blocoConfigurado.diasLembrete = listOf(diaSelecionado)
                    blocoConfigurado.horariosLembrete = listOf(String.format(Locale.getDefault(), "%02d:%02d", horaSelecionada, minutoSelecionado))
                }
                else -> { // Nenhum
                    blocoConfigurado.tipoLembrete = TipoLembrete.NENHUM
                    blocoConfigurado.diasLembrete = emptyList()
                    blocoConfigurado.horariosLembrete = emptyList()
                }
            }
            onSave(blocoConfigurado)
            dialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.btn_cancelar_lembrete).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun toggleSelection(note: Note) { notesAdapter.toggleSelecao(note); atualizarBotaoApagar() }
    private fun toggleBlocoSelection(bloco: Bloco) { blocosAdapter.toggleSelection(bloco); atualizarBotaoApagar() }
    private fun ativarModoExclusao() { modoExclusaoAtivo = true; binding.buttonDeleteSelected.visibility = View.VISIBLE }
    private fun desativarModoExclusao() { modoExclusaoAtivo = false; notesAdapter.limparSelecao(); binding.buttonDeleteSelected.visibility = View.GONE }
    private fun ativarModoExclusaoBlocos() { modoExclusaoBlocosAtivo = true; binding.buttonDeleteSelected.visibility = View.VISIBLE }
    private fun desativarModoExclusaoBlocos() { modoExclusaoBlocosAtivo = false; blocosAdapter.limparSelecao(); binding.buttonDeleteSelected.visibility = View.GONE }
    private fun atualizarBotaoApagar() {
        val count = if (modoBlocosAtivo) blocosAdapter.getSelecionados().size else notesAdapter.getSelecionados().size
        binding.buttonDeleteSelected.visibility = if (count > 0) View.VISIBLE else View.GONE
    }

    private fun confirmarExclusao(tipo: String, quantidade: Int, onConfirm: () -> Unit) {
        AlertDialog.Builder(this).setTitle("Excluir $tipo").setMessage("Tem certeza que deseja apagar $quantidade item(ns)?").setPositiveButton("Excluir") { _, _ -> onConfirm() }.setNegativeButton("Cancelar", null).show()
    }

    private fun criarCanalNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel("canal_lembrete", "Lembretes", NotificationManager.IMPORTANCE_HIGH)
            getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
        }
    }

    private fun configurarNavBar() {
        binding.navigationBar.botaoInicio.setOnClickListener { startActivity(Intent(this, Bemvindouser::class.java)) }
        binding.navigationBar.botaoAnotacoes.setOnClickListener { /* já está aqui */ }
        binding.navigationBar.botaoHabitos.setOnClickListener { startActivity(Intent(this, HabitosActivity::class.java)) }
        binding.navigationBar.botaoTreinos.setOnClickListener { startActivity(Intent(this, treinos::class.java)) }
        binding.navigationBar.botaoCronometro.setOnClickListener { startActivity(Intent(this, CronometroActivity::class.java)) }
        binding.navigationBar.botaoSugestoes.setOnClickListener { startActivity(Intent(this, SugestaoUser::class.java)) }
    }
}