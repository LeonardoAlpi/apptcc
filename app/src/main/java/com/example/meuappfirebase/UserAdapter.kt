package com.apol.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.meuappfirebase.R
import com.apol.myapplication.data.model.User

class UserAdapter(
    private var userList: List<User>,
    private val onDeleteClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    fun updateList(newList: List<User>) {
        userList = newList
        notifyDataSetChanged()
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emailTextView: TextView = itemView.findViewById(R.id.user_email_text)
        val deleteButton: ImageButton = itemView.findViewById(R.id.btn_delete_user)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.emailTextView.text = user.email ?: "E-mail não disponível"
        holder.deleteButton.setOnClickListener {
            onDeleteClick(user)
        }
    }

    override fun getItemCount() = userList.size
}