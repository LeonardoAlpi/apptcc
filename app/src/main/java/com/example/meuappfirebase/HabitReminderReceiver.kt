
package com.apol.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.meuappfirebase.R

class HabitReminderReceiver : BroadcastReceiver() {

    private val TAG = "HABIT_DEBUG" // Nossa tag para filtrar as mensagens

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "--- HabitReminderReceiver ACORDOU! ---")

        val habitsPrefs = context.getSharedPreferences("habitos_prefs", Context.MODE_PRIVATE)
        val allHabitsString = habitsPrefs.getString("habits_list_ordered", null)

        Log.d(TAG, "Procurando lista de hábitos. Valor encontrado: '$allHabitsString'")

        if (allHabitsString.isNullOrEmpty()) {
            Log.d(TAG, "Lista de hábitos está vazia ou nula. Encerrando verificação.")
            return
        }

        val allHabits = allHabitsString.split(";;;")
        val todayKey = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        Log.d(TAG, "Hábitos a verificar: $allHabits. Chave do dia: $todayKey")

        val incompleteHabits = allHabits.filter { habit ->
            val progressKey = "${habit}_$todayKey"
            val progressValue = habitsPrefs.getInt(progressKey, 0)
            Log.d(TAG, "Verificando '$habit'. Chave de progresso: '$progressKey'. Progresso encontrado: $progressValue")
            progressValue == 0
        }

        Log.d(TAG, "Verificação concluída. Hábitos incompletos encontrados: $incompleteHabits")

        if (incompleteHabits.isNotEmpty()) {
            Log.d(TAG, "Encontrou hábitos incompletos. Preparando para enviar notificação...")
            val habitName = removerEmoji(incompleteHabits.first()).trim()
            val message = if (incompleteHabits.size > 1) {
                "Você ainda não completou '$habitName' e outros hábitos hoje."
            } else {
                "Lembrete: Você ainda não completou o hábito '$habitName' hoje."
            }
            sendNotification(context, "Não se esqueça dos seus hábitos!", message)
        } else {
            Log.d(TAG, "Nenhum hábito incompleto encontrado. Nenhuma notificação será enviada.")
        }
    }

    private fun sendNotification(context: Context, title: String, message: String) {
        // ... (o resto da função sendNotification permanece o mesmo)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "habit_reminder_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Lembretes de Hábitos", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification_icon) // Garanta que este ícone existe em res/drawable
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
        Log.d(TAG, "NOTIFICAÇÃO ENVIADA!")
    }

    private fun removerEmoji(texto: String): String {
        return texto.replaceFirst(Regex("^\\p{So}\\s*"), "")
    }
}