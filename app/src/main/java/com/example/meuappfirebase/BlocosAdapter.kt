package com.apol.myapplication // Mantendo seu pacote original

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.apol.myapplication.data.model.Bloco
import com.example.meuappfirebase.R

class BlocosAdapter(
    private val onItemClick: (Bloco) -> Unit,
    private val onItemLongClick: (Bloco) -> Unit
) : RecyclerView.Adapter<BlocosAdapter.BlocoViewHolder>() {

    private var blocos: MutableList<Bloco> = mutableListOf()
    var modoExclusaoAtivo: Boolean = false

    fun submitList(novaLista: List<Bloco>) {
        blocos.clear()
        blocos.addAll(novaLista)
        notifyDataSetChanged()
    }

    fun limparSelecao() {
        blocos.forEach { it.isSelected = false }
        modoExclusaoAtivo = false
        notifyDataSetChanged()
    }

    fun getSelecionados(): List<Bloco> = blocos.filter { it.isSelected }

    // --- FUNÇÃO ADICIONADA AQUI ---
    /**
     * Alterna o estado de seleção de um bloco específico.
     * @param bloco O bloco que teve seu estado de seleção alterado.
     */
    fun toggleSelection(bloco: Bloco) {
        val blocoNaLista = blocos.find { it.id == bloco.id }
        blocoNaLista?.let {
            it.isSelected = !it.isSelected
            notifyItemChanged(blocos.indexOf(it))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlocoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bloco, parent, false)
        return BlocoViewHolder(view)
    }

    override fun onBindViewHolder(holder: BlocoViewHolder, position: Int) {
        holder.bind(blocos[position])
    }

    override fun getItemCount(): Int = blocos.size

    inner class BlocoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val texto = itemView.findViewById<TextView>(R.id.texto_bloco)
        private val container = itemView.findViewById<View>(R.id.item_bloco_container)

        fun bind(bloco: Bloco) {
            texto.text = if (bloco.subtitulo.isNotEmpty()) {
                "${bloco.nome} - ${bloco.subtitulo}"
            } else {
                bloco.nome
            }

            val background = if (bloco.isSelected) R.drawable.bg_selected_item else R.drawable.rounded_semi_transparent
            container.background = ContextCompat.getDrawable(itemView.context, background)

            itemView.setOnClickListener {
                if (modoExclusaoAtivo) {
                    toggleSelection(bloco) // Chama a função interna da classe
                } else {
                    onItemClick(bloco)
                }
            }

            itemView.setOnLongClickListener {
                if (!modoExclusaoAtivo) {
                    onItemLongClick(bloco)
                }
                true
            }
        }
    }
}