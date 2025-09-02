package com.example.meuappfirebase // Pacote corrigido

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.meuappfirebase.databinding.ActivityCronometroBinding
import java.util.*
import java.util.concurrent.TimeUnit

class CronometroActivity : AppCompatActivity() {

    // --- VARIÁVEIS DE ESTADO DO CRONÔMETRO ---
    private var timer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 0
    private var timerDuration: Long = 0L
    private var isTimerRunning = false

    // --- VIEW BINDING ---
    // Acesso seguro e direto aos componentes do XML.
    private lateinit var binding: ActivityCronometroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Infla o layout usando ViewBinding.
        binding = ActivityCronometroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configura os cliques dos botões.
        setupListeners()

        // Inicia o cronômetro no estado zerado.
        resetTimer()

        // Configura a barra de navegação.
        configurarNavBar()
    }

    override fun onBackPressed() {
        // Mantém sua lógica de fechar o app ao pressionar voltar nesta tela.
        finishAffinity()
    }

    private fun setupListeners() {
        binding.tvTimer.setOnClickListener {
            if (!isTimerRunning) {
                mostrarDialogoDefinirTempo()
            }
        }

        binding.btnStartPause.setOnClickListener {
            if (isTimerRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        binding.btnReset.setOnClickListener {
            resetTimer()
        }
    }

    private fun mostrarDialogoDefinirTempo() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_set_time, null)
        val dialog = AlertDialog.Builder(this, R.style.Theme_HAM_Dialog_Transparent)
            .setView(dialogView)
            .create()

        val pickerHours = dialogView.findViewById<NumberPicker>(R.id.picker_hours)
        val pickerMinutes = dialogView.findViewById<NumberPicker>(R.id.picker_minutes)
        val pickerSeconds = dialogView.findViewById<NumberPicker>(R.id.picker_seconds)
        val btnOk = dialogView.findViewById<Button>(R.id.btn_ok_tempo)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btn_cancelar_tempo)

        // Configurações do NumberPicker
        pickerHours.minValue = 0; pickerHours.maxValue = 23
        pickerMinutes.minValue = 0; pickerMinutes.maxValue = 59
        pickerSeconds.minValue = 0; pickerSeconds.maxValue = 59
        pickerMinutes.setFormatter { i -> String.format("%02d", i) }
        pickerSeconds.setFormatter { i -> String.format("%02d", i) }

        // Preenche com o tempo atual
        val currentHours = TimeUnit.MILLISECONDS.toHours(timerDuration)
        val currentMinutes = TimeUnit.MILLISECONDS.toMinutes(timerDuration) - TimeUnit.HOURS.toMinutes(currentHours)
        val currentSeconds = TimeUnit.MILLISECONDS.toSeconds(timerDuration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timerDuration))
        pickerHours.value = currentHours.toInt()
        pickerMinutes.value = currentMinutes.toInt()
        pickerSeconds.value = currentSeconds.toInt()

        btnOk.setOnClickListener {
            val hours = pickerHours.value
            val minutes = pickerMinutes.value
            val seconds = pickerSeconds.value
            timerDuration = (hours * 3600000L) + (minutes * 60000L) + (seconds * 1000L)
            resetTimer()
            dialog.dismiss()
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun startTimer() {
        if (timeLeftInMillis <= 0L) {
            Toast.makeText(this, "Defina um tempo para iniciar.", Toast.LENGTH_SHORT).show()
            return
        }

        timer = object : CountDownTimer(timeLeftInMillis, 50) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerDisplay()
                updateProgressBar(false)
            }
            override fun onFinish() {
                timeLeftInMillis = 0
                isTimerRunning = false
                updateTimerDisplay()
                updateProgressBar(true)
                updateUI()
                Toast.makeText(this@CronometroActivity, "Tempo finalizado!", Toast.LENGTH_SHORT).show()
            }
        }.start()

        isTimerRunning = true
        updateUI()
    }

    private fun pauseTimer() {
        timer?.cancel()
        isTimerRunning = false
        updateUI()
    }

    private fun resetTimer() {
        timer?.cancel()
        isTimerRunning = false
        timeLeftInMillis = timerDuration
        updateTimerDisplay()
        updateProgressBar(true)
        updateUI()
    }

    private fun updateTimerDisplay() {
        val hours = TimeUnit.MILLISECONDS.toHours(timeLeftInMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeLeftInMillis) - TimeUnit.HOURS.toMinutes(hours)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeLeftInMillis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timerDuration))

        val timeString = if (timerDuration >= 3600000L) {
            String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
        binding.tvTimer.text = timeString
    }

    private fun updateProgressBar(instant: Boolean) {
        val progress = if (timerDuration > 0) {
            (timeLeftInMillis.toDouble() / timerDuration.toDouble() * 100).toInt()
        } else {
            0
        }

        if (instant) {
            binding.progressBar.progress = progress
        } else {
            ObjectAnimator.ofInt(binding.progressBar, "progress", progress).apply {
                duration = 150
                interpolator = LinearInterpolator()
                start()
            }
        }
        binding.progressBar.visibility = if (timeLeftInMillis > 0) View.VISIBLE else View.INVISIBLE
    }

    private fun updateUI() {
        binding.btnStartPause.text = if (isTimerRunning) "Pausar" else "Iniciar"
    }

    private fun configurarNavBar() {
        // Assumindo que o ID do seu include da barra de navegação no XML é "navigation_bar"
        val navBar = binding.navigationBar
        navBar.botaoInicio.setOnClickListener {
            startActivity(Intent(this, Bemvindouser::class.java))
        }
        navBar.botaoAnotacoes.setOnClickListener {
            startActivity(Intent(this, anotacoes::class.java))
        }
        navBar.botaoHabitos.setOnClickListener {
            startActivity(Intent(this, HabitosActivity::class.java))
        }
        navBar.botaoTreinos.setOnClickListener {
            startActivity(Intent(this, treinos::class.java))
        }
        navBar.botaoCronometro.setOnClickListener {
            // Já está aqui
        }
        navBar.botaoSugestoes.setOnClickListener {
            startActivity(Intent(this, SugestaoUser::class.java))
        }
    }
}