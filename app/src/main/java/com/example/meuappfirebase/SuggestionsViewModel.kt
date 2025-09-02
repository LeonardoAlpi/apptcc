package com.example.meuappfirebase

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.apol.myapplication.AppDatabase
import com.example.meuappfirebase.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Data class para representar o estado de cada card na UI
data class SuggestionCardState(
    val key: String,
    val isVisible: Boolean,
    val iconResId: Int,
    val title: String,
    val suggestionTitle: String,
    val suggestionDescription: String,
    val isCompleted: Boolean
)

class SuggestionsViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = Firebase.auth
    private val firestore = Firebase.firestore
    private val userDao = AppDatabase.getDatabase(application).userDao()

    private val _suggestionCards = MutableLiveData<List<SuggestionCardState>>()
    val suggestionCards: LiveData<List<SuggestionCardState>> = _suggestionCards

    private val _statusMessage = MutableLiveData<Event<String>>()
    val statusMessage: LiveData<Event<String>> = _statusMessage

    // BANCO DE DADOS DE SUGESTÕES, AGORA DENTRO DO VIEWMODEL
    private val suggestionSources = mapOf(
        "livros" to listOf(
            "O Poder do Hábito" to "Por Charles Duhigg. Entenda como os hábitos funcionam e como mudá-los.",
            "Mindset" to "Por Carol S. Dweck. A nova psicologia do sucesso.",
            "Comece pelo Porquê" to "Por Simon Sinek. Como grandes líderes inspiram ação."
        ).shuffled(),
        "dietas" to listOf(
            "Beba Mais Água" to "Mantenha-se hidratado ao longo do dia com água e chás sem açúcar.",
            "Planeje suas Refeições" to "Dedique um tempo para planejar as refeições da semana.",
            "Reduza os Ultraprocessados" to "Diminua o consumo de salgadinhos e refrigerantes."
        ).shuffled(),
        "meditacao" to listOf(
            "5 Minutos de Atenção Plena" to "Sente-se em silêncio e foque apenas na sua respiração.",
            "Escaneamento Corporal" to "Deite-se e leve sua atenção para cada parte do seu corpo, relaxando."
        ).shuffled(),
        "respiracao" to listOf(
            "Técnica 4-7-8" to "Inspire por 4s, segure por 7s e expire por 8s. Repita 3 vezes.",
            "Respiração Abdominal" to "Inspire sentindo sua barriga se expandir e expire lentamente."
        ).shuffled(),
        "podcasts" to listOf(
            "Podcast: Autoconsciente" to "Uma jornada sobre autoconhecimento e psicologia.",
            "Podcast: Estamos Bem?" to "Aborda o bem-estar de forma leve e acolhedora."
        ).shuffled(),
        "exercicios" to listOf(
            "Diário de Gratidão" to "Antes de dormir, escreva três coisas pelas quais você foi grato(a) hoje.",
            "Desafie seu Cérebro" to "Dedique 15 minutos a um quebra-cabeça, Sudoku ou palavras-cruzadas."
        ).shuffled()
    )
    private val cardConfig = mapOf(
        "livros" to Triple(R.drawable.ic_book, "Livro do Mês", "livros"),
        "dietas" to Triple(R.drawable.ic_food, "Dica de Dieta", "dietas"),
        "meditacao" to Triple(R.drawable.ic_meditation, "Prática de Meditação", "meditacao"),
        "respiracao" to Triple(R.drawable.ic_breathing, "Respiração Guiada", "respiracao"),
        "podcasts" to Triple(R.drawable.ic_podcast, "Podcast Sugerido", "podcasts"),
        "exercicios" to Triple(R.drawable.ic_brain, "Exercício Mental", "exercicios")
    )

    fun loadSuggestions() {
        val user = auth.currentUser ?: return
        val hoje = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

        viewModelScope.launch {
            val userProfile = userDao.getUserById(user.uid)
            val userInterests = userProfile?.sugestoesInteresse ?: listOf("Nenhuma atividade")
            val dailyStateDocRef = firestore.collection("usuarios").document(user.uid)
                .collection("estadoSugestoes").document(hoje)

            dailyStateDocRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    buildUiState(userInterests, document.data ?: emptyMap())
                } else {
                    calculateNewDayStateAndBuildUi(userInterests)
                }
            }
        }
    }

    private fun calculateNewDayStateAndBuildUi(userInterests: List<String>) {
        val user = auth.currentUser ?: return
        val hoje = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val ontem = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(System.currentTimeMillis() - 86400000))

        val yesterdayStateDocRef = firestore.collection("usuarios").document(user.uid)
            .collection("estadoSugestoes").document(ontem)

        yesterdayStateDocRef.get().addOnSuccessListener { yesterdayDoc ->
            val yesterdayIndices = yesterdayDoc?.get("indices") as? Map<String, Long> ?: emptyMap()
            val newIndices = mutableMapOf<String, Int>()
            suggestionSources.keys.forEach { key ->
                val lastIndex = yesterdayIndices[key]?.toInt() ?: -1
                newIndices[key] = (lastIndex + 1) % (suggestionSources[key]?.size ?: 1)
            }

            val newDailyState = mapOf(
                "indices" to newIndices,
                "concluidas" to emptyList<String>()
            )

            firestore.collection("usuarios").document(user.uid)
                .collection("estadoSugestoes").document(hoje).set(newDailyState)

            buildUiState(userInterests, newDailyState)
        }
    }

    private fun buildUiState(userInterests: List<String>, dailyState: Map<String, Any>) {
        val indices = dailyState["indices"] as? Map<String, Long> ?: emptyMap()
        val concluidas = dailyState["concluidas"] as? List<String> ?: emptyList()

        val cards = cardConfig.map { (key, config) ->
            val sourceList = suggestionSources[key]!!
            val currentIndex = indices[key]?.toInt() ?: 0
            val currentSuggestion = sourceList.getOrNull(currentIndex) ?: ("Vazio" to "")

            SuggestionCardState(
                key = key,
                isVisible = userInterests.any { interest -> config.second.contains(interest, ignoreCase = true) || interest.contains(key, ignoreCase = true) },
                iconResId = config.first,
                title = config.second,
                suggestionTitle = currentSuggestion.first,
                suggestionDescription = currentSuggestion.second,
                isCompleted = concluidas.contains(currentSuggestion.first)
            )
        }
        _suggestionCards.postValue(cards)
    }

    fun markSuggestionAsDone(suggestionTitle: String) {
        val user = auth.currentUser ?: return
        val hoje = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val docRef = firestore.collection("usuarios").document(user.uid)
            .collection("estadoSugestoes").document(hoje)

        docRef.update("concluidas", FieldValue.arrayUnion(suggestionTitle))
            .addOnSuccessListener { loadSuggestions() }
    }

    fun cycleToNextSuggestion(categoryKey: String) {
        val user = auth.currentUser ?: return
        val hoje = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val docRef = firestore.collection("usuarios").document(user.uid)
            .collection("estadoSugestoes").document(hoje)

        val sourceList = suggestionSources[categoryKey]!!
        docRef.get().addOnSuccessListener { doc ->
            val indices = (doc.get("indices") as? Map<String, Long> ?: emptyMap()).toMutableMap()
            val currentIndex = indices[categoryKey]?.toInt() ?: 0
            indices[categoryKey] = ((currentIndex + 1) % sourceList.size).toLong()
            docRef.update("indices", indices).addOnSuccessListener { loadSuggestions() }
        }
    }

    fun updateVisibleSuggestions(newInterests: List<String>) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            val roomUser = userDao.getUserById(user.uid)
            roomUser?.let {
                val updatedUser = it.copy(sugestoesInteresse = newInterests)
                userDao.updateUser(updatedUser)
            }
            firestore.collection("usuarios").document(user.uid).update("sugestoesInteresse", newInterests)
                .addOnSuccessListener {
                    _statusMessage.postValue(Event("Preferências salvas!"))
                    loadSuggestions()
                }
        }
    }
}