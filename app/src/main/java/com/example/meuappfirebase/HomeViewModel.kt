package com.example.meuappfirebase

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

    // CORRIGIDO: Obtém o ID do usuário apenas quando necessário, garantindo que seja o mais recente.
    private val usuarioLogadoId: String?
        get() = auth.currentUser?.uid

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
                        val nome = doc.getString("nome") ?: ""
                        val userOwnerId = doc.getString("userOwnerId") ?: ""
                        val isFavorito = doc.getBoolean("isFavorito") ?: false
                        val isGoodHabit = doc.getBoolean("isGoodHabit") ?: true

                        HabitUI(
                            id = doc.id,
                            name = nome,
                            // Esses campos não são necessários para a lista de favoritos, mas o construtor exige.
                            // Passamos valores padrão seguros.
                            streakDays = 0,
                            message = "",
                            count = 0,
                            isFavorited = isFavorito,
                            isGoodHabit = isGoodHabit,
                            userOwnerId = userOwnerId // CORREÇÃO: O campo foi adicionado
                        )
                    } catch (e: Exception) {
                        Log.e("HomeViewModel", "Erro ao converter documento para HabitUI", e)
                        null
                    }
                } ?: emptyList()

                _favoritedHabitsList.value = habits
            }
    }
}