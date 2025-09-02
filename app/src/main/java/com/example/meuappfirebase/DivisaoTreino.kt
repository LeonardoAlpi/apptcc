
package com.apol.myapplication.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Ignore


@Entity(
    tableName = "divisoes_treino",
    foreignKeys = [ForeignKey(
        entity = TreinoEntity::class,
        parentColumns = ["id"],
        childColumns = ["treinoId"],
        onDelete = ForeignKey.CASCADE // Se apagar o treino, apaga as divisões dele
    )]
)
data class DivisaoTreino(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var userOwnerId: String,
    val treinoId: Long, // Chave estrangeira para ligar à TreinoEntity
    var nome: String, // "Segunda-feira", "A", ou um nome personalizado como "Peito e Tríceps"
    val ordem: Int // Para manter a ordem (0 para Domingo, 1 para Segunda... ou 0 para A, 1 para B...)
){
    @Ignore
    var isSelected: Boolean = false
}