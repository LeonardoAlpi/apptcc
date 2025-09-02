
package com.apol.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habito_progresso")
data class HabitoProgresso(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitoId: Long, // Para ligar ao hábito correspondente
    val data: String // Data no formato "yyyyMMdd"
)