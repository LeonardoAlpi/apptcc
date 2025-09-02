package com.example.meuappfirebase

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.apol.myapplication.TreinosAdapter
import com.apol.myapplication.data.model.TipoTreino
import com.apol.myapplication.data.model.TreinoEntity
import com.example.meuappfirebase.databinding.ActivityTreinosBinding

class treinos : AppCompatActivity() {

    private lateinit var binding: ActivityTreinosBinding
    private val viewModel: WorkoutsViewModel by viewModels()
    private lateinit var treinosAdapter: TreinosAdapter
    private var modoExclusaoAtivo = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTreinosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigationBar()
        setupRecyclerView()
        setupListeners()
        observarViewModel()

        // Pede ao ViewModel para verificar e criar o treino sugerido.
        viewModel.checkAndCreateSuggestedWorkout()
    }

    override fun onResume() {
        super.onResume()
        if (modoExclusaoAtivo) desativarModoExclusao()
        // Sempre que a tela é exibida, carrega os treinos.
        viewModel.loadWorkouts()
    }

    override fun onBackPressed() {
        if (modoExclusaoAtivo) {
            desativarModoExclusao()
        } else {
            finishAffinity() // Mantém sua lógica de fechar o app
        }
    }

    private fun observarViewModel() {
        viewModel.workouts.observe(this) { treinos ->
            treinosAdapter.submitList(treinos)
        }
        viewModel.operationStatus.observe(this) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        treinosAdapter = TreinosAdapter(
            mutableListOf(),
            onItemClick = { treino ->
                if (modoExclusaoAtivo) {
                    toggleSelecao(treino)
                } else {
                    val intent = Intent(this, TreinoDetalheActivity::class.java).apply {
                        putExtra("TREINO_ID", treino.id)
                        putExtra("TREINO_NOME", treino.nome)
                    }
                    startActivity(intent)
                }
            },
            onItemLongClick = { treino ->
                if (!modoExclusaoAtivo) ativarModoExclusao(treino)
            }
        )
        binding.recyclerViewTreinos.adapter = treinosAdapter
        binding.recyclerViewTreinos.layoutManager = LinearLayoutManager(this)
    }

    private fun setupListeners() {
        binding.fabAddTreino.setOnClickListener {
            if (!modoExclusaoAtivo) exibirDialogoAdicionarTreino()
        }
        binding.btnApagarTreinos.setOnClickListener {
            val selecionados = treinosAdapter.getSelecionados()
            if (selecionados.isNotEmpty()) confirmarExclusao(selecionados)
        }
        binding.clickOutsideView.setOnClickListener {
            if (modoExclusaoAtivo) desativarModoExclusao()
        }
    }

    private fun confirmarExclusao(treinosParaApagar: List<TreinoEntity>) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Treinos")
            .setMessage("Tem certeza que deseja apagar ${treinosParaApagar.size} treino(s)?")
            .setPositiveButton("Excluir") { _, _ ->
                viewModel.deleteWorkouts(treinosParaApagar)
                desativarModoExclusao()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun exibirDialogoAdicionarTreino() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_adicionar_treino, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<Button>(R.id.btn_academia).setOnClickListener {
            viewModel.addWorkout("Academia", R.drawable.ic_academia, TipoTreino.ACADEMIA); dialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.btn_corrida).setOnClickListener {
            viewModel.addWorkout("Corrida", R.drawable.ic_corrida, TipoTreino.CORRIDA); dialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.btn_esportes).setOnClickListener {
            viewModel.addWorkout("Esportes", R.drawable.ic_esportes, TipoTreino.ESPORTES); dialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.btn_personalizado).setOnClickListener {
            exibirDialogoPersonalizado(); dialog.dismiss()
        }
        dialog.show()
    }

    private fun exibirDialogoPersonalizado() {
        val editText = EditText(this).apply { hint = "Nome do treino" }
        AlertDialog.Builder(this).setTitle("Treino Personalizado").setView(editText)
            .setPositiveButton("Adicionar") { _, _ ->
                val nomeTreino = editText.text.toString().trim()
                if (nomeTreino.isNotEmpty()) {
                    viewModel.addWorkout(nomeTreino, R.drawable.ic_personalizado, TipoTreino.GENERICO)
                }
            }.setNegativeButton("Cancelar", null).show()
    }

    private fun ativarModoExclusao(primeiroItem: TreinoEntity) {
        modoExclusaoAtivo = true
        treinosAdapter.modoExclusaoAtivo = true
        binding.fabAddTreino.visibility = View.GONE
        binding.btnApagarTreinos.visibility = View.VISIBLE
        binding.clickOutsideView.visibility = View.VISIBLE
        toggleSelecao(primeiroItem)
    }

    private fun desativarModoExclusao() {
        modoExclusaoAtivo = false
        treinosAdapter.limparSelecao()
        binding.fabAddTreino.visibility = View.VISIBLE
        binding.btnApagarTreinos.visibility = View.GONE
        binding.clickOutsideView.visibility = View.GONE
    }

    private fun toggleSelecao(treino: TreinoEntity) {
        treino.isSelected = !treino.isSelected
        treinosAdapter.notifyDataSetChanged()
        if (modoExclusaoAtivo && treinosAdapter.getSelecionados().isEmpty()) {
            desativarModoExclusao()
        }
    }

    private fun setupNavigationBar() {
        binding.navigationBar.botaoInicio.setOnClickListener {
            startActivity(Intent(this, Bemvindouser::class.java))
        }
        binding.navigationBar.botaoAnotacoes.setOnClickListener {
            startActivity(Intent(this, anotacoes::class.java))
        }
        binding.navigationBar.botaoHabitos.setOnClickListener {
            startActivity(Intent(this, HabitosActivity::class.java))
        }
        binding.navigationBar.botaoTreinos.setOnClickListener { /* Já está aqui */ }
        binding.navigationBar.botaoCronometro.setOnClickListener {
            startActivity(Intent(this, CronometroActivity::class.java))
        }
        binding.navigationBar.botaoSugestoes.setOnClickListener {
            startActivity(Intent(this, SugestaoUser::class.java))
        }
    }
}