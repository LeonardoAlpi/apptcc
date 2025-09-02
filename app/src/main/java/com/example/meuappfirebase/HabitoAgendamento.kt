package com.apol.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habito_agendamentos")
data class HabitoAgendamento(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitoId: Long,
    val diasProgramados: String, // Ex: "MON,TUE,WED"
    val dataDeInicio: String // Data em que este agendamento começou a valer (formato "yyyyMMdd")
)