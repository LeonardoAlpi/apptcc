
package com.apol.myapplication.data.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

enum class TipoTreino {
    ACADEMIA,
    CORRIDA,
    ESPORTES,
    GENERICO // Para o "Outro"
}

// O enum permanece o mesmo
enum class TipoDivisao {
    NAO_DEFINIDO,
    DIAS_DA_SEMANA,
    LETRAS
}


@Entity(tableName = "treinos")
data class TreinoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // --- CORREÇÃO AQUI ---
    var userOwnerId: String, // Mudamos de userOwnerEmail para userOwnerId

    val nome: String,
    val iconeResId: Int,
    val tipoDivisao: TipoDivisao = TipoDivisao.NAO_DEFINIDO,
    val detalhes: String = "Toque para adicionar detalhes",
    val tipoDeTreino: TipoTreino,
    var templateJson: String? = null
) {
    @Ignore
    var isSelected: Boolean = false
}