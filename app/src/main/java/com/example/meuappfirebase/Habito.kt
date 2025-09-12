package com.apol.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude

@Entity(tableName = "habitos")
data class Habito(
    @PrimaryKey(autoGenerate = true)
    @get:Exclude // 2. DIZ AO FIRESTORE PARA IGNORAR ESTE CAMPO
    var id: Long = 0,

    // 1. ADICIONADO VALORES PADRÃO A TODOS OS CAMPOS
    var firestoreId: String = "",
    var userOwnerId: String = "",
    var nome: String = "",
    var isFavorito: Boolean = false,
    var isGoodHabit: Boolean = true
)