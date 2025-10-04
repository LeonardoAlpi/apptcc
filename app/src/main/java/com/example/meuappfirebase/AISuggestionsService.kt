package com.example.meuappfirebase.ia

import android.util.Log
import com.apol.myapplication.data.model.User
import com.example.meuappfirebase.Sugestao
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AISuggestionsService {

    // Pega uma referência para as Cloud Functions
    private val functions = Firebase.functions("southamerica-east1") // Especifique a região se necessário

    suspend fun generateSuggestions(userProfile: User?): List<Sugestao> {
        val userInterests = userProfile?.sugestoesInteresse?.joinToString(", ") ?: "bem-estar geral"
        val userBadHabits = userProfile?.habitosNegativos?.joinToString(", ") ?: "nenhum em específico"

        // Prepara os dados para enviar para a função
        val data = hashMapOf(
            "interests" to userInterests,
            "badHabits" to userBadHabits
        )

        return withContext(Dispatchers.IO) {
            try {
                Log.d("AIService", "Chamando a Cloud Function 'getAISuggestions'...")

                // Chama a função e espera o resultado
                val result = functions
                    .getHttpsCallable("getAISuggestions")
                    .call(data)
                    .await()

                // O resultado já vem como um objeto (geralmente uma List<HashMap<String, Any>>)
                val suggestionsList = result.data as? List<HashMap<String, Any>>

                // Converte o resultado para a nossa data class 'Sugestao'
                return@withContext suggestionsList?.mapNotNull { map ->
                    Sugestao(
                        categoria = map["categoria"] as? String ?: "",
                        titulo = map["titulo"] as? String ?: "",
                        descricao = map["descricao"] as? String ?: "",
                        passos = map["passos"] as? List<String> ?: emptyList()
                    )
                } ?: emptyList()

            } catch (e: Exception) {
                Log.e("AIService", "Erro ao chamar a Cloud Function", e)
                emptyList()
            }
        }
    }
}