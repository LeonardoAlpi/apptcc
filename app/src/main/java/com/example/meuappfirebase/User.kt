package com.apol.myapplication.data.model

import androidx.databinding.adapters.Converters
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
    var nome: String,
    var idade: Int,
    var peso: Int,
    var altura: Float,
    var genero: String,
    var perguntaSecreta: String = "",
    var respostaSecreta: String = "",
    var profilePicUri: String? = null,

    // --- CAMPOS ADICIONADOS PARA O APP ---
    var temHabitoLeitura: Boolean? = null,
    var segueDieta: Boolean? = null,
    var gostariaSeguirDieta: Boolean? = null,
    var praticaAtividade: String? = null,
    var tempoDisponivel: String? = null,

    // Adicione esta anotação se ela não estiver no nível da classe
    @TypeConverters(Converters::class)
    var espacosDisponiveis: List<String>? = null,

    @TypeConverters(Converters::class)
    var sugestoesInteresse: List<String>? = null,

    // --- LINHAS NOVAS A SEREM ADICIONADAS ---
    @TypeConverters(Converters::class)
    var habitosNegativos: List<String>? = null,

    @TypeConverters(Converters::class)
    var problemasEmocionais: List<String>? = null
)
