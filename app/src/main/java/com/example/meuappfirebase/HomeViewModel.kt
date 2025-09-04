package com.example.meuappfirebase // Ou o pacote dos seus ViewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth
    private val usuarioLogadoId = auth.currentUser?.uid

    // StateFlow para expor a lista de hábitos favoritados para a UI
    private val _favoritedHabitsList = MutableStateFlow<List<HabitUI>>(emptyList())
    val favoritedHabitsList = _favoritedHabitsList.asStateFlow()

    init {
        // Começa a ouvir os hábitos favoritados em tempo real
        carregarHabitosFavoritados()
    }

    private fun carregarHabitosFavoritados() {
        if (usuarioLogadoId == null) {
            Log.w("HomeViewModel", "Usuário nulo, não é possível carregar hábitos.")
            return
        }

        firestore.collection("habitos")
            .whereEqualTo("userOwnerId", usuarioLogadoId)
            .whereEqualTo("isFavorito", true) // A MÁGICA ACONTECE AQUI!
            .limit(3) // Garante que o Firestore não traga mais de 3
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.w("HomeViewModel", "Erro ao ouvir os hábitos favoritados.", error)
                    return@addSnapshotListener
                }

                // Mapeia os documentos para a nossa classe de UI (HabitUI)
                val habits = snapshots?.documents?.mapNotNull { doc ->
                    try {
                        HabitUI(
                            id = doc.id,
                            name = doc.getString("nome") ?: "",
                            // Preencha outros campos se necessário, mas para o widget só precisamos do ID e nome
                            streakDays = 0,
                            message = "",
                            count = 0,
                            isFavorited = true,
                            isGoodHabit = true
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                _favoritedHabitsList.value = habits
            }
    }
}