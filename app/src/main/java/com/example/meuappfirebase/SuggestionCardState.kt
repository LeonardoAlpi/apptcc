package com.example.meuappfirebase // Ou o pacote onde seus outros modelos estão

/**
 * Representa o estado visual de um único card de sugestão na tela.
 */
data class SuggestionCardState(
    val key: String,
    val isVisible: Boolean,
    val iconResId: Int,
    val title: String,
    val suggestionTitle: String,
    val suggestionDescription: String,
    val isCompleted: Boolean
)