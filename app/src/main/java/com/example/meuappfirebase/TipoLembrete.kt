
package com.apol.myapplication.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class TipoLembrete {
    NENHUM,
    DIARIO,
    MENSAL,
    SEGUNDOS_TESTE
}