package com.example.meuappfirebase

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

// ATUALIZADO: O construtor agora aceita as funções auxiliares da Activity
class HabitsAdapter(
    private val onItemClick: (HabitUI) -> Unit,
    private val onItemLongClick: (HabitUI) -> Unit,
    private val onMarkDone: (HabitUI) -> Unit,
    private val onUndoDone: (HabitUI) -> Unit,
    private val onToggleFavorite: (HabitUI) -> Unit,
    private val emojiExtractor: (String) -> String,
    private val emojiRemover: (String) -> String,
    private val textDrawableFactory: (Context, String) -> Drawable
) : ListAdapter<HabitUI, HabitsAdapter.HabitViewHolder>(HabitUIDiffCallback()) {

    private var modoExclusaoAtivo: Boolean = false
    // Mantém o controle dos itens selecionados internamente
    private val selecionados = mutableSetOf<HabitUI>()

    fun setModoExclusao(ativo: Boolean) {
        modoExclusaoAtivo = ativo
        notifyDataSetChanged() // Necessário para atualizar a UI de todos os itens ao mudar de modo
    }

    fun toggleSelecao(habit: HabitUI) {
        if (selecionados.contains(habit)) {
            selecionados.remove(habit)
            habit.isSelected = false
        } else {
            selecionados.add(habit)
            habit.isSelected = true
        }
        // Notifica apenas o item que mudou, de forma eficiente
        notifyItemChanged(currentList.indexOf(habit))
    }

    fun getSelecionados(): List<HabitUI> = selecionados.toList()

    fun limparSelecao() {
        // Desmarca a seleção em todos os itens da lista atual antes de limpar
        currentList.forEach { it.isSelected = false }
        selecionados.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.habit_item, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = getItem(position)
        holder.bind(habit, this)
    }

    inner class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icone: ImageView = itemView.findViewById(R.id.icone_habito)
        private val nome: TextView = itemView.findViewById(R.id.habit_name)
        private val streakDays: TextView = itemView.findViewById(R.id.text_streak_days)
        private val message: TextView = itemView.findViewById(R.id.text_streak_message)
        private val btnFavorite: ImageButton = itemView.findViewById(R.id.btn_favorite)
        private val btnCheck: ImageButton = itemView.findViewById(R.id.btn_check)
        private val btnCheckDone: ImageButton = itemView.findViewById(R.id.btn_check_done)

        fun bind(habit: HabitUI, adapter: HabitsAdapter) {
            val context = itemView.context

            // ATUALIZADO: Usa as funções passadas pelo construtor
            val nomeCompleto = habit.name
            val emoji = adapter.emojiExtractor(nomeCompleto)
            val nomeSemEmoji = adapter.emojiRemover(nomeCompleto)

            nome.text = nomeSemEmoji

            if (emoji.isNotEmpty()) {
                icone.setImageDrawable(adapter.textDrawableFactory(context, emoji))
            } else {
                icone.setImageResource(R.drawable.ic_habits) // Imagem padrão
            }

            streakDays.text = "${habit.streakDays} dias seguidos"
            message.text = habit.message

            if (habit.count > 0) {
                btnCheck.visibility = View.GONE
                btnCheckDone.visibility = View.VISIBLE
            } else {
                btnCheck.visibility = View.VISIBLE
                btnCheckDone.visibility = View.GONE
            }

            btnFavorite.setImageResource(
                if (habit.isFavorited) R.drawable.ic_star_filled else R.drawable.ic_star_outline
            )

            val background = if (habit.isSelected) R.drawable.bg_selected_item else R.drawable.rounded_semi_transparent
            itemView.background = ContextCompat.getDrawable(context, background)

            // Listeners
            itemView.setOnClickListener { adapter.onItemClick(habit) }
            itemView.setOnLongClickListener { adapter.onItemLongClick(habit); true }
            btnFavorite.setOnClickListener { adapter.onToggleFavorite(habit) }
            btnCheck.setOnClickListener { adapter.onMarkDone(habit) }
            btnCheckDone.setOnClickListener { adapter.onUndoDone(habit) }
        }
    }
}

class HabitUIDiffCallback : DiffUtil.ItemCallback<HabitUI>() {
    override fun areItemsTheSame(oldItem: HabitUI, newItem: HabitUI): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: HabitUI, newItem: HabitUI): Boolean {
        return oldItem == newItem
    }
}