package com.apol.myapplication

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.Calendar

object HabitAlarmScheduler {

    // Tag para encontrarmos fácil no Logcat
    private const val TAG = "HabitAlarmScheduler"

    fun scheduleDailyReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // VERIFICAÇÃO DE PERMISSÃO (MUITO IMPORTANTE!)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.e(TAG, "!!! PERMISSÃO 'ALARMES E LEMBRETES' NÃO CONCEDIDA. O ALARME NÃO PODE SER AGENDADO. !!!")
                // Em um app de produção, aqui você pediria ao usuário para ir às configurações.
                return
            }
        }

        val intent = Intent(context, HabitReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0, // Request code
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // --- LÓGICA DE TESTE: Agendar para 2 minutos no futuro ---
        val triggerTime = System.currentTimeMillis() + (2 * 60 * 1000) // Agora + 2 minutos
        val triggerTimeCalendar = Calendar.getInstance().apply { timeInMillis = triggerTime }
        val timeStr = java.text.SimpleDateFormat("HH:mm:ss").format(triggerTimeCalendar.time)

        // Log detalhado para sabermos exatamente o que está acontecendo
        Log.d(TAG, "------------------------------------------")
        Log.d(TAG, "Tentando agendar o alarme de teste...")
        Log.d(TAG, "Horário do agendamento: $timeStr")
        Log.d(TAG, "------------------------------------------")

        // Usando setExactAndAllowWhileIdle que é mais robusto contra o modo Doze
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }
}