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
    private val aiService = AISuggestionsService()

    private val _suggestionCards = MutableLiveData<List<SuggestionCardState>>()
    val suggestionCards: LiveData<List<SuggestionCardState>> = _suggestionCards

    private val _statusMessage = MutableLiveData<Event<String>>()
    val statusMessage: LiveData<Event<String>> = _statusMessage

    // LiveData para controlar o ProgressBar
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val cardConfig = mapOf(
        "LEITURA" to Triple("livros", R.drawable.ic_book, "Sugestões de Livros"),
        "DIETA" to Triple("dietas", R.drawable.ic_food, "Dicas de Dieta"),
        "MEDITACAO" to Triple("meditacao", R.drawable.ic_meditation, "Meditação"),
        "RESPIRACAO" to Triple("respiracao", R.drawable.ic_breathing, "Respiração Guiada"),
        "PODCASTS" to Triple("podcasts", R.drawable.ic_podcast, "Podcasts Relaxantes"),
        "SAUDE_MENTAL_ANSIEDADE" to Triple("exercicios", R.drawable.ic_brain, "Exercícios Mentais"),
        "SAUDE_MENTAL_DEPRESSAO" to Triple("exercicios", R.drawable.ic_brain, "Exercícios Mentais"),
        "SAUDE_MENTAL_ESTRESSE" to Triple("exercicios", R.drawable.ic_brain, "Exercícios Mentais"),
        "SAUDE_MENTAL_MOTIVACAO" to Triple("exercicios", R.drawable.ic_brain, "Exercícios Mentais")
    )

    /**
     * NOVA FUNÇÃO PRINCIPAL: Chamada pela tela para buscar as sugestões da IA em tempo real.
     */
    fun buscarSugestoesDaIA() {
        val user = auth.currentUser ?: return
        _isLoading.value = true // Mostra o loading
        viewModelScope.launch {
            val userProfile = userDao.getUserById(user.uid)
            try {
                // Chama o serviço que contata a Cloud Function
                val sugestoesDaIA = aiService.generateSuggestions(userProfile)

                if (sugestoesDaIA.isNotEmpty()) {
                    exibirSugestoes(sugestoesDaIA)
                } else {
                    _statusMessage.postValue(Event("Não foi possível obter sugestões no momento."))
                    _suggestionCards.postValue(emptyList()) // Limpa a tela se não vier nada
                }

            } catch (e: Exception) {
                _statusMessage.postValue(Event("Erro ao conectar com a IA."))
                Log.e("SuggestionsViewModel", "Erro ao gerar sugestões da IA", e)
            } finally {
                _isLoading.value = false // Esconde o loading, mesmo se der erro
            }
        }
    }

    private fun exibirSugestoes(sugestoes: List<Sugestao>) {
        val user = auth.currentUser ?: return
        val hoje = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val estadoRef = firestore.collection("usuarios").document(user.uid).collection("estadoSugestoes").document(hoje)

        // Busca o estado de conclusão das sugestões de hoje
        estadoRef.get().addOnSuccessListener { estadoSnapshot ->
            val concluidas = estadoSnapshot.get("concluidas") as? List<String> ?: emptyList()
            viewModelScope.launch {
                val userProfile = userDao.getUserById(user.uid)
                val userInterests = userProfile?.sugestoesInteresse
                buildUiStateFromAI(sugestoes, userInterests, concluidas)
            }
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
                    .addOnSuccessListener { buscarSugestoesDaIA() }
            }
    }

    // --- FUNÇÃO ADICIONADA ---
    // Esta é a nova função que permite desmarcar uma sugestão
    fun markSuggestionAsNotDone(suggestionTitle: String) {
        val user = auth.currentUser ?: return
        val hoje = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val docRef = firestore.collection("usuarios").document(user.uid)
            .collection("estadoSugestoes").document(hoje)

        // Usa arrayRemove para tirar o item da lista de "concluidas"
        docRef.update("concluidas", FieldValue.arrayRemove(suggestionTitle))
            .addOnSuccessListener {
                // Recarrega as sugestões para a UI refletir a mudança
                buscarSugestoesDaIA()
            }
            .addOnFailureListener { e ->
                _statusMessage.postValue(Event("Erro ao desmarcar sugestão."))
                Log.e("SuggestionsViewModel", "Erro ao remover do array 'concluidas'", e)
            }
    }
    // --- FIM DA FUNÇÃO ADICIONADA ---

    fun updateVisibleSuggestions(newInterests: List<String>) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            val roomUser = userDao.getUserById(user.uid)
            roomUser?.let {
                it.sugestoesInteresse = newInterests
                userDao.updateUser(it)
                firestore.collection("usuarios").document(user.uid).update("sugestoesInteresse", newInterests)
                    .addOnSuccessListener {
                        _statusMessage.postValue(Event("Preferências salvas!"))
                        buscarSugestoesDaIA()
                    }
            }
        }
    }
}