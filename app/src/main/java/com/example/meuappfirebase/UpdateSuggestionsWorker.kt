package com.example.meuappfirebase

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.apol.myapplication.AppDatabase
import com.example.meuappfirebase.ia.AISuggestionsService
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class UpdateSuggestionsWorker(appContext: Context, workerParams: WorkerParameters):
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("UpdateSuggestionsWorker", "Iniciando tarefa diária de atualização de sugestões...")
        val auth = Firebase.auth
        val firestore = Firebase.firestore
        val userDao = AppDatabase.getDatabase(applicationContext).userDao()
        val aiService = AISuggestionsService()

        val user = auth.currentUser ?: return Result.failure()
        val userProfile = userDao.getUserById(user.uid)

        return try {
            // Gera novas sugestões
            val newSuggestions = aiService.generateSuggestions(userProfile)

            if (newSuggestions.isEmpty()) {
                Log.w("UpdateSuggestionsWorker", "IA não retornou sugestões. Tarefa terminando.")
                return Result.success() // Sucesso, mas sem dados para atualizar
            }

            // Salva no Firestore (sobrescrevendo as antigas)
            val collectionRef = firestore.collection("usuarios").document(user.uid).collection("sugestoesIA")

            // Limpa as sugestões antigas
            val oldSuggestions = collectionRef.get().await()
            val batch = firestore.batch()
            for (doc in oldSuggestions) {
                batch.delete(doc.reference)
            }

            // Adiciona as novas
            newSuggestions.forEach { sugestao ->
                val newDocRef = collectionRef.document()
                batch.set(newDocRef, sugestao)
            }

            batch.commit().await()
            Log.d("UpdateSuggestionsWorker", "Sugestões atualizadas com sucesso no Firestore.")
            Result.success()
        } catch (e: Exception) {
            Log.e("UpdateSuggestionsWorker", "Falha na tarefa de atualização de sugestões.", e)
            Result.failure()
        }
    }
}