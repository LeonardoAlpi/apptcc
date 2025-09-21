package com.apol.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.apol.myapplication.Converters
import java.util.*

@Entity(tableName = "blocos")
@TypeConverters(Converters::class)
data class Bloco(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userOwnerId: String = "", // Adicionado valor padrão
    var nome: String = "",        // Adicionado valor padrão

    var favorito: Boolean = false, // <-- RENOMEADO DE 'isFavorito' PARA 'favorito'

    var subtitulo: String = "",
    var anotacao: String = "",
    var mensagemNotificacao: String = "",
    var tipoLembrete: TipoLembrete = TipoLembrete.NENHUM,
    var diasLembrete: List<Int> = emptyList(),
    var horariosLembrete: List<String> = emptyList(),
    var segundosLembrete: Long? = null,

    // Este campo é para controle de UI, não precisa ser salvo no Firestore.
    // @get:Exclude // Você pode adicionar isso se não quiser salvar no Firestore
    var isSelected: Boolean = false
) {
    // O construtor vazio não é mais necessário, pois todos os campos agora têm valor padrão.
}