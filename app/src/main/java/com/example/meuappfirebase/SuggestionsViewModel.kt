package com.example.meuappfirebase

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.apol.myapplication.AppDatabase
import com.apol.myapplication.data.model.User
import com.example.meuappfirebase.ia.AISuggestionsService
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SuggestionsViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = Firebase.auth
    private val firestore = Firebase.firestore
    private val userDao = AppDatabase.getDatabase(application).userDao()
    private val aiService = AISuggestionsService(application)

    private val _suggestionCards = MutableLiveData<List<SuggestionCardState>>()
    val suggestionCards: LiveData<List<SuggestionCardState>> = _suggestionCards

    private val _statusMessage = MutableLiveData<Event<String>>()
    val statusMessage: LiveData<Event<String>> = _statusMessage

    // ... (o seu cardConfig continua o mesmo)
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


    /**
     * MUDANÇA: Gera novas sugestões e as salva. Chamada apenas em momentos estratégicos.
     */
    fun gerarEcarregarSugestoes() {
        val user = auth.currentUser ?: return
        _statusMessage.postValue(Event("Gerando novas sugestões personalizadas..."))
        viewModelScope.launch {
            val userProfile = userDao.getUserById(user.uid)
            try {
                val sugestoesDaIA = aiService.generateSuggestions(userProfile)
                saveSuggestionsToFirestore(user.uid, sugestoesDaIA) {
                    carregarSugestoesDoCache()
                }
            } catch (e: Exception) {
                _statusMessage.postValue(Event("Não foi possível gerar as sugestões com a IA."))
                Log.e("SuggestionsViewModel", "Erro ao gerar sugestões da IA", e)
            }
        }
    }

    /**
     * MUDANÇA: Apenas lê os dados que já estão salvos no Firestore. É rápido e eficiente.
     */
    fun carregarSugestoesDoCache() {
        val user = auth.currentUser ?: return
        val hoje = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

        val sugestoesRef = firestore.collection("usuarios").document(user.uid).collection("sugestoesIA")
        val estadoRef = firestore.collection("usuarios").document(user.uid).collection("estadoSugestoes").document(hoje)

        sugestoesRef.get().addOnSuccessListener { sugestoesSnapshot ->
            val sugestoesDaIA = sugestoesSnapshot.toObjects(Sugestao::class.java)

            estadoRef.get().addOnSuccessListener { estadoSnapshot ->
                val concluidas = estadoSnapshot.get("concluidas") as? List<String> ?: emptyList()
                viewModelScope.launch {
                    val userProfile = userDao.getUserById(user.uid)
                    val userInterests = userProfile?.sugestoesInteresse
                    buildUiStateFromAI(sugestoesDaIA, userInterests, concluidas)
                }
            }
        }.addOnFailureListener {
            _statusMessage.postValue(Event("Não foi possível carregar sugestões."))
        }
    }

    private fun saveSuggestionsToFirestore(userId: String, suggestions: List<Sugestao>, onComplete: () -> Unit) {
        val batch = firestore.batch()
        val collectionRef = firestore.collection("usuarios").document(userId).collection("sugestoesIA")

        collectionRef.get().addOnSuccessListener { documents ->
            for (doc in documents) { batch.delete(doc.reference) }
            suggestions.forEach { sugestao ->
                val newDocRef = collectionRef.document()
                batch.set(newDocRef, sugestao)
            }
            batch.commit().addOnSuccessListener { onComplete() }
        }
    }

    private fun buildUiStateFromAI(
        sugestoesIA: List<Sugestao>,
        userInterests: List<String>?,
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
            val isVisible = userInterests.isNullOrEmpty() || userInterests.any { interest ->
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
                    .addOnSuccessListener { carregarSugestoesDoCache() } // Atualiza a UI
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
                    carregarSugestoesDoCache() // Atualiza a UI
                }
        }
    }
}