package com.example.meuappfirebase

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.apol.myapplication.AppDatabase
import com.apol.myapplication.BlocoNotificationReceiver
import com.apol.myapplication.data.model.Bloco
import com.apol.myapplication.data.model.Note
import com.apol.myapplication.data.model.TipoLembrete
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = Firebase.auth
    private val firestore = Firebase.firestore
    private val notesDao = AppDatabase.getDatabase(application).notesDao()
    private val alarmManager = application.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private val _notes = MutableLiveData<List<Note>>()
    val notes: LiveData<List<Note>> = _notes

    private val _blocos = MutableLiveData<List<Bloco>>()
    val blocos: LiveData<List<Bloco>> = _blocos

    private val _statusMessage = MutableLiveData<Event<String>>()
    val statusMessage: LiveData<Event<String>> = _statusMessage

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            notesDao.getNotesByUser(user.uid).collect { _notes.postValue(it) }
        }

        // Listener em tempo real para os blocos direto do Firestore
        firestore.collection("usuarios").document(user.uid).collection("blocos")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    _statusMessage.postValue(Event("Erro ao carregar blocos."))
                    return@addSnapshotListener
                }
                // Mapeia os documentos, garantindo que o ID do Firestore seja atribuído
                val blocosList = snapshots?.documents?.mapNotNull { doc ->
                    doc.toObject(Bloco::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                _blocos.postValue(blocosList)
                // Sincroniza a lista com o banco de dados local (Room)
                viewModelScope.launch {
                    notesDao.syncBlocos(user.uid, blocosList)
                }
            }
    }

    fun addNote(text: String) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            notesDao.insertNote(Note(userOwnerId = user.uid, text = text))
        }
    }

    fun updateNote(note: Note) = viewModelScope.launch { notesDao.updateNote(note) }

    fun deleteNotes(notes: List<Note>) = viewModelScope.launch { notesDao.deleteNotes(notes) }

    // --- FUNÇÃO addBloco CORRIGIDA ---
    fun addBloco(nome: String) {
        val user = auth.currentUser ?: return
        // O Bloco já é criado com um ID único (UUID)
        val novoBloco = Bloco(userOwnerId = user.uid, nome = nome)

        // Usamos .document(ID).set(OBJETO) para forçar o Firestore a usar nosso ID
        firestore.collection("usuarios").document(user.uid).collection("blocos")
            .document(novoBloco.id) // << Usa o ID gerado no app
            .set(novoBloco)         // << Salva o objeto inteiro
            .addOnSuccessListener {
                Log.d("NotesViewModel", "Bloco criado com sucesso no Firestore com ID: ${novoBloco.id}")
                // O listener em tempo real já vai atualizar a lista na UI e no Room.
            }
            .addOnFailureListener { e ->
                _statusMessage.value = Event("Erro ao criar bloco: ${e.message}")
            }
    }

    fun updateBloco(bloco: Bloco) {
        val user = auth.currentUser ?: return
        firestore.collection("usuarios").document(user.uid).collection("blocos")
            .document(bloco.id).set(bloco) // .set() também funciona para atualizar/sobrescrever
            .addOnSuccessListener {
                // O listener em tempo real já vai atualizar o Room.
                cancelarLembretesParaBloco(bloco)
                if (bloco.tipoLembrete != TipoLembrete.NENHUM) {
                    agendarLembretesParaBloco(bloco)
                }
            }
    }

    fun deleteBlocos(blocos: List<Bloco>) {
        val user = auth.currentUser ?: return
        val batch = firestore.batch()
        blocos.forEach { bloco ->
            cancelarLembretesParaBloco(bloco)
            val docRef = firestore.collection("usuarios").document(user.uid).collection("blocos").document(bloco.id)
            batch.delete(docRef)
        }
        batch.commit() // O listener em tempo real vai remover do Room.
    }

    fun toggleFavoritoBloco(bloco: Bloco) {
        val userId = auth.currentUser?.uid ?: return
        val blocosAtuais = _blocos.value ?: return
        val favoritosAtuais = blocosAtuais.count { it.favorito }

        if (!bloco.favorito && favoritosAtuais >= 3) {
            _statusMessage.value = Event("Você só pode favoritar até 3 blocos.")
            return
        }

        val novoStatus = !bloco.favorito

        firestore.collection("usuarios").document(userId).collection("blocos")
            .document(bloco.id)
            .update("favorito", novoStatus) // Atualiza apenas o campo 'favorito'
            .addOnFailureListener { e ->
                _statusMessage.value = Event("Erro ao sincronizar favorito: ${e.message}")
            }
    }

    // --- LÓGICA DE ALARME E NOTIFICAÇÃO ---
    // (O resto do seu código de alarmes continua aqui, sem alterações)
    private fun agendarLembretesParaBloco(bloco: Bloco) {
        val context = getApplication<Application>().applicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            _statusMessage.postValue(Event("Permissão para agendar alarmes é necessária."))
            return
        }
        var requestCodeCounter = 0
        when (bloco.tipoLembrete) {
            TipoLembrete.DIARIO -> {
                bloco.horariosLembrete.forEach { horarioStr ->
                    val (hora, minuto) = parseHorario(horarioStr) ?: return@forEach
                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, hora); set(Calendar.MINUTE, minuto); set(Calendar.SECOND, 0)
                        if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
                    }
                    val pendingIntent = getPendingIntent(context, bloco, bloco.id.hashCode() + requestCodeCounter++)
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
                }
            }
            TipoLembrete.MENSAL -> {
                bloco.diasLembrete.forEach { dia ->
                    bloco.horariosLembrete.forEach { horarioStr ->
                        val (hora, minuto) = parseHorario(horarioStr) ?: return@forEach
                        getNextMonthlyOccurrence(dia, hora, minuto)?.let { calendar ->
                            val pendingIntent = getPendingIntent(context, bloco, bloco.id.hashCode() + requestCodeCounter++)
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                        }
                    }
                }
            }
            else -> {}
        }
    }

    private fun cancelarLembretesParaBloco(bloco: Bloco) {
        val context = getApplication<Application>().applicationContext
        for (i in 0 until 50) {
            val requestCode = bloco.id.hashCode() + i
            val pendingIntent = getPendingIntent(context, bloco, requestCode)
            alarmManager.cancel(pendingIntent)
        }
    }

    private fun getPendingIntent(context: Context, bloco: Bloco, requestCode: Int): PendingIntent {
        val intent = Intent(context, BlocoNotificationReceiver::class.java).apply {
            putExtra("titulo", bloco.nome + if (bloco.subtitulo.isNotEmpty()) " - ${bloco.subtitulo}" else "")
            putExtra("mensagem", bloco.mensagemNotificacao.ifEmpty { "Você tem um lembrete para este bloco." })
            putExtra("bloco_id", bloco.id)
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getBroadcast(context, requestCode, intent, flags)
    }

    private fun parseHorario(horarioStr: String): Pair<Int, Int>? {
        val parts = horarioStr.split(":").mapNotNull { it.toIntOrNull() }
        return if (parts.size == 2) parts[0] to parts[1] else null
    }

    private fun getNextMonthlyOccurrence(dia: Int, hora: Int, minuto: Int): Calendar? {
        val agora = Calendar.getInstance()
        val proximoAgendamento = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hora); set(Calendar.MINUTE, minuto); set(Calendar.SECOND, 0)
        }
        val maxDayOfMonth = proximoAgendamento.getActualMaximum(Calendar.DAY_OF_MONTH)
        val diaValido = if (dia > maxDayOfMonth) maxDayOfMonth else dia
        proximoAgendamento.set(Calendar.DAY_OF_MONTH, diaValido)

        if (proximoAgendamento.after(agora)) {
            return proximoAgendamento
        }

        proximoAgendamento.add(Calendar.MONTH, 1)
        val maxDayOfNextMonth = proximoAgendamento.getActualMaximum(Calendar.DAY_OF_MONTH)
        val diaValidoProximoMes = if (dia > maxDayOfNextMonth) maxDayOfNextMonth else dia
        proximoAgendamento.set(Calendar.DAY_OF_MONTH, diaValidoProximoMes)
        return proximoAgendamento
    }
}