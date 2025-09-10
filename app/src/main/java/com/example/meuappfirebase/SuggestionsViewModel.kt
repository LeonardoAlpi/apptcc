package com.example.meuappfirebase

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.apol.myapplication.AppDatabase
import com.example.meuappfirebase.ia.AISuggestionsService
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Importa a classe Event de onde ela foi definida
import com.example.meuappfirebase.Event // Certifique-se que o caminho esteja correto

// Data class para representar o estado de cada card na UI (Sua classe, sem alterações)
data class SuggestionCardState(
    val key: String,
    val isVisible: Boolean,
    val iconResId: Int,
    val title: String,
    val suggestionTitle: String,
    val suggestionDescription: String,
    val isCompleted: Boolean
)

// Data class para receber os dados da IA do Firestore
data class Sugestao(
    val categoria: String = "",
    val titulo: String = "",
    val descricao: String = "",
    val passos: List<String> = emptyList()
)

// **REMOVIDA**: A classe 'Event' foi removida daqui para evitar redeclaração.
//              Agora ela é importada do seu arquivo original.

class SuggestionsViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = Firebase.auth
    private val firestore = Firebase.firestore
    private val userDao = AppDatabase.getDatabase(application).userDao()

    private val aiService = AISuggestionsService()

    private val _suggestionCards = MutableLiveData<List<SuggestionCardState>>()
    val suggestionCards: LiveData<List<SuggestionCardState>> = _suggestionCards

    private val _statusMessage = MutableLiveData<Event<String>>()
    val statusMessage: LiveData<Event<String>> = _statusMessage

    private val cardConfig = mapOf(
        "LEITURA" to Triple("livros", R.drawable.ic_book, "Livro Sugerido"),
        "DIETA" to Triple("dietas", R.drawable.ic_food, "Dica de Dieta"),
        "MEDITACAO" to Triple("meditacao", R.drawable.ic_meditation, "Prática de Meditação"),
        "RESPIRACAO" to Triple("respiracao", R.drawable.ic_breathing, "Respiração Guiada"),
        "PODCASTS" to Triple("podcasts", R.drawable.ic_podcast, "Podcast Sugerido"),
        "SAUDE_MENTAL_ANSIEDADE" to Triple("exercicios", R.drawable.ic_brain, "Exercício Mental"),
        "SAUDE_MENTAL_DEPRESSAO" to Triple("exercicios", R.drawable.ic_brain, "Exercício Mental"),
        "SAUDE_MENTAL_ESTRESSE" to Triple("exercicios", R.drawable.ic_brain, "Exercício Mental"),
        "SAUDE_MENTAL_MOTIVACAO" to Triple("exercicios", R.drawable.ic_brain, "Exercício Mental")
    )

    fun loadSuggestions() {
        val user = auth.currentUser ?: return
        val hoje = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

        viewModelScope.launch {
            val userProfile = userDao.getUserById(user.uid)

            try {
                // 1. Gera as sugestões com o serviço de IA
                val sugestoesDaIA = aiService.generateSuggestions(userProfile)

                // 2. Salva as sugestões no Firestore
                saveSuggestionsToFirestore(user.uid, sugestoesDaIA)

                // 3. Busca o estado de conclusão de hoje
                firestore.collection("usuarios").document(user.uid)
                    .collection("estadoSugestoes").document(hoje).get()
                    .addOnSuccessListener { dailyStateDoc ->
                        val concluidas = dailyStateDoc.get("concluidas") as? List<String> ?: emptyList()
                        // 4. Constrói o estado da UI com os dois conjuntos de dados
                        val userInterests = userProfile?.sugestoesInteresse ?: emptyList()
                        buildUiStateFromAI(sugestoesDaIA, userInterests, concluidas)
                    }
            } catch (e: Exception) {
                _statusMessage.postValue(Event("Não foi possível gerar as sugestões com a IA."))
                Log.e("SuggestionsViewModel", "Erro ao gerar sugestões da IA", e)
            }
        }
    }

    private fun saveSuggestionsToFirestore(userId: String, suggestions: List<Sugestao>) {
        val batch = firestore.batch()
        val collectionRef = firestore.collection("usuarios").document(userId).collection("sugestoesIA")

        collectionRef.get().addOnSuccessListener { documents ->
            for (doc in documents) {
                batch.delete(doc.reference)
            }
            suggestions.forEach { sugestao ->
                val newDocRef = collectionRef.document()
                batch.set(newDocRef, sugestao)
            }
            batch.commit()
        }
    }

    private fun buildUiStateFromAI(
        sugestoesIA: List<Sugestao>,
        userInterests: List<String>,
        concluidas: List<String>
    ) {
        val cards = sugestoesIA.mapNotNull { sugestao ->
            val config = cardConfig[sugestao.categoria] ?: return@mapNotNull null

            val descricaoCompleta = buildString {
                append(sugestao.descricao)
                if (sugestao.passos.isNotEmpty()) {
                    append("\n\nPassos:\n")
                    sugestao.passos.forEach { passo -> append("• $passo\n") }
                }
            }

            val isVisible = userInterests.any { interest ->
                config.third.contains(interest, ignoreCase = true) || interest.contains(config.first, ignoreCase = true)
            }

            SuggestionCardState(
                key = config.first,
                isVisible = isVisible,
                iconResId = config.second,
                title = config.third,
                suggestionTitle = sugestao.titulo,
                suggestionDescription = descricaoCompleta,
                isCompleted = concluidas.contains(sugestao.titulo)
            )
        }
        _suggestionCards.postValue(cards)
    }

    fun markSuggestionAsDone(suggestionTitle: String) {
        val user = auth.currentUser ?: return
        val hoje = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val docRef = firestore.collection("usuarios").document(user.uid)
            .collection("estadoSugestoes").document(hoje)

        docRef.set(mapOf("lastUpdate" to FieldValue.serverTimestamp()), SetOptions.merge())
            .addOnSuccessListener {
                docRef.update("concluidas", FieldValue.arrayUnion(suggestionTitle))
                    .addOnSuccessListener { loadSuggestions() }
            }
    }

    fun updateVisibleSuggestions(newInterests: List<String>) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            val roomUser = userDao.getUserById(user.uid)
            roomUser?.let {
                it.sugestoesInteresse = newInterests
                userDao.updateUser(it)
            }
            firestore.collection("usuarios").document(user.uid).update("sugestoesInteresse", newInterests)
                .addOnSuccessListener {
                    _statusMessage.postValue(Event("Preferências salvas!"))
                    loadSuggestions()
                }
        }
    }
}