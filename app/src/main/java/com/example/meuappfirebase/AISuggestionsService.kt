package com.example.meuappfirebase.ia

import com.apol.myapplication.data.model.User
import com.example.meuappfirebase.Sugestao
import com.example.meuappfirebase.SugestoesPredefinidas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AISuggestionsService {

    // A função agora é muito mais simples e não precisa de Context.
    suspend fun generateSuggestions(userProfile: User?): List<Sugestao> {
        return withContext(Dispatchers.Default) {
            // Em vez de chamar a nuvem, simplesmente pega a lista pré-definida.
            return@withContext SugestoesPredefinidas.getSugestoes()
        }
    }
}