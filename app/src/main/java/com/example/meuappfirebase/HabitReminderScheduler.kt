package com.example.meuappfirebase

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.apol.myapplication.HabitReminderReceiver
import com.apol.myapplication.data.model.Habito // IMPORTANTE: Verifique se este é o caminho para sua entidade Habito do Room
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
            // Horário fixo para o lembrete (ex: 9 da manhã)
            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }

            // Se o horário já passou hoje, agenda para o dia seguinte
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            val intent = Intent(context, HabitReminderReceiver::class.java)
            // A CHAVE EXTRA QUE ESTAVA AQUI FOI REMOVIDA

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                habit.id.hashCode(), // Usamos o hashCode do ID para ter um código de requisição único
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Agenda um alarme que se repete todos os dias
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
            Log.d("HabitScheduler", "Alarme agendado para o hábito '${habit.nome}'")
        } // A chave que fecha o forEach está aqui, no lugar certo.
    }

    fun cancelAllReminders(habits: List<Habito>) {
        habits.forEach { habit ->
            val intent = Intent(context, HabitReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                habit.id.hashCode(),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )

            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                Log.d("HabitScheduler", "Alarme cancelado para o hábito '${habit.nome}'")
            }
        }
    }
}