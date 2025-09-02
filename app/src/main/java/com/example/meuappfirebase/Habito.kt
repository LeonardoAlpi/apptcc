package com.apol.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habitos")
data class Habito(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    // --- CORREÇÃO PRINCIPAL AQUI ---
    // O campo que identifica o dono do hábito agora é o ID do Firebase.
    val userOwnerId: String,

    var nome: String,
    var isFavorito: Boolean = false,
    val isGoodHabit: Boolean
)