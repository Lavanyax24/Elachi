package com.elachi.app.ui.recipe

import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    var ttsReady by remember { mutableStateOf(false) }

    val tts = remember(context) {
        TextToSpeech(context) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
        }
    }

    LaunchedEffect(currentStep, steps, ttsReady) {
        if (ttsReady) {
            tts.language = Locale.getDefault()
            if (steps.isNotEmpty() && currentStep in steps.indices) {
                tts.speak(
                    steps[currentStep],
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "step_$currentStep",
                )
            }
        }
    }

    LaunchedEffect(steps.size) {
        if (steps.isNotEmpty() && currentStep > steps.lastIndex) {
            currentStep = steps.lastIndex
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            countDownTimer?.cancel()
            tts.stop()
            tts.shutdown()
        }
    }

    fun startTimer(minutes: Int) {
        countDownTimer?.cancel()
        timerSecondsLeft = minutes * 60
        timerRunning = true
        countDownTimer = object : CountDownTimer(minutes * 60_000L, 1_000L) {
            override fun onTick(millisUntilFinished: Long) {
                timerSecondsLeft = (millisUntilFinished / 1_000L).toInt()
            }

            override fun onFinish() {
                timerRunning = false
                timerSecondsLeft = 0
            }
        }.start()
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize(),
    ) {
        if (steps.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("This recipe has no cooking steps yet.")
                Spacer(Modifier.height(16.dp))
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = onExit) { Text("Exit") }
                        Text(
                            "Step ${currentStep + 1} of ${steps.size}",
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Spacer(Modifier.width(48.dp))
                    }

                    Spacer(Modifier.height(24.dp))
                    Text(
                        steps[currentStep],
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        String.format(
                            "%02d:%02d",
                            timerSecondsLeft / 60,
                            timerSecondsLeft % 60,
                        ),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Spacer(Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1, 5, 10, 15).forEach { minutes ->
                            OutlinedButton(onClick = { startTimer(minutes) }) {
                                Text("${minutes}m")
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (!timerRunning) {
                                    startTimer(maxOf(timerSecondsLeft / 60, 1))
                                }
                            },
                        ) {
                            Text("Start")
                        }

                        OutlinedButton(
                            onClick = {
                                countDownTimer?.cancel()
                                timerRunning = false
                                timerSecondsLeft = 0
                            },
                        ) {
                            Text("Reset")
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { if (currentStep > 0) currentStep-- },
                        enabled = currentStep > 0,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("← Previous")
                    }

                    if (currentStep == steps.lastIndex) {
                        Button(
                            onClick = onFinish,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Finish Cooking!")
                        }
                    } else {
                        Button(
                            onClick = { currentStep++ },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Next →")
                        }
                    }
                }
            }
        }
    }
}