package com.apol.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // --- CORREÇÃO AQUI ---
    val userOwnerId: String, // Mudamos de userOwnerEmail para userOwnerId

    var text: String,
    val lastModified: Long = System.currentTimeMillis(),
    var isSelected: Boolean = false
)