package com.apol.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.apol.myapplication.data.converters.ListConverters

@Entity(tableName = "users")
@TypeConverters(ListConverters::class) // Necessário para salvar listas no Room
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val userId: String, // Este é o UID do Firebase
    val nome: String,
    val idade: Int,
    val peso: Int,
    val altura: Float,
    val genero: String,
    val perguntaSecreta: String = "",
    val respostaSecreta: String = "",
    val profilePicUri: String? = null,

    // --- CAMPOS ADICIONADOS PARA O APP ---
    var temHabitoLeitura: Boolean? = null,
    var segueDieta: Boolean? = null,
    var gostariaSeguirDieta: Boolean? = null,
    var praticaAtividade: String? = null,
    var tempoDisponivel: String? = null,
    var espacosDisponiveis: List<String>? = null,
    var sugestoesInteresse: List<String>? = null
)
