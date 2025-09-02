package com.apol.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.apol.myapplication.Converters
import kotlinx.serialization.Serializable

@Entity(tableName = "blocos")
@Serializable
@TypeConverters(Converters::class) // <- Adiciona aqui para que o Room use os conversores
data class Bloco(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),

    val userOwnerId: String,

    var nome: String,
    var subtitulo: String = "",
    var anotacao: String = "",
    var mensagemNotificacao: String = "",
    var tipoLembrete: TipoLembrete = TipoLembrete.NENHUM,

    var diasLembrete: List<Int> = emptyList(),   // Room vai usar o conversor
    var horariosLembrete: List<String> = emptyList(), // Room vai usar o conversor

    var segundosLembrete: Long? = null,
    var isSelected: Boolean = false
)
