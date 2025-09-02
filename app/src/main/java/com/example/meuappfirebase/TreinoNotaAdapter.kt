// Substitua o conteúdo COMPLETO do seu arquivo TreinoNotaAdapter.kt
package com.apol.myapplication.data.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.meuappfirebase.R

class TreinoNotaAdapter(
    private var notas: MutableList<TreinoNota>,
    private val onItemClick: (TreinoNota) -> Unit,
    private val onItemLongClick: (TreinoNota) -> Unit
) : RecyclerView.Adapter<TreinoNotaAdapter.NotaViewHolder>() {

    var modoExclusaoAtivo: Boolean = false

    fun submitList(newNotas: List<TreinoNota>) {
        notas.clear()
        notas.addAll(newNotas)
        notifyDataSetChanged()
    }

    fun getSelecionados(): List<TreinoNota> = notas.filter { it.isSelected }

    fun limparSelecao() {
        notas.forEach { it.isSelected = false }
        modoExclusaoAtivo = false
        notifyDataSetChanged()
    }

    class NotaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: LinearLayout = itemView.findViewById(R.id.item_nota_container)
        val titulo: TextView = itemView.findViewById(R.id.titulo_nota)
        val conteudo: TextView = itemView.findViewById(R.id.conteudo_nota_snippet)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_treino_nota, parent, false)
        return NotaViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotaViewHolder, position: Int) {
        val nota = notas[position]
        holder.titulo.text = nota.titulo
        if (nota.conteudo.isNotBlank()) {
            holder.conteudo.visibility = View.VISIBLE
            holder.conteudo.text = nota.conteudo
        } else {
            holder.conteudo.visibility = View.GONE
        }

        val background = if (nota.isSelected) R.drawable.bg_selected_item else R.drawable.rounded_semi_transparent
        holder.container.background = ContextCompat.getDrawable(holder.itemView.context, background)

        holder.itemView.setOnClickListener { onItemClick(nota) }
        holder.itemView.setOnLongClickListener {
            onItemLongClick(nota)
            true
        }
    }

    override fun getItemCount() = notas.size
}