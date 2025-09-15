package com.apol.myapplication // Mantendo seu pacote original

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
import com.apol.myapplication.data.model.Bloco
import com.example.meuappfirebase.R

class BlocosAdapter(
    private val onItemClick: (Bloco) -> Unit,
    private val onItemLongClick: (Bloco) -> Unit,
    private val onFavoriteClick: (Bloco) -> Unit
) : ListAdapter<Bloco, BlocosAdapter.BlocoViewHolder>(BlocoDiffCallback()) {

    // Estas propriedades controlam o estado de seleção da UI
    private var modoExclusaoAtivo: Boolean = false
    private val itensSelecionados = mutableSetOf<String>()

    fun getSelecionados(): List<Bloco> {
        return currentList.filter { itensSelecionados.contains(it.id) }
    }

    fun limparSelecao() {
        modoExclusaoAtivo = false
        itensSelecionados.clear()
        notifyDataSetChanged() // Necessário para redesenhar todos os itens
    }

    fun ativarModoExclusao() {
        modoExclusaoAtivo = true
    }

    fun toggleSelection(bloco: Bloco) {
        if (itensSelecionados.contains(bloco.id)) {
            itensSelecionados.remove(bloco.id)
        } else {
            itensSelecionados.add(bloco.id)
        }
        notifyItemChanged(currentList.indexOf(bloco))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlocoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bloco, parent, false)
        return BlocoViewHolder(view)
    }

    override fun onBindViewHolder(holder: BlocoViewHolder, position: Int) {
        val bloco = getItem(position)
        holder.bind(bloco)
    }

    inner class BlocoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // IDs corretos do novo item_bloco.xml
        private val iconeBloco: ImageView = itemView.findViewById(R.id.icone_bloco)
        private val textoCompleto: TextView = itemView.findViewById(R.id.bloco_texto_completo) // << MUDOU
        private val favoriteButton: ImageButton = itemView.findViewById(R.id.btn_favorite_bloco)

        fun bind(bloco: Bloco) {
            // 1. ADICIONA O ÍCONE
            // Substitua 'ic_block' pelo ícone que desejar
            iconeBloco.setImageResource(R.drawable.ic_block)

            // 2. JUNTA NOME E COMPLEMENTO (SUBTÍTULO)
            val textoFinal = if (bloco.subtitulo.isNotEmpty()) {
                "${bloco.nome} - ${bloco.subtitulo}"
            } else {
                bloco.nome
            }
            textoCompleto.text = textoFinal

            // O resto da sua lógica de bind continua igual...
            val favoriteIconRes = if (bloco.isFavorito) R.drawable.ic_star_outline else R.drawable.ic_star_filled
            favoriteButton.setImageResource(favoriteIconRes)

            val isSelected = itensSelecionados.contains(bloco.id)
            val backgroundRes = if (isSelected) R.drawable.bg_selected_item else R.drawable.rounded_semi_transparent
            itemView.background = ContextCompat.getDrawable(itemView.context, backgroundRes)

            favoriteButton.setOnClickListener { onFavoriteClick(bloco) }
            itemView.setOnClickListener {
                if (modoExclusaoAtivo) toggleSelection(bloco) else onItemClick(bloco)
            }
            itemView.setOnLongClickListener {
                if (!modoExclusaoAtivo) onItemLongClick(bloco)
                true
            }
        }
    }
}

/**
 * Classe auxiliar para o ListAdapter calcular as diferenças entre a lista antiga e a nova.
 * Isso torna as atualizações da lista muito mais eficientes.
 */
class BlocoDiffCallback : DiffUtil.ItemCallback<Bloco>() {
    override fun areItemsTheSame(oldItem: Bloco, newItem: Bloco): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Bloco, newItem: Bloco): Boolean {
        // Compara os campos que afetam a UI para ver se o item precisa ser redesenhado
        return oldItem == newItem
    }
}