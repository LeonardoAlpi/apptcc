package com.apol.myapplication // (use o package correto)

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.apol.myapplication.data.model.TipoLembrete
import com.example.meuappfirebase.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BlocoNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // 1. Pega os dados do Intent
        val titulo = intent.getStringExtra("titulo") ?: "Lembrete"
        val mensagem = intent.getStringExtra("mensagem") ?: "Você tem um novo lembrete."
        val blocoId = intent.getStringExtra("bloco_id")
        val tipoLembreteStr = intent.getStringExtra("tipo_lembrete")

        // 2. Exibe a notificação
        val notification = NotificationCompat.Builder(context, "canal_lembrete")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(titulo)
            .setContentText(mensagem)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = NotificationManagerCompat.from(context)
        // Usa um ID único baseado no tempo para não sobrepor notificações
        manager.notify(System.currentTimeMillis().toInt(), notification)

        // 3. REAGENDAR o próximo alarme
        if (blocoId != null && tipoLembreteStr != null) {
            val tipoLembrete = try {
                TipoLembrete.valueOf(tipoLembreteStr)
            } catch (e: Exception) {
                TipoLembrete.NENHUM
            }

            // Só precisamos reagendar se for um alarme repetitivo (Diário ou Mensal)
            if (tipoLembrete == TipoLembrete.DIARIO || tipoLembrete == TipoLembrete.MENSAL) {

                // Usamos goAsync para manter o processo vivo enquanto
                // acessamos o banco de dados em uma Coroutine.
                val pendingResult = goAsync()

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // Busca o bloco no banco de dados
                        val dao = AppDatabase.getDatabase(context).notesDao()
                        val bloco = dao.getBlocoById(blocoId)

                        // Se o bloco ainda existe E seu lembrete não foi desativado...
                        if (bloco != null && bloco.tipoLembrete == tipoLembrete) {
                            // ...chama o Scheduler para agendar a *próxima* ocorrência!
                            BlocoScheduler.agendarLembrete(context, bloco)
                        }
                    } finally {
                        // Informa ao sistema que terminamos
                        pendingResult.finish()
                    }
                }
            }
        }
    }
}