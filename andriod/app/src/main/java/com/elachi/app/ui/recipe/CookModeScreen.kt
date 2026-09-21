package com.elachi.app.ui.recipe

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

    val textToSpeech = remember(context) { TextToSpeech(context) { status -> ttsReady = status == TextToSpeech.SUCCESS } }

    LaunchedEffect(currentStep, steps, ttsReady) {
        if (ttsReady && steps.isNotEmpty() && currentStep in steps.indices) {
            textToSpeech.language = Locale.getDefault()
            textToSpeech.speak(steps[currentStep], TextToSpeech.QUEUE_FLUSH, null, "step_$currentStep")
        }
    }

    fun stopAlarm() { try { alarm?.stop() } catch (_: Exception) {} ; alarm = null }

    fun playAlarm() {
        stopAlarm(); textToSpeech.stop()
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, uri)
            if (ringtone != null) {
                ringtone.audioAttributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) ringtone.isLooping = true
                ringtone.play(); alarm = ringtone; return
            }
        } catch (_: Exception) {}
        try { ToneGenerator(AudioManager.STREAM_ALARM, 100).startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 3000) } catch (_: Exception) {}
    }

    LaunchedEffect(showTimerDoneDialog) { if (showTimerDoneDialog) { delay(30_000L); stopAlarm(); showTimerDoneDialog = false } }

    DisposableEffect(Unit) { onDispose { countDownTimer?.cancel(); stopAlarm(); textToSpeech.stop(); textToSpeech.shutdown() } }

    fun startTimer(seconds: Int) {
        if (seconds <= 0) return
        countDownTimer?.cancel(); timerSecondsLeft = seconds; timerRunning = true
        countDownTimer = object : CountDownTimer(seconds * 1000L, 1000L) {
            override fun onTick(ms: Long) { timerSecondsLeft = ((ms + 999L) / 1000L).toInt() }
            override fun onFinish() { timerRunning = false; timerSecondsLeft = 0; playAlarm(); showTimerDoneDialog = true }
        }.start()
    }

    fun pauseTimer() { countDownTimer?.cancel(); timerRunning = false }
    fun resetTimer() { countDownTimer?.cancel(); timerRunning = false; timerSecondsLeft = 0 }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (steps.isEmpty()) {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text("This recipe has no steps.", color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onExit) { Text("Go Back") }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = onExit) { Text("Exit", color = MaterialTheme.colorScheme.primary) }
                        Text("Step ${currentStep + 1} of ${steps.size}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(48.dp))
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(steps[currentStep], style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }

                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(String.format(Locale.getDefault(), "%02d:%02d", timerSecondsLeft / 60, timerSecondsLeft % 60), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1, 5, 10).forEach { m -> OutlinedButton(onClick = { startTimer(m * 60) }, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)) { Text("${m}m") } }
                        OutlinedButton(onClick = { showCustomTimerDialog = true }) { Text("Custom") }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { if (timerRunning) pauseTimer() else startTimer(timerSecondsLeft) }, enabled = timerRunning || timerSecondsLeft > 0, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                            Text(if (timerRunning) "Pause" else if (timerSecondsLeft > 0) "Resume" else "Start", color = Color.White)
                        }
                        OutlinedButton(onClick = { resetTimer() }) { Text("Reset") }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { if (currentStep > 0) currentStep-- }, enabled = currentStep > 0, modifier = Modifier.weight(1f)) { Text("Previous") }
                    if (currentStep == steps.lastIndex) {
                        Button(onClick = { finishing = true; pauseTimer(); textToSpeech.stop(); onFinish() }, enabled = !finishing, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                            Text(if (finishing) "Finishing…" else "Finish", color = Color.White)
                        }
                    } else {
                        Button(onClick = { currentStep++ }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text("Next", color = Color.White) }
                    }
                }
            }
        }
    }

    if (showCustomTimerDialog) {
        var min by remember { mutableStateOf("") }; var sec by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCustomTimerDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            title = { Text("Set Timer") },
            text = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = min, onValueChange = { min = it.filter { c -> c.isDigit() }.take(3) }, label = { Text("Min") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = sec, onValueChange = { sec = it.filter { c -> c.isDigit() }.take(2) }, label = { Text("Sec") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
            },
            confirmButton = { TextButton(onClick = { startTimer((min.toIntOrNull() ?: 0) * 60 + (sec.toIntOrNull() ?: 0)); showCustomTimerDialog = false }) { Text("Start") } },
            dismissButton = { TextButton(onClick = { showCustomTimerDialog = false }) { Text("Cancel") } }
        )
    }

    if (showTimerDoneDialog) {
        AlertDialog(
            onDismissRequest = { stopAlarm(); showTimerDoneDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Time's up!") },
            text = { Text("Your timer has finished.") },
            confirmButton = { Button(onClick = { stopAlarm(); showTimerDoneDialog = false }) { Text("Stop") } }
        )
    }
}
