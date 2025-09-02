package com.example.meuappfirebase

data class UserProfile(
    val uid: String = "",
    val nome: String = "",
    val idade: Int = 0,
    val peso: Double = 0.0,
    val altura: Double = 0.0,
    val genero: String = "",
    val temHabitoLeitura: Boolean? = null,
    val segueDieta: Boolean? = null,
    val gostariaSeguirDieta: Boolean? = null,
    // --- ADICIONE ESTES CAMPOS ---
    val praticaAtividade: String? = null,
    val tempoDisponivel: String? = null,
    val espacosDisponiveis: List<String>? = null,
    val sugestoesInteresse: List<String>? = null
)