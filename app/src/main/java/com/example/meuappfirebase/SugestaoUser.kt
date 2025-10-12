package com.example.meuappfirebase

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.meuappfirebase.databinding.ActivitySugestaoUserBinding
import com.example.meuappfirebase.databinding.CardSugestaoBinding

class SugestaoUser : AppCompatActivity() {

    private lateinit var binding: ActivitySugestaoUserBinding
    private val viewModel: SuggestionsViewModel by viewModels()

    private lateinit var cardViews: Map<String, View>
    private var currentCardStates: List<SuggestionCardState> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySugestaoUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cardViews = mapOf(
            "livros" to binding.cardLivros.root,
            "dietas" to binding.cardDietas.root,
            "meditacao" to binding.cardMeditacao.root,
            "respiracao" to binding.cardRespiracao.root,
            "podcasts" to binding.cardPodcasts.root,
            "exercicios" to binding.cardExerciciosMentais.root
        )

        configurarNavBar()
        observarViewModel()

        binding.fabAddSugestao.setOnClickListener {
            exibirDialogoAdicionarSugestao()
        }
    }

    override fun onResume() {
        super.onResume()
        // Chama a busca de sugestões (agora via Cloud Function)
        viewModel.buscarSugestoesDaIA()
    }

    override fun onBackPressed() {
        finishAffinity()
    }

    private fun observarViewModel() {
        // Observa a lista de cards para exibir na tela
        viewModel.suggestionCards.observe(this) { cardStates ->
            if (cardStates == null) return@observe
            currentCardStates = cardStates
            atualizarTodosOsCards()
        }

        // Observa mensagens de status para mostrar Toasts
        viewModel.statusMessage.observe(this) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }

        // Observa o estado de carregamento para mostrar/esconder o ProgressBar
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBarSugestoes.visibility = if (isLoading) View.VISIBLE else View.GONE
            // Esconde os cards enquanto carrega para evitar que o usuário veja conteúdo antigo
            binding.scrollViewCards.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
        }
    }

    private fun atualizarTodosOsCards() {
        // Esconde todos os cards primeiro para garantir um estado limpo
        cardViews.values.forEach { it.visibility = View.GONE }

        currentCardStates.forEach { state ->
            val cardView = cardViews[state.key]
            cardView?.let {
                it.visibility = if (state.isVisible) View.VISIBLE else View.GONE
                if (state.isVisible) {
                    val cardBinding = CardSugestaoBinding.bind(it)
                    cardBinding.iconeSugestao.setImageResource(state.iconResId)
                    cardBinding.tituloCardSugestao.text = state.title
                    cardBinding.textoSugestao.text = state.suggestionTitle

                    if (state.isCompleted) {
                        it.alpha = 0.6f
                        cardBinding.btnConcluirSugestao.visibility = View.GONE
                        cardBinding.textoSugestao.setOnClickListener {
                            Toast.makeText(this, "Você já concluiu esta sugestão hoje!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        it.alpha = 1.0f
                        cardBinding.btnConcluirSugestao.visibility = View.VISIBLE
                        cardBinding.btnProximaSugestao.visibility = View.GONE

                        cardBinding.textoSugestao.setOnClickListener {
                            AlertDialog.Builder(this)
                                .setTitle(state.suggestionTitle)
                                .setMessage(state.suggestionDescription)
                                .setPositiveButton("OK", null)
                                .show()
                        }
                        cardBinding.btnConcluirSugestao.setOnClickListener { viewModel.markSuggestionAsDone(state.suggestionTitle) }
                    }
                }
            }
        }
    }

    private fun exibirDialogoAdicionarSugestao() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_adicionar_sugestao, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val checkLivros = dialogView.findViewById<CheckBox>(R.id.check_add_livros)
        val checkDietas = dialogView.findViewById<CheckBox>(R.id.check_add_dietas)
        val checkMeditacao = dialogView.findViewById<CheckBox>(R.id.check_add_meditacao)
        val checkRespiracao = dialogView.findViewById<CheckBox>(R.id.check_add_respiracao)
        val checkPodcasts = dialogView.findViewById<CheckBox>(R.id.check_add_podcasts)
        val checkExercicios = dialogView.findViewById<CheckBox>(R.id.check_add_exercicios)
        val btnConfirmar = dialogView.findViewById<Button>(R.id.btn_confirmar_add_sugestao)

        val allCheckBoxes = mapOf(
            "Sugestões de Livros" to checkLivros,
            "Dicas de Dieta" to checkDietas,
            "Meditação" to checkMeditacao,
            "Respiração Guiada" to checkRespiracao,
            "Podcasts Relaxantes" to checkPodcasts,
            "Exercícios Mentais" to checkExercicios
        )

        val currentInterests = currentCardStates.filter { it.isVisible }.map { it.title }.toSet()
        allCheckBoxes.forEach { (interest, checkbox) ->
            checkbox.isChecked = currentInterests.contains(interest)
        }

        btnConfirmar.setOnClickListener {
            val newInterests = allCheckBoxes.filter { it.value.isChecked }.map { it.key }
            viewModel.updateVisibleSuggestions(newInterests)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun configurarNavBar() {
        binding.navigationBar.botaoInicio.setOnClickListener {
            startActivity(Intent(this, Bemvindouser::class.java))
        }
        binding.navigationBar.botaoAnotacoes.setOnClickListener {
            startActivity(Intent(this, anotacoes::class.java))
        }
        binding.navigationBar.botaoHabitos.setOnClickListener {
            startActivity(Intent(this, HabitosActivity::class.java))
        }
        binding.navigationBar.botaoTreinos.setOnClickListener {
            startActivity(Intent(this, treinos::class.java))
        }
        binding.navigationBar.botaoCronometro.setOnClickListener {
            startActivity(Intent(this, CronometroActivity::class.java))
        }
        binding.navigationBar.botaoSugestoes.setOnClickListener {
            // Já está aqui
        }
    }
}
