
package com.apol.myapplication

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.apol.myapplication.data.model.User
import com.example.meuappfirebase.R
import kotlinx.coroutines.launch

class AdminUsersActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var userAdapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_users)

        db = AppDatabase.getDatabase(this)
        val btnVoltar = findViewById<ImageButton>(R.id.btn_voltar_admin)
        btnVoltar.setOnClickListener {
            finish()
        }

        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        loadUsers()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.users_recyclerview)
        userAdapter = UserAdapter(emptyList()) { user ->
            // Ação do clique no botão de deletar
            showDeleteConfirmationDialog(user)
        }
        recyclerView.adapter = userAdapter
    }

    private fun loadUsers() {
        lifecycleScope.launch {
            val userList = db.userDao().getAllUsers()
            runOnUiThread {
                userAdapter.updateList(userList)
            }
        }
    }

    private fun showDeleteConfirmationDialog(user: User) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Exclusão")
            .setMessage("Tem certeza que deseja apagar o usuário ${user.email}?")
            .setPositiveButton("Sim") { _, _ ->
                deleteUser(user)
            }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun deleteUser(user: User) {
        lifecycleScope.launch {
            db.userDao().deleteUserById(user.userId)
            // Recarrega a lista após a exclusão
            runOnUiThread {
                Toast.makeText(this@AdminUsersActivity, "Usuário apagado.", Toast.LENGTH_SHORT).show()
                loadUsers()
            }
        }
    }
}