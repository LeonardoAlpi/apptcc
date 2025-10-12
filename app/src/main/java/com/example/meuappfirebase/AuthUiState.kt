package com.example.meuappfirebase // Use o mesmo pacote dos seus outros arquivos

/**
 * Representa o estado da interface para operações de autenticação.
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)