// Arquivo: Habito.kt
package com.apol.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude

@Entity(tableName = "habitos")
data class Habito(
    @PrimaryKey(autoGenerate = true)
    @get:Exclude
    var id: Long = 0,
    var firestoreId: String = "",
    var userOwnerId: String = "",
    var nome: String = "",
    var isFavorito: Boolean = false,
    var isGoodHabit: Boolean = true,
    var diasProgramados: List<String> = emptyList()
)