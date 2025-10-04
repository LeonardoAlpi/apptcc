package com.apol.myapplication // Mantendo seu pacote original

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.meuappfirebase.HabitosActivity // Importe sua Activity de Hábitos
import com.example.meuappfirebase.R

class HabitReminderReceiver : BroadcastReceiver() {

    private val TAG = "HabitReminderReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        // --- LÓGICA SIMPLIFICADA ---
        // Apenas pegamos os dados que o alarme nos enviou.
        val habitId = intent.getStringExtra("HABIT_ID")
        val habitName = intent.getStringExtra("HABIT_NAME")

        if (habitId == null || habitName == null) {
            Log.e(TAG, "Alarme recebido sem os dados do hábito.")
            return
        }

        Log.d(TAG, "Alarme recebido para o hábito: $habitName")
        val nomeLimpo = removerEmoji(habitName).trim()

        sendNotification(
            context,
            "Hora do Hábito!",
            "Não se esqueça de completar: $nomeLimpo",
            habitId
        )
    }

    private fun sendNotification(context: Context, title: String, message: String, habitId: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "habit_reminder_channel"

        // Intent para abrir a tela de hábitos ao clicar
        val intent = Intent(context, HabitosActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, habitId.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Lembretes de Hábitos", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // Usamos o hashCode do ID do hábito para que cada notificação seja única
        notificationManager.notify(habitId.hashCode(), notification)
        Log.d(TAG, "Notificação enviada para o hábito ID: $habitId")
    }

    private fun removerEmoji(texto: String): String {
        return texto.replaceFirst(Regex("^\\p{So}\\s*"), "")
    }
}