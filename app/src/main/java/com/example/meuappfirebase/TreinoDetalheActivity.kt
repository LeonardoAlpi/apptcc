package com.example.meuappfirebase

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.apol.myapplication.DivisaoAdapter // <-- IMPORT ADICIONADO AQUI
import com.example.meuappfirebase.R
import com.apol.myapplication.data.model.DivisaoTreino
import com.apol.myapplication.data.model.TipoDivisao
import com.apol.myapplication.data.model.TreinoEntity
import com.example.meuappfirebase.databinding.ActivityTreinoDetalheBinding

class TreinoDetalheActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTreinoDetalheBinding
    private val viewModel: WorkoutDetailViewModel by viewModels()
    private lateinit var divisaoAdapter: DivisaoAdapter
    private var treinoId: Long = -1L
    private var modoExclusaoAtivo = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTreinoDetalheBinding.inflate(layoutInflater)
        setContentView(binding.root)

        treinoId = intent.getLongExtra("TREINO_ID", -1L)
        if (treinoId == -1L) { finish(); return }

        binding.nomeTreinoDetalhe.text = intent.getStringExtra("TREINO_NOME") ?: "Detalhes"

        setupRecyclerView()
        setupListeners()
        observarViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadInitialData(treinoId)
    }

    override fun onBackPressed() {
        if (modoExclusaoAtivo) {
            desativarModoExclusao()
        } else {
            super.onBackPressed()
        }
    }

    private fun observarViewModel() {
        viewModel.workout.observe(this) { treino ->
            if (treino != null) {
                if (treino.tipoDivisao == TipoDivisao.NAO_DEFINIDO) {
                    exibirDialogoEscolhaDeDivisao(treino)
                }
                binding.fabAddDivisao.visibility = if (treino.tipoDivisao == TipoDivisao.LETRAS) View.VISIBLE else View.GONE
            }
        }

        viewModel.divisions.observe(this) { divisoes ->
            divisaoAdapter.submitList(divisoes)
        }
    }

    private fun setupRecyclerView() {
        divisaoAdapter = DivisaoAdapter(
            mutableListOf(),
            onItemClick = { divisao ->
                if (modoExclusaoAtivo) {
                    toggleSelecao(divisao)
                } else {
                    val intent = Intent(this, DivisaoDetalheActivity::class.java).apply {
                        putExtra("DIVISAO_ID", divisao.id)
                        putExtra("DIVISAO_NOME", divisao.nome)
                    }
                    startActivity(intent)
                }
            },
            onItemLongClick = { divisao ->
                if (!modoExclusaoAtivo && viewModel.workout.value?.tipoDivisao == TipoDivisao.LETRAS) {
                    ativarModoExclusao(divisao)
                }
            },
            onEditClick = { divisao ->
                if (!modoExclusaoAtivo) {
                    exibirDialogoRenomearDivisao(divisao)
                }
            }
        )
        binding.recyclerViewDivisoes.adapter = divisaoAdapter
        binding.recyclerViewDivisoes.layoutManager = LinearLayoutManager(this)
    }

    private fun setupListeners() {
        binding.btnVoltarDetalhe.setOnClickListener { finish() }
        binding.fabAddDivisao.setOnClickListener { viewModel.addLetterDivision() }
        binding.btnApagarDivisoes.setOnClickListener {
            val selecionados = divisaoAdapter.getSelecionados()
            if (selecionados.isNotEmpty()) {
                confirmarExclusao(selecionados)
            }
        }
    }

    private fun confirmarExclusao(divisoesParaApagar: List<DivisaoTreino>) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Divisões")
            .setMessage("Tem certeza que deseja apagar ${divisoesParaApagar.size} divisão(ões)?")
            .setPositiveButton("Excluir") { _, _ ->
                viewModel.deleteDivisions(divisoesParaApagar)
                desativarModoExclusao()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun exibirDialogoEscolhaDeDivisao(treino: TreinoEntity) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_escolher_divisao, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)

        val clickListener = View.OnClickListener { view ->
            val tipoEscolhido = when(view.id) {
                R.id.btn_dias_da_semana -> TipoDivisao.DIAS_DA_SEMANA
                R.id.btn_letras -> TipoDivisao.LETRAS
                else -> null
            }
            tipoEscolhido?.let { viewModel.setWorkoutType(treino, it) }
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btn_dias_da_semana).setOnClickListener(clickListener)
        dialogView.findViewById<Button>(R.id.btn_letras).setOnClickListener(clickListener)
        dialog.setOnDismissListener {
            if (viewModel.workout.value?.tipoDivisao == TipoDivisao.NAO_DEFINIDO) {
                finish()
            }
        }
        dialogView.findViewById<TextView>(R.id.btn_cancelar).setOnClickListener {
            dialog.dismiss() // Apenas fecha o diálogo
        }
        dialog.show()
    }

    private fun exibirDialogoRenomearDivisao(divisao: DivisaoTreino) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_renomear, null)
        val dialog = AlertDialog.Builder(this, R.style.Theme_HAM_Dialog_Transparent).setView(dialogView).create()

        val editText = dialogView.findViewById<EditText>(R.id.edit_text_rename)
        val btnSalvar = dialogView.findViewById<Button>(R.id.btn_salvar_rename)

        editText.setText(divisao.nome)

        btnSalvar.setOnClickListener {
            val novoNome = editText.text.toString().trim()
            if (novoNome.isNotEmpty()) {
                viewModel.renameDivision(divisao, novoNome)
                dialog.dismiss()
            }
        }

        dialogView.findViewById<Button>(R.id.btn_cancelar_rename).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun ativarModoExclusao(primeiraDivisao: DivisaoTreino) {
        modoExclusaoAtivo = true
        divisaoAdapter.modoExclusaoAtivo = true
        binding.fabAddDivisao.visibility = View.GONE
        binding.btnApagarDivisoes.visibility = View.VISIBLE
        toggleSelecao(primeiraDivisao)
    }

    private fun desativarModoExclusao() {
        modoExclusaoAtivo = false
        divisaoAdapter.limparSelecao()
        if (viewModel.workout.value?.tipoDivisao == TipoDivisao.LETRAS) {
            binding.fabAddDivisao.visibility = View.VISIBLE
        }
        binding.btnApagarDivisoes.visibility = View.GONE
    }

    private fun toggleSelecao(divisao: DivisaoTreino) {
        divisao.isSelected = !divisao.isSelected
        divisaoAdapter.notifyDataSetChanged()
        if (modoExclusaoAtivo && divisaoAdapter.getSelecionados().isEmpty()) {
            desativarModoExclusao()
        }
    }
}