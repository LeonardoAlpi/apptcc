package com.example.meuappfirebase

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
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
    private val firestore = Firebase.firestore // << NOVO: Referência correta para o Firestore
    private val notesDao = AppDatabase.getDatabase(application).notesDao()
    private val alarmManager = application.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // LiveData para as listas que a UI vai observar
    private val _notes = MutableLiveData<List<Note>>()
    val notes: LiveData<List<Note>> = _notes

    private val _blocos = MutableLiveData<List<Bloco>>()
    val blocos: LiveData<List<Bloco>> = _blocos

    // LiveData para mensagens de status
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
        viewModelScope.launch {
            notesDao.getBlocosByUser(user.uid).collect { _blocos.postValue(it) }
        }
    }

    // --- Métodos de Modificação (CRUD) ---

    fun addNote(text: String) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            notesDao.insertNote(Note(userOwnerId = user.uid, text = text))
        }
    }

    fun updateNote(note: Note) = viewModelScope.launch { notesDao.updateNote(note) }

    fun deleteNotes(notes: List<Note>) = viewModelScope.launch { notesDao.deleteNotes(notes) }

    fun addBloco(nome: String) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            val novoBloco = Bloco(userOwnerId = user.uid, nome = nome)
            // Insere no Room
            notesDao.insertBloco(novoBloco)
            // Sincroniza com o Firestore
            firestore.collection("users").document(user.uid).collection("blocos")
                .document(novoBloco.id).set(novoBloco)
        }
    }

    fun updateBloco(bloco: Bloco) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            // Atualiza no Room
            notesDao.updateBloco(bloco)
            // Sincroniza com o Firestore
            firestore.collection("users").document(user.uid).collection("blocos")
                .document(bloco.id).set(bloco)

            // Lógica de alarme
            cancelarLembretesParaBloco(bloco)
            if (bloco.tipoLembrete != TipoLembrete.NENHUM) {
                agendarLembretesParaBloco(bloco)
            }
        }
    }

    fun deleteBlocos(blocos: List<Bloco>) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            blocos.forEach { bloco ->
                cancelarLembretesParaBloco(bloco)
                // Deleta do Firestore
                firestore.collection("users").document(user.uid).collection("blocos")
                    .document(bloco.id).delete()
            }
            // Deleta do Room
            notesDao.deleteBlocos(blocos)
        }
    }

    // --- FUNÇÃO DE FAVORITAR CORRIGIDA ---

    fun toggleFavoritoBloco(bloco: Bloco) {
        val userId = auth.currentUser?.uid ?: run {
            _statusMessage.value = Event("Ação não permitida. Usuário não encontrado.")
            return
        }

        val blocosAtuais = _blocos.value ?: return
        val favoritosAtuais = blocosAtuais.count { it.isFavorito }

        if (!bloco.isFavorito && favoritosAtuais >= 3) {
            _statusMessage.value = Event("Você só pode favoritar até 3 blocos.")
            return
        }

        val blocoAtualizado = bloco.copy(isFavorito = !bloco.isFavorito)

        viewModelScope.launch {
            // 1. Atualiza o Room para feedback instantâneo na UI
            notesDao.updateBloco(blocoAtualizado)

            // 2. Sincroniza a mudança com o Firestore
            firestore.collection("users").document(userId).collection("blocos")
                .document(bloco.id)
                .update("favorito", blocoAtualizado.isFavorito) // Verifique se o nome do campo é "favorito" no seu Firestore
                .addOnFailureListener {
                    _statusMessage.value = Event("Erro ao sincronizar favorito.")
                    // Opcional: reverter a mudança no Room em caso de falha
                    viewModelScope.launch { notesDao.updateBloco(bloco) }
                }
        }
    }


    // --- LÓGICA DE ALARME E NOTIFICAÇÃO ---

    private fun agendarLembretesParaBloco(bloco: Bloco) {
        val context = getApplication<Application>().applicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            _statusMessage.postValue(Event("Permissão para agendar alarmes é necessária."))
            return
        }
        var requestCodeCounter = 0
        when (bloco.tipoLembrete) {
            TipoLembrete.DIARIO -> {
                if (bloco.horariosLembrete.isEmpty()) return
                bloco.horariosLembrete.forEach { horarioStr ->
                    val (hora, minuto) = parseHorario(horarioStr) ?: return@forEach
                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, hora)
                        set(Calendar.MINUTE, minuto)
                        set(Calendar.SECOND, 0)
                        if (before(Calendar.getInstance())) {
                            add(Calendar.DAY_OF_YEAR, 1)
                        }
                    }
                    val pendingIntent = getPendingIntent(context, bloco, bloco.id.hashCode() + requestCodeCounter++)
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
                }
                _statusMessage.postValue(Event("Lembrete(s) diário(s) agendado(s)!"))
            }
            TipoLembrete.MENSAL -> {
                if (bloco.diasLembrete.isEmpty() || bloco.horariosLembrete.isEmpty()) return
                bloco.diasLembrete.forEach { dia ->
                    bloco.horariosLembrete.forEach { horarioStr ->
                        val (hora, minuto) = parseHorario(horarioStr) ?: return@forEach
                        val proximoAgendamento = getNextMonthlyOccurrence(dia, hora, minuto)
                        proximoAgendamento?.let { calendar ->
                            val pendingIntent = getPendingIntent(context, bloco, bloco.id.hashCode() + requestCodeCounter++)
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                        }
                    }
                }
                _statusMessage.postValue(Event("Lembrete(s) mensal(is) agendado(s)!"))
            }
            else -> {} // Nenhum ou Teste
        }
    }

    private fun cancelarLembretesParaBloco(bloco: Bloco) {
        val context = getApplication<Application>().applicationContext
        for (i in 0 until 100) { // Um número arbitrário para garantir que todos os p.i. sejam cancelados
            val requestCode = bloco.id.hashCode() + i
            val pendingIntent = getPendingIntent(context, bloco, requestCode, cancel = true)
            alarmManager.cancel(pendingIntent)
        }
    }

    private fun getPendingIntent(context: Context, bloco: Bloco, requestCode: Int, cancel: Boolean = false): PendingIntent {
        val intent = Intent(context, BlocoNotificationReceiver::class.java).apply {
            if (!cancel) {
                putExtra("titulo", bloco.nome + if (bloco.subtitulo.isNotEmpty()) " - ${bloco.subtitulo}" else "")
                putExtra("mensagem", bloco.mensagemNotificacao.ifEmpty { "Você tem um lembrete para este bloco." })
                putExtra("bloco_id", bloco.id)
            }
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
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, minuto)
            set(Calendar.SECOND, 0)
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