package com.apol.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.apol.myapplication.data.model.Habito
import com.example.meuappfirebase.HabitosActivity
import com.example.meuappfirebase.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HabitReminderReceiver : BroadcastReceiver() {

    private val TAG = "HabitReminderReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "==============================================")
        Log.d(TAG, "Alarme recebido! Iniciando verificação...")
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                verificarEnotificarHabitos(context)
            } finally {
                pendingResult.finish()
                Log.d(TAG, "Verificação finalizada.")
                Log.d(TAG, "==============================================")
            }
        }
    }

    private suspend fun verificarEnotificarHabitos(context: Context) {
        val usuario = Firebase.auth.currentUser
        if (usuario == null) {
            Log.e(TAG, "ERRO: Usuário nulo. O Receiver não pode continuar.")
            return
        }
        Log.d(TAG, "Usuário encontrado: ${usuario.uid}")

        val hojeStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val dao = AppDatabase.getDatabase(context).habitoDao()
        Log.d(TAG, "Data de hoje para verificação: $hojeStr")

        // 1. Pega TODOS os hábitos do usuário
        val todosHabitos: List<Habito> = dao.getHabitosByUser(usuario.uid)
        Log.d(TAG, "Passo 1: dao.getHabitosByUser retornou ${todosHabitos.size} hábito(s).")
        if (todosHabitos.isNotEmpty()) {
            Log.d(TAG, "Nomes dos hábitos encontrados: ${todosHabitos.joinToString { it.nome }}")
        }

        // 2. Pega os IDs de todos os hábitos já CONCLUÍDOS hoje
        val habitosConcluidosIds: Set<Long> = dao.getCompletedHabitIdsForDate(hojeStr).toSet()
        Log.d(TAG, "Passo 2: dao.getCompletedHabitIdsForDate retornou ${habitosConcluidosIds.size} ID(s) concluído(s) hoje.")

        // 3. Filtra para encontrar os hábitos PENDENTES
        val habitosPendentes = todosHabitos.filter { habito ->
            habito.id !in habitosConcluidosIds
        }
        Log.d(TAG, "Passo 3: Filtro resultou em ${habitosPendentes.size} hábito(s) pendente(s).")
        Log.d(TAG, "Nomes dos hábitos pendentes: ${habitosPendentes.joinToString { it.nome }}")


        if (habitosPendentes.isNotEmpty()) {
            val primeiroHabitoPendente = habitosPendentes.first()
            val nomeHabito = removerEmoji(primeiroHabitoPendente.nome).trim()

            val mensagem = if (habitosPendentes.size > 1) {
                "'$nomeHabito' e outros ${habitosPendentes.size - 1} hábitos estão esperando por você!"
            } else {
                "Não se esqueça de completar o hábito '$nomeHabito' hoje."
            }
            sendNotification(context, "Seus hábitos te aguardam!", mensagem)
        } else {
            Log.d(TAG, "Conclusão: Nenhum hábito pendente encontrado. Nenhuma notificação necessária.")
        }
    }

    private fun sendNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "habit_reminder_channel"

        // --- MUDANÇA AQUI: INTENÇÃO PARA ABRIR O APP AO TOCAR NA NOTIFICAÇÃO ---
        val intent = Intent(context, HabitosActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        // --- FIM DA MUDANÇA ---

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Lembretes de Hábitos", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification_icon) // Garanta que este ícone existe
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // <-- Linha adicionada para tornar a notificação clicável
            .build()

        // Usar um ID fixo (ex: 1) garante que uma notificação substitua a anterior
        notificationManager.notify(1, notification)
        Log.d(TAG, "Notificação enviada: $message")
    }

    private fun removerEmoji(texto: String): String {
        return texto.replaceFirst(Regex("^\\p{So}\\s*"), "")
    }
}