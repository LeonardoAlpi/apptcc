package com.example.meuappfirebase // Pacote corrigido

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.apol.myapplication.data.model.TreinoNota
import com.apol.myapplication.data.model.TreinoNotaAdapter
import com.example.meuappfirebase.databinding.ActivityDivisaoDetalheBinding

class DivisaoDetalheActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDivisaoDetalheBinding
    private val viewModel: WorkoutSplitDetailViewModel by viewModels()
    private lateinit var notaAdapter: TreinoNotaAdapter
    private var divisaoId: Long = -1L
    private var modoExclusaoAtivo = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDivisaoDetalheBinding.inflate(layoutInflater)
        setContentView(binding.root)

        divisaoId = intent.getLongExtra("DIVISAO_ID", -1L)
        if (divisaoId == -1L) { finish(); return }

        binding.nomeDivisaoDetalhe.text = intent.getStringExtra("DIVISAO_NOME") ?: "Anotações"

        setupRecyclerView()
        setupListeners()
        observarViewModel()

        // Pede ao ViewModel para carregar os dados iniciais
        viewModel.loadNotes(divisaoId)
    }

    override fun onBackPressed() {
        if (modoExclusaoAtivo) {
            desativarModoExclusao()
        } else {
            super.onBackPressed()
        }
    }

    private fun observarViewModel() {
        viewModel.notes.observe(this) { notas ->
            notaAdapter.submitList(notas)
        }
    }

    private fun setupRecyclerView() {
        notaAdapter = TreinoNotaAdapter(
            mutableListOf(),
            onItemClick = { nota ->
                if (modoExclusaoAtivo) {
                    toggleSelecao(nota)
                } else {
                    exibirDialogoEditarNota(nota)
                }
            },
            onItemLongClick = { nota ->
                if (!modoExclusaoAtivo) {
                    ativarModoExclusao(nota)
                }
            }
        )
        binding.recyclerViewExercicios.adapter = notaAdapter
        binding.recyclerViewExercicios.layoutManager = LinearLayoutManager(this)
    }

    private fun setupListeners() {
        binding.fabAddExercicio.setOnClickListener {
            exibirDialogoCriarNota()
        }
        binding.btnVoltarDivisao.setOnClickListener {
            finish()
        }
        binding.btnApagarNotas.setOnClickListener {
            val selecionados = notaAdapter.getSelecionados()
            if (selecionados.isNotEmpty()) {
                confirmarExclusao(selecionados)
            }
        }
    }

    private fun exibirDialogoCriarNota() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_nova_anotacao, null)
        val dialog = AlertDialog.Builder(this, R.style.Theme_HAM_Dialog_Transparent)
            .setView(dialogView)
            .create()

        val editText = dialogView.findViewById<EditText>(R.id.edit_text_new_note)
        val btnCriar = dialogView.findViewById<Button>(R.id.btn_criar_new_note)

        btnCriar.setOnClickListener {
            val titulo = editText.text.toString().trim()
            if (titulo.isNotEmpty()) {
                // Chama o ViewModel para adicionar a nota
                viewModel.addNote(divisaoId, titulo)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "O título não pode ser vazio.", Toast.LENGTH_SHORT).show()
            }
        }
        dialogView.findViewById<Button>(R.id.btn_cancelar_new_note).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun confirmarExclusao(notasParaApagar: List<TreinoNota>) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Anotações")
            .setMessage("Tem certeza que deseja apagar ${notasParaApagar.size} anotação(ões)?")
            .setPositiveButton("Excluir") { _, _ ->
                // Chama o ViewModel para deletar as notas
                viewModel.deleteNotes(notasParaApagar)
                desativarModoExclusao()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun exibirDialogoEditarNota(nota: TreinoNota) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_editar_nota_treino, null)
        val dialog = AlertDialog.Builder(this, R.style.Theme_HAM_Dialog_Transparent)
            .setView(dialogView)
            .create()

        val tituloView = dialogView.findViewById<TextView>(R.id.titulo_dialogo_nota)
        val conteudoInput = dialogView.findViewById<EditText>(R.id.input_conteudo_nota)
        val btnSalvar = dialogView.findViewById<Button>(R.id.btn_salvar_nota)

        tituloView.text = nota.titulo
        conteudoInput.setText(nota.conteudo)

        btnSalvar.setOnClickListener {
            val novoConteudo = conteudoInput.text.toString().trim()
            val notaAtualizada = nota.copy(conteudo = novoConteudo)
            // Chama o ViewModel para atualizar a nota
            viewModel.updateNote(notaAtualizada)
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btn_cancelar_nota).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<Button>(R.id.btn_apagar_nota).setOnClickListener {
            confirmarExclusao(listOf(nota)) // Reutiliza a função de confirmação
            dialog.dismiss()
        }

        dialog.show()
    }

    // Funções de UI como 'ativar/desativarModoExclusao' e 'toggleSelecao' continuam aqui
    private fun ativarModoExclusao(primeiraNota: TreinoNota) {
        modoExclusaoAtivo = true
        notaAdapter.modoExclusaoAtivo = true
        binding.fabAddExercicio.visibility = View.GONE
        binding.btnApagarNotas.visibility = View.VISIBLE
        toggleSelecao(primeiraNota)
    }

    private fun desativarModoExclusao() {
        modoExclusaoAtivo = false
        notaAdapter.limparSelecao()
        binding.fabAddExercicio.visibility = View.VISIBLE
        binding.btnApagarNotas.visibility = View.GONE
    }

    private fun toggleSelecao(nota: TreinoNota) {
        nota.isSelected = !nota.isSelected
        notaAdapter.notifyDataSetChanged()
        if (modoExclusaoAtivo && notaAdapter.getSelecionados().isEmpty()) {
            desativarModoExclusao()
        }
    }
}