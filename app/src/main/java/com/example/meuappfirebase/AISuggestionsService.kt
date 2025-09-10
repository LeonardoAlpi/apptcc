package com.example.meuappfirebase.ia

import com.example.meuappfirebase.Sugestao
import com.apol.myapplication.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class AISuggestionsService {

    /**
     * Esta função agora apenas GERA as sugestões e retorna uma lista.
     * Ela NÃO salva mais nada no Firestore.
     * Ela é uma função 'suspend' para que possa ser chamada de dentro de uma coroutine.
     */
    suspend fun generateSuggestions(userProfile: User?): List<Sugestao> {
        val userInterests = userProfile?.sugestoesInteresse?.joinToString(", ") ?: "bem-estar, saúde mental"
        val prompt = "Gere 5 sugestões de bem-estar para uma pessoa com interesse em $userInterests. Inclua 'categoria', 'titulo', 'descricao' e 'passos'."

        // Esta operação de rede deve ser feita em um thread de I/O.
        return withContext(Dispatchers.IO) {
            // SUBSTITUA ESTA LÓGICA PELO SEU CÓDIGO DA API DO GOOGLE CLOUD
            // Por enquanto, usaremos uma lista de sugestões fixa.
            // Quando a API do Google Cloud retornar os dados, você deve processá-los
            // e retorná-los neste formato.
            return@withContext getMockSuggestions()
        }
    }

    /**
     * Mock de sugestões. Substitua esta função pela chamada à API do Google Cloud.
     * O formato da resposta deve ser o mesmo.
     */
    private fun getMockSuggestions(): List<Sugestao> {
        return listOf(
            Sugestao(
                categoria = "LEITURA",
                titulo = "O Poder do Hábito",
                descricao = "Entenda a ciência por trás de como os hábitos se formam e como mudá-los.",
                passos = listOf("Leia o Capítulo 1", "Anote suas principais percepções.")
            ),
            Sugestao(
                categoria = "DIETA",
                titulo = "Smoothie Energético Matinal",
                descricao = "Uma receita simples e rápida para começar o dia com energia.",
                passos = listOf("1 banana", "200ml de leite vegetal", "1 colher de chia.")
            ),
            Sugestao(
                categoria = "MEDITACAO",
                titulo = "Meditação Guiada de 10 Minutos",
                descricao = "Foco na respiração e relaxamento para começar o dia com calma.",
                passos = listOf("Encontre um lugar tranquilo", "Sente-se confortavelmente", "Feche os olhos e respire profundamente por 10 minutos")
            ),
            Sugestao(
                categoria = "SAUDE_MENTAL_ESTRESSE",
                titulo = "Jornal do Agradecimento",
                descricao = "Passe 5 minutos escrevendo sobre 3 coisas pelas quais você é grato hoje.",
                passos = listOf("Pegue um caderno", "Anote 3 coisas", "Reflita sobre elas")
            ),
            Sugestao(
                categoria = "RESPIRACAO",
                titulo = "Técnica da Respiração Quadrada",
                descricao = "Uma técnica simples para acalmar o sistema nervoso em momentos de estresse.",
                passos = listOf("Inspire por 4s", "Segure por 4s", "Expire por 4s", "Segure por 4s. Repita 5 vezes.")
            ),
            Sugestao(
                categoria = "PODCASTS",
                titulo = "Podcast: Eslen Delanogare",
                descricao = "Um podcast com temas sobre saúde mental e autoconhecimento.",
                passos = listOf("Ouça o episódio 'O que é a vida?'", "Pense em sua experiência com o episódio")
            )
        )
    }
}