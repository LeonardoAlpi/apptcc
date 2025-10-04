package com.example.meuappfirebase

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.apol.myapplication.HabitReminderReceiver
import com.apol.myapplication.data.model.Habito
import java.util.*

class HabitReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun scheduleAllHabitReminders(habits: List<Habito>) {
        habits.forEach { habit ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }

            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            val intent = Intent(context, HabitReminderReceiver::class.java).apply {
                putExtra("HABIT_ID", habit.firestoreId)
                putExtra("HABIT_NAME", habit.nome)
            }

            // --- CORREÇÃO APLICADA AQUI ---
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                habit.firestoreId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // Corrigido de "Pending" para "PendingIntent"
            )

            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
            Log.d("HabitScheduler", "Alarme agendado para o hábito '${habit.nome}'")
        }
    }

    fun cancelAllReminders(habits: List<Habito>) {
        habits.forEach { habit ->
            val intent = Intent(context, HabitReminderReceiver::class.java)

            // --- CORREÇÃO APLICADA AQUI TAMBÉM ---
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                habit.firestoreId.hashCode(),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE // Corrigido para ser consistente
            )

            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                Log.d("HabitScheduler", "Alarme cancelado para o hábito '${habit.nome}'")
            }
        }
    }
}