
package com.apol.myapplication.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "log_entries",
    foreignKeys = [ForeignKey(
        entity = DivisaoTreino::class,
        parentColumns = ["id"],
        childColumns = ["divisaoId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class LogEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val divisaoId: Long,

    // Campos genéricos que servirão para todos os tipos
    var campo1: String = "", // Para Academia: Nome do Exercício | Para Corrida: Distância (km)
    var campo2: String = "", // Para Academia: Carga | Para Corrida: Tempo (hh:mm:ss)
    var campo3: String = "", // Para Academia: Séries | Para Corrida: Ritmo (min/km)
    var campo4: String = ""  // Para Academia: Repetições | Para Corrida/Esportes: Observações
)