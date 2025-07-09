package com.cardomomo.walkairwear.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.*
import androidx.compose.ui.res.painterResource
import androidx.wear.compose.material.*
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

class MainActivity : ComponentActivity() {

    private var steps by mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ScalingLazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        Text(
                            text = "Pasos: $steps",
                            style = MaterialTheme.typography.title3
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun Entrenamiento(onFinish: (WearData) -> Unit) {
        var isRunning by remember { mutableStateOf(false) }
        var isPaused by remember { mutableStateOf(false) }

        val startTime = remember { mutableStateOf<LocalDateTime?>(null) }
        val endTime = remember { mutableStateOf<LocalDateTime?>(null) }

        var elapsedTime by remember { mutableStateOf(Duration.ZERO) }
        val coroutineScope = rememberCoroutineScope()

        var steps by remember { mutableIntStateOf(0) }
        var calories by remember { mutableFloatStateOf(0f) }
        var heartRate by remember { mutableFloatStateOf(70f) }

        // Simula datos en el emulador
        LaunchedEffect(isRunning, isPaused) {
            if (isRunning && !isPaused) {
                while (true) {
                    delay(1000L)
                    elapsedTime = elapsedTime.plusSeconds(1)
                    steps += 2
                    calories += 0.15f
                    heartRate = (70..130).random().toFloat() // puedes conectar sensor real
                }
            }
        }

        Scaffold(
            timeText = {
                TimeText()
            }
        ) {
            if (!isRunning) {
                StartWorkoutScreen {
                    startTime.value = LocalDateTime.now()
                    elapsedTime = Duration.ZERO
                    steps = 0
                    calories = 0f
                    heartRate = 70f
                    isRunning = true
                    isPaused = false
                }
            } else {
                ActiveWorkoutScreen(
                    elapsedTime = elapsedTime,
                    steps = steps,
                    calories = calories,
                    heartRate = heartRate,
                    isPaused = isPaused,
                    onPauseToggle = { isPaused = !isPaused },
                    onStop = {
                        endTime.value = LocalDateTime.now()
                        isRunning = false
                        onFinish(
                            WearData(
                                start = startTime.value!!,
                                ending = endTime.value!!,
                                steps = steps,
                                calories = calories,
                                heartRate = heartRate,
                                lastSync = LocalTime.now()
                            )
                        )
                    }
                )
            }
        }


}

    @Composable
    fun ActiveWorkoutScreen(
        elapsedTime: Duration,
        steps: Int,
        calories: Float,
        heartRate: Float,
        isPaused: Boolean,
        onPauseToggle: () -> Unit,
        onStop: () -> Unit
    ) {
        val minutes = elapsedTime.toMinutes()
        val seconds = elapsedTime.seconds % 60

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("⏱ ${"%02d:%02d".format(minutes, seconds)}")
            Text("❤️ ${heartRate.toInt()} bpm")
            Text("👣 $steps pasos")
            Text("🔥 ${String.format("%.1f", calories)} cal")

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onPauseToggle) {
                    Icon(
                        painter = if (isPaused) painterResource(R.drawable.outline_steps_24) else painterResource(R.drawable.outline_pause_24),
                        contentDescription = "Pausar/Reanudar"
                    )
                }

                Button(onClick = onStop) {
                    Icon(painter = painterResource(id = R.drawable.outline_stop_24), contentDescription = "Detener")
                }
            }
        }
    }
    @Composable
    fun StartWorkoutScreen(onStart: () -> Unit) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Listo para entrenar")
            Spacer(Modifier.height(8.dp))
            Button(onClick = onStart) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Iniciar")
            }
        }
    }