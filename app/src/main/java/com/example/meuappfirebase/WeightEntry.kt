package com.apol.myapplication.data.model // Mantenha seu pacote original

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weight_history")
data class WeightEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // --- CORREÇÃO PRINCIPAL AQUI ---
    // A coluna que liga ao usuário agora se chama 'userOwnerId'
    val userOwnerId: String,

    val weight: Float,
    val date: Long = System.currentTimeMillis()
)