package com.example.meuappfirebase

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.apol.myapplication.AppDatabase
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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


class SuggestionsViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = Firebase.auth
    private val firestore = Firebase.firestore
    private val userDao = AppDatabase.getDatabase(application).userDao()

    private val _suggestionCards = MutableLiveData<List<SuggestionCardState>>()
    val suggestionCards: LiveData<List<SuggestionCardState>> = _suggestionCards

    private val _statusMessage = MutableLiveData<Event<String>>()
    val statusMessage: LiveData<Event<String>> = _statusMessage

    // REMOVIDO: O grande mapa `suggestionSources` não é mais necessário.
    // A fonte de dados agora é 100% dinâmica e vem da IA via Firestore.

    // MODIFICADO: Mapeamento de 'categoria' da IA para a configuração do seu card.
    private val cardConfig = mapOf(
        "LEITURA" to Triple("livros", R.drawable.ic_book, "Livro Sugerido"),
        "DIETA" to Triple("dietas", R.drawable.ic_food, "Dica de Dieta"),
        "MEDITACAO" to Triple("meditacao", R.drawable.ic_meditation, "Prática de Meditação"),
        "RESPIRACAO" to Triple("respiracao", R.drawable.ic_breathing, "Respiração Guiada"),
        "PODCASTS" to Triple("podcasts", R.drawable.ic_podcast, "Podcast Sugerido"),
        // Usaremos a chave 'exercicios' para todas as sugestões de saúde mental
        "SAUDE_MENTAL_ANSIEDADE" to Triple("exercicios", R.drawable.ic_brain, "Exercício Mental"),
        "SAUDE_MENTAL_DEPRESSAO" to Triple("exercicios", R.drawable.ic_brain, "Exercício Mental"),
        "SAUDE_MENTAL_ESTRESSE" to Triple("exercicios", R.drawable.ic_brain, "Exercício Mental"),
        "SAUDE_MENTAL_MOTIVACAO" to Triple("exercicios", R.drawable.ic_brain, "Exercício Mental")
    )

    /**
     * MODIFICADO: Função principal que agora orquestra o carregamento de dados da IA
     * e o estado de conclusão do dia.
     */
    fun loadSuggestions() {
        val user = auth.currentUser ?: return
        val hoje = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

        viewModelScope.launch {
            val userProfile = userDao.getUserById(user.uid)
            val userInterests = userProfile?.sugestoesInteresse ?: emptyList()

            // 1. Busca as sugestões geradas pela IA
            firestore.collection("usuarios").document(user.uid)
                .collection("sugestoesIA").get()
                .addOnSuccessListener { aiDocuments ->
                    val sugestoesDaIA = aiDocuments.toObjects<Sugestao>()

                    // 2. Busca o estado de conclusão de hoje
                    firestore.collection("usuarios").document(user.uid)
                        .collection("estadoSugestoes").document(hoje).get()
                        .addOnSuccessListener { dailyStateDoc ->
                            val concluidas = dailyStateDoc.get("concluidas") as? List<String> ?: emptyList()
                            // 3. Constrói o estado da UI com os dois conjuntos de dados
                            buildUiStateFromAI(sugestoesDaIA, userInterests, concluidas)
                        }
                }
                .addOnFailureListener {
                    _statusMessage.postValue(Event("Não foi possível carregar as sugestões da IA."))
                }
        }
    }

    /**
     * NOVO: Constrói a lista de cards (UI State) a partir dos dados da IA e do estado diário.
     */
    private fun buildUiStateFromAI(
        sugestoesIA: List<Sugestao>,
        userInterests: List<String>,
        concluidas: List<String>
    ) {
        val cards = sugestoesIA.mapNotNull { sugestao ->
            val config = cardConfig[sugestao.categoria] ?: return@mapNotNull null

            // O conteúdo principal do card agora combina descrição e passos
            val descricaoCompleta = buildString {
                append(sugestao.descricao)
                if (sugestao.passos.isNotEmpty()) {
                    append("\n\nPassos:\n")
                    sugestao.passos.forEach { passo -> append("• $passo\n") }
                }
            }

            // Verifica se a categoria está nos interesses do usuário
            // (Isso usa a lógica do seu FAB de adicionar/remover sugestões)
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

    /**
     * MANTIDO: Marca uma sugestão como concluída para o dia.
     * A lógica é a mesma, mas agora usamos o título vindo da IA.
     */
    fun markSuggestionAsDone(suggestionTitle: String) {
        val user = auth.currentUser ?: return
        val hoje = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val docRef = firestore.collection("usuarios").document(user.uid)
            .collection("estadoSugestoes").document(hoje)

        // Garante que o documento do dia exista antes de atualizá-lo
        docRef.set(mapOf("lastUpdate" to FieldValue.serverTimestamp()), SetOptions.merge())
            .addOnSuccessListener {
                docRef.update("concluidas", FieldValue.arrayUnion(suggestionTitle))
                    .addOnSuccessListener { loadSuggestions() } // Recarrega para atualizar a UI
            }
    }

    /**
     * REMOVIDO: Esta função não é mais necessária, pois a IA fornece
     * uma única sugestão por categoria, não uma lista para ciclar.
     */
    // fun cycleToNextSuggestion(categoryKey: String) { ... }

    /**
     * MANTIDO: Atualiza as preferências de visibilidade do usuário.
     * Esta função do seu FAB continua 100% funcional.
     */
    fun updateVisibleSuggestions(newInterests: List<String>) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            val roomUser = userDao.getUserById(user.uid)
            roomUser?.let {
                it.sugestoesInteresse = newInterests // Corrigido para atribuir a 'var'
                userDao.updateUser(it)
            }
            firestore.collection("usuarios").document(user.uid).update("sugestoesInteresse", newInterests)
                .addOnSuccessListener {
                    _statusMessage.postValue(Event("Preferências salvas!"))
                    loadSuggestions() // Recarrega para aplicar a nova visibilidade
                }
        }
    }
}