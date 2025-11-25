package com.apol.myapplication // (Ou o pacote que preferir)

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.meuappfirebase.HabitosActivity // (Importa sua tela)
import com.example.meuappfirebase.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class DailyCheckupReceiver : BroadcastReceiver() {

    private val TAG = "DailyCheckupReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Alarme das 16h recebido. Verificando hábitos...")
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val auth = Firebase.auth
                val user = auth.currentUser
                if (user == null) {
                    Log.d(TAG, "Usuário nulo, não é possível verificar.")
                    return@launch
                }

                val firestore = Firebase.firestore
                val hojeStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

                // 1. Buscar TODOS os hábitos do usuário no Firestore
                val habitSnapshot = firestore.collection("habitos")
                    .whereEqualTo("userOwnerId", user.uid)
                    .get()
                    .await() // Espera a consulta terminar

                var habitosPendentes = 0
                for (doc in habitSnapshot.documents) {
                    // Pega a lista de dias em que o hábito foi feito
                    val progresso = doc.get("progresso") as? List<String> ?: emptyList()

                    // 2. Verificar se o hábito NÃO foi feito hoje
                    if (!progresso.contains(hojeStr)) {
                        // Verifica se o hábito é para ser feito hoje
                        val diasProgramados = doc.get("diasProgramados") as? List<String> ?: emptyList()
                        val calendar = Calendar.getInstance()
                        val diaDaSemanaHoje = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                            Calendar.SUNDAY -> "SUN"
                            Calendar.MONDAY -> "MON"
                            Calendar.TUESDAY -> "TUE"
                            Calendar.WEDNESDAY -> "WED"
                            Calendar.THURSDAY -> "THU"
                            Calendar.FRIDAY -> "FRI"
                            Calendar.SATURDAY -> "SAT"
                            else -> ""
                        }

                        // Se a lista de dias está vazia (todos os dias) OU contém o dia de hoje
                        if (diasProgramados.isEmpty() || diasProgramados.contains(diaDaSemanaHoje)) {
                            habitosPendentes++
                        }
                    }
                }

                // 3. Se houver algum pendente, notificar
                if (habitosPendentes > 0) {
                    Log.d(TAG, "Encontrados $habitosPendentes hábitos pendentes. Enviando notificação.")
                    sendIncentiveNotification(context, habitosPendentes)
                } else {
                    Log.d(TAG, "Todos os hábitos concluídos. Nenhuma notificação necessária.")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Erro ao verificar hábitos", e)
            } finally {
                // --- ADICIONE ESTA LINHA ---
                // REAGENDA o alarme para o próximo dia (amanhã às 16h)
                DailyCheckupScheduler.scheduleNextCheckup(context)
                // --- FIM DA ADIÇÃO ---

                pendingResult.finish() // Conclui o receiver
            }
        }
    }

    private fun sendIncentiveNotification(context: Context, count: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "daily_checkup_channel" // Novo canal de notificação

        // Intent para abrir a tela de hábitos ao clicar
        val intent = Intent(context, HabitosActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 1001, intent, PendingIntent.FLAG_IMMUTABLE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Lembrete Diário de Hábitos", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val title = "Ainda dá tempo! 💪"
        val message = if (count == 1) "Você tem hábitos pendentes para hoje. Vamos lá! 💪"
        else "Você tem hábitos pendentes para hoje. Vamos lá! 💪"

        val notification = NotificationCompat.Builder(context, channelId)
            // Use um ícone seu. Estou pegando um do seu outro receiver
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // ID único para esta notificação
        notificationManager.notify(1001, notification)
    }
}