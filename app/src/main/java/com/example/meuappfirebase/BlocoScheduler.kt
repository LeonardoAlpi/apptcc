package com.apol.myapplication // (use o package correto do seu app)

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.apol.myapplication.data.model.Bloco
import com.apol.myapplication.data.model.TipoLembrete
import java.util.*

/**
 * Objeto centralizado para agendar e cancelar todos os lembretes de blocos.
 * Usa setExactAndAllowWhileIdle para precisão em todos os alarmes.
 */
object BlocoScheduler {

    /**
     * Agenda o *próximo* lembrete para um bloco.
     * Esta função é "idempotente": ela sempre calcula a próxima ocorrência a partir de "agora".
     */
    fun agendarLembrete(context: Context, bloco: Bloco) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 1. Verifica a permissão (necessário para o ViewModel e o Receiver)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Log.w("BlocoScheduler", "Permissão para alarmes exatos não concedida.")
            return // Não pode fazer nada sem a permissão
        }

        // 2. Cancela quaisquer alarmes antigos para este bloco ANTES de agendar um novo.
        // Isso evita alarmes duplicados se as configurações mudarem.
        cancelarLembretes(context, bloco)

        // 3. Agenda o próximo alarme com base no tipo
        var requestCodeCounter = 0
        when (bloco.tipoLembrete) {
            TipoLembrete.DIARIO -> {
                bloco.horariosLembrete.forEach { horarioStr ->
                    val (hora, minuto) = parseHorario(horarioStr) ?: return@forEach
                    val calendar = getProximoAgendamentoDiario(hora, minuto)
                    val pendingIntent = getPendingIntent(context, bloco, bloco.id.hashCode() + requestCodeCounter++)

                    Log.d("BlocoScheduler", "Agendando DIÁRIO para Bloco ${bloco.nome} em: ${calendar.time}")
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                }
            }
            TipoLembrete.MENSAL -> {
                bloco.diasLembrete.forEach { dia ->
                    bloco.horariosLembrete.forEach { horarioStr ->
                        val (hora, minuto) = parseHorario(horarioStr) ?: return@forEach
                        getProximoAgendamentoMensal(dia, hora, minuto)?.let { calendar ->
                            val pendingIntent = getPendingIntent(context, bloco, bloco.id.hashCode() + requestCodeCounter++)

                            Log.d("BlocoScheduler", "Agendando MENSAL para Bloco ${bloco.nome} em: ${calendar.time}")
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                        }
                    }
                }
            }
            TipoLembrete.NENHUM -> {
                // Já foi cancelado acima, nada a fazer.
            }
            else -> {}
        }
    }

    /**
     * Cancela *todos* os possíveis PendingIntents para um bloco.
     */
    fun cancelarLembretes(context: Context, bloco: Bloco) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // Temos que tentar cancelar os mesmos requestCodes que poderíamos ter criado.
        // Iteramos por um número razoável para garantir.
        for (i in 0 until 50) { // 50 é um limite seguro para múltiplos horários/dias
            val requestCode = bloco.id.hashCode() + i
            val pendingIntent = getPendingIntent(context, bloco, requestCode)
            alarmManager.cancel(pendingIntent)
        }
        Log.d("BlocoScheduler", "Cancelando lembretes para Bloco: ${bloco.nome}")
    }

    // --- Funções Auxiliares (Movidas do seu ViewModel) ---

    private fun getPendingIntent(context: Context, bloco: Bloco, requestCode: Int): PendingIntent {
        val intent = Intent(context, BlocoNotificationReceiver::class.java).apply {
            action = "com.example.meuappfirebase.BLOCO_LEMBRETE" // Ação explícita
            putExtra("titulo", bloco.nome + if (bloco.subtitulo.isNotEmpty()) " - ${bloco.subtitulo}" else "")
            putExtra("mensagem", bloco.mensagemNotificacao.ifEmpty { "Você tem um lembrete para este bloco." })
            putExtra("bloco_id", bloco.id)
            // Passa o tipo para o receiver saber se deve reagendar
            putExtra("tipo_lembrete", bloco.tipoLembrete.name)
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

    private fun getProximoAgendamentoDiario(hora: Int, minuto: Int): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hora); set(Calendar.MINUTE, minuto); set(Calendar.SECOND, 0)
            // Se a hora já passou hoje, agenda para amanhã
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
    }

    private fun getProximoAgendamentoMensal(dia: Int, hora: Int, minuto: Int): Calendar? {
        val agora = Calendar.getInstance()
        val proximoAgendamento = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hora); set(Calendar.MINUTE, minuto); set(Calendar.SECOND, 0)
        }

        val maxDayOfMonth = proximoAgendamento.getActualMaximum(Calendar.DAY_OF_MONTH)
        val diaValido = if (dia > maxDayOfMonth) maxDayOfMonth else dia
        proximoAgendamento.set(Calendar.DAY_OF_MONTH, diaValido)

        // Se a data/hora for no futuro *este mês*, agenda
        if (proximoAgendamento.after(agora)) {
            return proximoAgendamento
        }

        // Se não, calcula para o próximo mês
        proximoAgendamento.add(Calendar.MONTH, 1)
        val maxDayOfNextMonth = proximoAgendamento.getActualMaximum(Calendar.DAY_OF_MONTH)
        val diaValidoProximoMes = if (dia > maxDayOfNextMonth) maxDayOfNextMonth else dia
        proximoAgendamento.set(Calendar.DAY_OF_MONTH, diaValidoProximoMes)
        return proximoAgendamento
    }
}