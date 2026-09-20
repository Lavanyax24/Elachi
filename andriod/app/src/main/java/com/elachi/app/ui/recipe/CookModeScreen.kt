package com.elachi.app.ui.recipe

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun CookModeScreen(
    steps: List<String>,
    onExit: () -> Unit,
    onFinish: () -> Unit,
) {
    val context = LocalContext.current

    var currentStep by remember { mutableIntStateOf(0) }
    var timerSecondsLeft by remember { mutableIntStateOf(0) }
    var timerRunning by remember { mutableStateOf(false) }
    var countDownTimer by remember { mutableStateOf<CountDownTimer?>(null) }
    var showCustomTimerDialog by remember { mutableStateOf(false) }
    var showTimerDoneDialog by remember { mutableStateOf(false) }
    var alarm by remember { mutableStateOf<Ringtone?>(null) }
    var ttsReady by remember { mutableStateOf(false) }
    var finishing by remember { mutableStateOf(false) }

    val textToSpeech = remember(context) {
        TextToSpeech(context) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
        }
    }

    LaunchedEffect(currentStep, steps, ttsReady) {
        if (ttsReady && steps.isNotEmpty() && currentStep in steps.indices) {
            textToSpeech.language = Locale.getDefault()
            textToSpeech.speak(
                steps[currentStep],
                TextToSpeech.QUEUE_FLUSH,
                null,
                "cook_mode_step_$currentStep",
            )
        }
    }

    LaunchedEffect(steps.size) {
        if (steps.isNotEmpty() && currentStep > steps.lastIndex) {
            currentStep = steps.lastIndex
        }
    }

    fun stopAlarm() {
        try {
            alarm?.stop()
        } catch (_: Exception) {
        }
        alarm = null
    }

    // Plays the phone's alarm sound. Falls back to a beep if no alarm sound is set.
    fun playAlarm() {
        stopAlarm()
        textToSpeech.stop() // don't talk over the alarm
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            val ringtone = RingtoneManager.getRingtone(context, uri)
            if (ringtone != null) {
                ringtone.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ringtone.isLooping = true
                }
                ringtone.play()
                alarm = ringtone
                return
            }
        } catch (e: Exception) {
            android.util.Log.e("CookMode", "Could not play alarm ringtone", e)
        }
        try {
            ToneGenerator(AudioManager.STREAM_ALARM, 100)
                .startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 3000)
        } catch (e: Exception) {
            android.util.Log.e("CookMode", "Could not play fallback beep", e)
        }
    }

    // Silence the alarm automatically after 30 seconds if nobody dismisses the dialog.
    LaunchedEffect(showTimerDoneDialog) {
        if (showTimerDoneDialog) {
            delay(30_000L)
            stopAlarm()
            showTimerDoneDialog = false
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            countDownTimer?.cancel()
            stopAlarm()
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    fun startTimer(totalSeconds: Int) {
        if (totalSeconds <= 0) return
        countDownTimer?.cancel()

        timerSecondsLeft = totalSeconds
        timerRunning = true

        countDownTimer = object : CountDownTimer(totalSeconds * 1_000L, 1_000L) {
            override fun onTick(millisUntilFinished: Long) {
                timerSecondsLeft = ((millisUntilFinished + 999L) / 1_000L).toInt()
            }

            override fun onFinish() {
                timerRunning = false
                timerSecondsLeft = 0
                playAlarm()
                showTimerDoneDialog = true
            }
        }.start()
    }

    fun pauseTimer() {
        countDownTimer?.cancel()
        timerRunning = false
    }

    fun resetTimer() {
        countDownTimer?.cancel()
        timerRunning = false
        timerSecondsLeft = 0
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        if (steps.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "This recipe has no cooking steps yet.",
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onExit) { Text("Go Back") }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        TextButton(onClick = onExit) { Text("Exit") }
                        Text(
                            text = "Step ${currentStep + 1} of ${steps.size}",
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Spacer(modifier = Modifier.width(48.dp))
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = steps[currentStep],
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = String.format(
                            Locale.getDefault(),
                            "%02d:%02d",
                            timerSecondsLeft / 60,
                            timerSecondsLeft % 60,
                        ),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick presets + custom
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1, 5, 10, 15).forEach { minutes ->
                            OutlinedButton(onClick = { startTimer(minutes * 60) }) {
                                Text("${minutes}m")
                            }
                        }
                        OutlinedButton(onClick = { showCustomTimerDialog = true }) {
                            Text("Custom")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            enabled = timerRunning || timerSecondsLeft > 0,
                            onClick = {
                                if (timerRunning) pauseTimer() else startTimer(timerSecondsLeft) // pause / resume
                            },
                        ) {
                            Text(
                                when {
                                    timerRunning -> "Pause"
                                    timerSecondsLeft > 0 -> "Resume"
                                    else -> "Start"
                                },
                            )
                        }

                        OutlinedButton(onClick = { resetTimer() }) { Text("Reset") }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = { if (currentStep > 0) currentStep-- },
                        enabled = currentStep > 0,
                        modifier = Modifier.weight(1f),
                    ) { Text("Previous") }

                    if (currentStep == steps.lastIndex) {
                        Button(
                            enabled = !finishing,
                            onClick = {
                                finishing = true
                                pauseTimer()
                                textToSpeech.stop()
                                onFinish()
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(if (finishing) "Finishing…" else "Finish Cooking")
                        }
                    } else {
                        Button(onClick = { currentStep++ }, modifier = Modifier.weight(1f)) {
                            Text("Next")
                        }
                    }
                }
            }
        }
    }

    // ---------- Custom timer dialog ----------
    if (showCustomTimerDialog) {
        var minutesText by remember { mutableStateOf("") }
        var secondsText by remember { mutableStateOf("") }
        val totalSeconds =
            (minutesText.toIntOrNull() ?: 0) * 60 + (secondsText.toIntOrNull() ?: 0).coerceAtMost(59)

        AlertDialog(
            onDismissRequest = { showCustomTimerDialog = false },
            title = { Text("Custom timer") },
            text = {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = minutesText,
                        onValueChange = { minutesText = it.filter(Char::isDigit).take(3) },
                        label = { Text("Minutes") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = secondsText,
                        onValueChange = { secondsText = it.filter(Char::isDigit).take(2) },
                        label = { Text("Seconds") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = totalSeconds > 0,
                    onClick = {
                        startTimer(totalSeconds)
                        showCustomTimerDialog = false
                    },
                ) { Text("Start") }
            },
            dismissButton = {
                TextButton(onClick = { showCustomTimerDialog = false }) { Text("Cancel") }
            },
        )
    }

    // ---------- Timer finished ----------
    if (showTimerDoneDialog) {
        AlertDialog(
            onDismissRequest = {
                stopAlarm()
                showTimerDoneDialog = false
            },
            title = { Text("Time's up!") },
            text = { Text("Your timer has finished.") },
            confirmButton = {
                Button(
                    onClick = {
                        stopAlarm()
                        showTimerDoneDialog = false
                    },
                ) { Text("Stop alarm") }
            },
        )
    }
}