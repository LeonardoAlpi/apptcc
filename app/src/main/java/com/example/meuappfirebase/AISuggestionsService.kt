package com.example.meuappfirebase.ia

import android.content.Context
import android.util.Log
import com.apol.myapplication.data.model.User
import com.example.meuappfirebase.BuildConfig
import com.example.meuappfirebase.Sugestao
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class AISuggestionsService(private val context: Context) {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash-latest",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    suspend fun generateSuggestions(userProfile: User?): List<Sugestao> {
        val userInterests = userProfile?.sugestoesInteresse?.joinToString(", ") ?: "bem-estar geral"
        val userBadHabits = userProfile?.habitosNegativos?.joinToString(", ") ?: "nenhum em específico"

        val prompt = """
            Você é um assistente de bem-estar. Baseado no perfil de um usuário, gere 5 sugestões personalizadas e criativas.
            O usuário tem interesse em: "$userInterests".
            Os hábitos que ele quer mudar são: "$userBadHabits".

            Formate sua resposta EXATAMENTE como um array JSON, sem nenhum texto adicional antes ou depois.
            Cada objeto no array deve ter as seguintes chaves: "categoria", "titulo", "descricao", e "passos" (que é um array de strings).
            As categorias válidas são: LEITURA, DIETA, MEDITACAO, RESPIRACAO, PODCASTS, SAUDE_MENTAL_ANSIEDADE, SAUDE_MENTAL_DEPRESSAO, SAUDE_MENTAL_ESTRESSE, SAUDE_MENTAL_MOTIVACAO.
        """.trimIndent()

        return withContext(Dispatchers.IO) {
            try {
                Log.d("AIService", "Enviando prompt para a IA...")
                val response = generativeModel.generateContent(prompt)
                val responseText = response.text ?: ""
                Log.d("AIService", "Resposta da IA: $responseText")

                val suggestionsFromAI = parseJsonToSugestoes(responseText)

                // --- LÓGICA DE GARANTIA DE SUGESTÃO DE LIVRO ---
                val userWantsBook = userProfile?.temHabitoLeitura == true
                val aiGaveBook = suggestionsFromAI.any { it.categoria == "LEITURA" }

                if (userWantsBook && !aiGaveBook) {
                    // Se o usuário quer um livro e a IA não deu, adicionamos um padrão.
                    val defaultBookSuggestion = Sugestao(
                        categoria = "LEITURA",
                        titulo = "Sugestão Bônus de Leitura",
                        descricao = "Explorar novos livros pode abrir sua mente. Que tal começar com 'O Poder do Hábito' de Charles Duhigg?",
                        passos = listOf("Encontre um resumo online.", "Leia as primeiras 10 páginas.", "Veja se o tema te interessa.")
                    )
                    // Retorna a lista da IA + a nossa sugestão garantida
                    return@withContext suggestionsFromAI + defaultBookSuggestion
                } else {
                    // Se não, apenas retorna a lista original da IA
                    return@withContext suggestionsFromAI
                }

            } catch (e: Exception) {
                Log.e("AIService", "Erro ao chamar a API da IA", e)
                emptyList() // Retorna uma lista vazia em caso de erro
            }
        }
    }

    private fun parseJsonToSugestoes(jsonString: String): List<Sugestao> {
        val suggestions = mutableListOf<Sugestao>()
        try {
            val cleanedJson = jsonString
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val jsonArray = JSONArray(cleanedJson)
            for (i in 0 until jsonArray.length()) {
                val jsonObj = jsonArray.getJSONObject(i)
                val passosJson = jsonObj.getJSONArray("passos")
                val passosList = mutableListOf<String>()
                for (j in 0 until passosJson.length()) {
                    passosList.add(passosJson.getString(j))
                }

                suggestions.add(
                    Sugestao(
                        categoria = jsonObj.getString("categoria"),
                        titulo = jsonObj.getString("titulo"),
                        descricao = jsonObj.getString("descricao"),
                        passos = passosList
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("AIService", "Erro ao interpretar o JSON da IA", e)
        }
        return suggestions
    }
}