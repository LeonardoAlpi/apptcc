
package com.apol.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.meuappfirebase.R
import com.apol.myapplication.data.model.TreinoEntity

class TreinosAdapter(
    private var listaTreinos: MutableList<TreinoEntity>,
    private val onItemClick: (TreinoEntity) -> Unit,
    private val onItemLongClick: (TreinoEntity) -> Unit
) : RecyclerView.Adapter<TreinosAdapter.TreinoViewHolder>() {

    var modoExclusaoAtivo: Boolean = false

    fun getSelecionados(): List<TreinoEntity> = listaTreinos.filter { it.isSelected }

    fun limparSelecao() {
        listaTreinos.forEach { it.isSelected = false }
        modoExclusaoAtivo = false
        notifyDataSetChanged()
    }

    fun submitList(novaLista: List<TreinoEntity>) {
        listaTreinos.clear()
        listaTreinos.addAll(novaLista)
        notifyDataSetChanged()
    }

    class TreinoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: LinearLayout = itemView.findViewById(R.id.item_treino_container)
        val icone: ImageView = itemView.findViewById(R.id.icone_treino)
        val nome: TextView = itemView.findViewById(R.id.nome_treino)
        val detalhes: TextView = itemView.findViewById(R.id.detalhes_treino)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TreinoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_treino, parent, false)
        return TreinoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TreinoViewHolder, position: Int) {
        val treinoAtual = listaTreinos[position]

        holder.icone.setImageResource(treinoAtual.iconeResId)
        holder.nome.text = treinoAtual.nome
        holder.detalhes.text = treinoAtual.detalhes

        val background = if (treinoAtual.isSelected) R.drawable.bg_selected_item
        else R.drawable.rounded_semi_transparent
        holder.container.background = ContextCompat.getDrawable(holder.itemView.context, background)

        holder.itemView.setOnClickListener { onItemClick(treinoAtual) }
        holder.itemView.setOnLongClickListener {
            onItemLongClick(treinoAtual)
            true
        }
    }

    override fun getItemCount() = listaTreinos.size
}