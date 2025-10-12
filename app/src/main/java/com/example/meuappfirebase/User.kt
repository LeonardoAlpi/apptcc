package com.apol.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.apol.myapplication.data.converters.ListConverters
import java.io.Serializable

@Entity(tableName = "users")
@TypeConverters(ListConverters::class)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val email: String? = null,
    val userId: String? = null, // UID do Firebase
    var nome: String? = null,
    var idade: Int? = null,
    var peso: Float? = null,
    var altura: Float? = null,
    var genero: String? = null,
    var perguntaSecreta: String? = null,
    var respostaSecreta: String? = null,
    var profilePicUri: String? = null,

    // --- CAMPOS ADICIONADOS PARA O APP ---
    var temHabitoLeitura: Boolean? = null,
    var segueDieta: Boolean? = null,
    var gostariaSeguirDieta: Boolean? = null,
    var praticaAtividade: String? = null,
    var tempoDisponivel: String? = null,

    var espacosDisponiveis: List<String>? = null,
    var sugestoesInteresse: List<String>? = null,

    var habitosNegativos: List<String>? = null,
    var problemasEmocionais: List<String>? = null,

    // --- CAMPO ADICIONADO PARA CONTROLAR O ROTEAMENTO ---
    var onboardingStep: Int = 1 // Valor inicial é 1 (primeira etapa do questionário)

) : Serializable
