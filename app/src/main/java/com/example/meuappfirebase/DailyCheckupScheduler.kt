package com.apol.myapplication // (Seu pacote)

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.*

object DailyCheckupScheduler {

    private const val TAG = "DailyCheckupScheduler"
    private const val REQUEST_CODE = 1000 // Um request code único para este alarme

    // Renomeamos de 'schedule' para 'scheduleNextCheckup'
    fun scheduleNextCheckup(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 1. Verificar permissão
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "Permissão de alarme exato não concedida. Não é possível agendar o checkup diário.")
                return
            }
        }

        // 2. Criar o Intent para o Receiver
        val intent = Intent(context, DailyCheckupReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Definir o horário: 16:00
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 16)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // 4. Se o horário já passou hoje, agenda para amanhã
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // 5. --- A CORREÇÃO ESTÁ AQUI ---
        // Trocamos setRepeating (inexato) por setExactAndAllowWhileIdle (exato)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
        // --- FIM DA CORREÇÃO ---

        Log.d(TAG, "Próximo checkup diário (16:00) agendado para: ${calendar.time}")
    }
}