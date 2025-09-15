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
        val userEmail = user.email ?: "usuário sem e-mail"

        AlertDialog.Builder(this)
            .setTitle("Confirmar Exclusão")
            .setMessage("Tem certeza que deseja apagar o usuário $userEmail?")
            .setPositiveButton("Sim") { _, _ ->
                deleteUser(user)
            }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun deleteUser(user: User) {
        // CORREÇÃO: Usando 'let' para garantir que o userId não é nulo antes de usá-lo.
        user.userId?.let { nonNullUserId ->
            lifecycleScope.launch {
                // A sua classe UserDao precisa ter um método para deletar pelo ID do usuário
                db.userDao().deleteUserById(nonNullUserId)

                runOnUiThread {
                    Toast.makeText(this@AdminUsersActivity, "Usuário apagado.", Toast.LENGTH_SHORT).show()
                    loadUsers()
                }
            }
        } ?: run {
            // Caso o userId seja nulo, exibe uma mensagem de erro
            Toast.makeText(this, "Erro: ID do usuário é nulo. Não foi possível apagar.", Toast.LENGTH_SHORT).show()
        }
    }
}