package com.example.meuappfirebase

data class Sugestao(
    val categoria: String = "",
    val titulo: String = "",
    val descricao: String = "",
    val passos: List<String> = emptyList()
)