
package com.apol.myapplication.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(
    tableName = "treino_notas",
    foreignKeys = [ForeignKey(
        entity = DivisaoTreino::class,
        parentColumns = ["id"],
        childColumns = ["divisaoId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class TreinoNota(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var userOwnerId: String,
    val divisaoId: Long, // Para saber a qual dia/divisão a nota pertence
    var titulo: String,
    var conteudo: String = "" // O texto livre que o usuário vai escrever
){
    @Ignore
    var isSelected: Boolean = false
}