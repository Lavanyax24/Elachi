package com.elachi.app.ui.recipe

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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun CookModeScreen(
    steps: List<String>,
    onExit: () -> Unit,
    onFinish: () -> Unit,
) {
    val context = LocalContext.current

    var currentStep by remember {
        mutableIntStateOf(0)
    }

    var timerSecondsLeft by remember {
        mutableIntStateOf(0)
    }

    var timerRunning by remember {
        mutableStateOf(false)
    }

    var countDownTimer by remember {
        mutableStateOf<CountDownTimer?>(null)
    }

    var ttsReady by remember {
        mutableStateOf(false)
    }

    val textToSpeech = remember(context) {
        TextToSpeech(context) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
        }
    }

    LaunchedEffect(currentStep, steps, ttsReady) {
        if (
            ttsReady &&
            steps.isNotEmpty() &&
            currentStep in steps.indices
        ) {
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
        if (
            steps.isNotEmpty() &&
            currentStep > steps.lastIndex
        ) {
            currentStep = steps.lastIndex
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            countDownTimer?.cancel()
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    fun startTimer(minutes: Int) {
        countDownTimer?.cancel()

        timerSecondsLeft = minutes * 60
        timerRunning = true

        countDownTimer = object : CountDownTimer(
            minutes * 60_000L,
            1_000L,
        ) {
            override fun onTick(millisUntilFinished: Long) {
                timerSecondsLeft =
                    (millisUntilFinished / 1_000L).toInt()
            }

            override fun onFinish() {
                timerRunning = false
                timerSecondsLeft = 0
            }
        }.start()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        if (steps.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "This recipe has no cooking steps yet.",
                    style = MaterialTheme.typography.titleMedium,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = onExit) {
                    Text("Go Back")
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement =
                            Arrangement.SpaceBetween,
                    ) {
                        TextButton(onClick = onExit) {
                            Text("Exit")
                        }

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
                        style = MaterialTheme.typography.headlineMedium,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp),
                    ) {
                        listOf(1, 5, 10, 15).forEach { minutes ->
                            OutlinedButton(
                                onClick = {
                                    startTimer(minutes)
                                },
                            ) {
                                Text("${minutes}m")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = {
                                if (!timerRunning) {
                                    val minutes =
                                        maxOf(timerSecondsLeft / 60, 1)

                                    startTimer(minutes)
                                }
                            },
                        ) {
                            Text(
                                if (timerRunning) {
                                    "Running"
                                } else {
                                    "Start"
                                },
                            )
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = {
                            if (currentStep > 0) {
                                currentStep--
                            }
                        },
                        enabled = currentStep > 0,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Previous")
                    }

                    if (currentStep == steps.lastIndex) {
                        Button(
                            onClick = onFinish,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Finish Cooking")
                        }
                    } else {
                        Button(
                            onClick = {
                                currentStep++
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Next")
                        }
                    }
                }
            }
        }
    }
}