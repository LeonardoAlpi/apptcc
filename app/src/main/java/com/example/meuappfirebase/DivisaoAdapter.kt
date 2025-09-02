
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
import com.apol.myapplication.data.model.DivisaoTreino

class DivisaoAdapter(
    private var listaDivisoes: MutableList<DivisaoTreino>,
    private val onItemClick: (DivisaoTreino) -> Unit,
    private val onItemLongClick: (DivisaoTreino) -> Unit,
    private val onEditClick: (DivisaoTreino) -> Unit
) : RecyclerView.Adapter<DivisaoAdapter.DivisaoViewHolder>() {

    var modoExclusaoAtivo: Boolean = false

    fun getSelecionados(): List<DivisaoTreino> = listaDivisoes.filter { it.isSelected }

    fun limparSelecao() {
        listaDivisoes.forEach { it.isSelected = false }
        modoExclusaoAtivo = false
        notifyDataSetChanged()
    }

    fun submitList(novaLista: List<DivisaoTreino>) {
        listaDivisoes.clear()
        listaDivisoes.addAll(novaLista)
        notifyDataSetChanged()
    }

    class DivisaoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: LinearLayout = itemView.findViewById(R.id.item_divisao_container)
        val nome: TextView = itemView.findViewById(R.id.nome_divisao)
        val btnEditar: ImageView = itemView.findViewById(R.id.btn_editar_nome_divisao)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DivisaoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_divisao_treino, parent, false)
        return DivisaoViewHolder(view)
    }

    override fun onBindViewHolder(holder: DivisaoViewHolder, position: Int) {
        val divisaoAtual = listaDivisoes[position]
        holder.nome.text = divisaoAtual.nome
        holder.btnEditar.visibility = if (modoExclusaoAtivo) View.GONE else View.VISIBLE
        val background = if (divisaoAtual.isSelected) R.drawable.bg_selected_item else R.drawable.rounded_semi_transparent
        holder.container.background = ContextCompat.getDrawable(holder.itemView.context, background)
        holder.itemView.setOnClickListener { onItemClick(divisaoAtual) }
        holder.itemView.setOnLongClickListener { onItemLongClick(divisaoAtual); true }
        holder.btnEditar.setOnClickListener { onEditClick(divisaoAtual) }
    }

    override fun getItemCount() = listaDivisoes.size
}