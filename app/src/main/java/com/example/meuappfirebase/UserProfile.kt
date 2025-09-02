package com.example.meuappfirebase

// Este é o arquivo UserProfile.kt (ou onde quer que ele esteja)

data class UserProfile(
    val uid: String = "",
    val nome: String = "",
    val idade: Int = 0,
    val peso: Double = 0.0,
    val altura: Double = 0.0,
    val genero: String = "",

    // Adicione todos os campos do questionário aqui também
    val temHabitoLeitura: Boolean? = null,
    val segueDieta: Boolean? = null,
    val gostariaSeguirDieta: Boolean? = null,
    val praticaAtividade: String? = null,
    val tempoDisponivel: String? = null,
    val espacosDisponiveis: List<String>? = null,
    val sugestoesInteresse: List<String>? = null,

    // --- LINHAS NOVAS PARA CORRIGIR O ERRO ---
    // Os nomes devem ser IDÊNTICOS aos do Firestore ("habitosParaMudar")
    val habitosParaMudar: List<String>? = null,
    val problemasEmocionais: List<String>? = null
)