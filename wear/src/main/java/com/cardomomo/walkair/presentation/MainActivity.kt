package com.cardomomo.walkair.presentation

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.wear.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material3.MaterialTheme
import com.cardomomo.walkair.presentation.theme.WalkAirWearTheme
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.cardomomo.walkair.R
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.math.sqrt

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var heartRateSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null

    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.BODY_SENSORS,
        Manifest.permission.ACTIVITY_RECOGNITION
    )
    private val PERMISSION_REQUEST_CODE = 100

    private var stepCount = 0
    private var previousMagnitude = 0.0
    private var threshold = 6.0

    private var _heartRate = mutableStateOf(0f)
    private var _steps = mutableStateOf(0)

    private var _calories = mutableStateOf(0f)

    private var startedFromNotification = false
    private val shouldStartWorkout = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startedFromNotification = intent.getBooleanExtra("start_training", false)
        val startTraining = intent.getBooleanExtra("start_training", false)
        // Sensores
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

        requestPermissionsIfNeeded()
        requestNotificationPermissionIfNeeded()

        // Mensajes desde el teléfono
        Wearable.getMessageClient(this).addListener { messageEvent ->
            if (messageEvent.path == "/start_workout") {
                Log.d("WearTrigger", "Mensaje recibido desde el teléfono: iniciar entrenamiento")

                // Lanzar trigger para UI
                shouldStartWorkout.value = true
            }
        }
        setContent {
            WalkAirWearTheme {
                Entrenamiento(
                    steps = _steps.value,
                    heartRate = _heartRate.value,
                    onReset = {
                        _steps.value = 0
                        stepCount = 0
                    },
                    onFinish = { data ->
                        sendToMobileApp(data)
                        shouldStartWorkout.value = false
                    },
                    startImmediately = startedFromNotification
                )
            }
        }
    }

    @Composable
    fun Entrenamiento(
        steps: Int,
        heartRate: Float,
        onReset: () -> Unit,
        onFinish: (WearData) -> Unit,
        startImmediately: Boolean = false
    ) {
        var isRunning by remember { mutableStateOf(false) }
        var isPaused by remember { mutableStateOf(false) }
        var startTime by remember { mutableStateOf<LocalDateTime?>(null) }
        var elapsed by remember { mutableStateOf(Duration.ZERO) }
        val heartRates = remember { mutableStateListOf<Float>() }
        var calories by remember { mutableStateOf(0f) }
        val currentSteps by rememberUpdatedState(steps)
        val currentHeartRate by rememberUpdatedState(heartRate)

        LaunchedEffect(startImmediately) {
            if (startImmediately) {
                isRunning = true
                isPaused = false
            }
        }


        LaunchedEffect(isRunning, isPaused) { // Only restart if isRunning or isPaused changes

            if (isRunning && !isPaused) {
                launch {
                    // snapshotFlow now reads the 'currentSteps' and 'currentHeartRate' State objects.
                    // It will emit a new pair whenever the VALUE INSIDE these State objects changes.
                    snapshotFlow { currentSteps to currentHeartRate }
                        .collect { (updatedSteps, updatedHR) ->
                            // updatedSteps and updatedHR are the actual latest values
                            calories = updatedSteps * 0.04f
                            if (updatedHR > 0) heartRates.add(updatedHR)
                            Log.d("CaloriesDebug", "snapshotFlow collected: $calories, Steps: $updatedSteps, HR: $updatedHR")
                        }
                }

                launch {
                    while (true) {
                        delay(1000)
                        elapsed = elapsed.plusSeconds(1)
                    }
                }
            }
        }


        Scaffold(timeText = { TimeText() }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (!isRunning) {
                    StartWorkoutScreen {
                        isRunning = true
                        isPaused = false
                        startTime = LocalDateTime.now()
                        elapsed = Duration.ZERO
                        calories = 0f
                        onReset()
                    }
                } else {
                    ActiveWorkoutScreen(
                        elapsedTime = elapsed,
                        steps = steps,
                        calories = calories,
                        heartRate = heartRate,
                        isPaused = isPaused,
                        onPauseToggle = { isPaused = !isPaused },
                        onStop = {
                            val endTime = LocalDateTime.now()
                            isRunning = false
                            val avgHR = if (heartRates.isNotEmpty()) heartRates.average().toFloat() else heartRate
                            val data = WearData(
                                start = startTime ?: endTime,
                                ending = endTime,
                                steps = steps,
                                calories = calories,
                                heartRate = avgHR,
                                lastSync = LocalTime.now()
                            )
                            onFinish(data)
                        }
                    )
                }
            }
        }
    }

    fun endWorkout() {
        // Cierra MainActivity y vuelve a WaitingActivity
        val intent = Intent(this, WaitingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    @Composable
    fun StartWorkoutScreen(onStart: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Bienvenido a WalkAir",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "¿Listo para entrenar?",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onStart) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_play_arrow_24),
                    contentDescription = "Iniciar"
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
                .padding(4.dp)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("⏱ ${"%02d:%02d".format(minutes, seconds)}", style = MaterialTheme.typography.titleLarge)
            Text("❤️ ${heartRate.toInt()} bpm")
            Text("👣 $steps pasos")
            Text("🔥 ${String.format("%.1f", calories)} cal")

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = onPauseToggle) {
                    Icon(
                        painter = painterResource(
                            if (isPaused) R.drawable.outline_play_arrow_24 else R.drawable.outline_pause_24
                        ),
                        contentDescription = "Pausar/Reanudar"
                    )
                }

                Button(onClick = onStop) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_stop_24),
                        contentDescription = "Detener"
                    )
                }
            }
        }
    }

    private fun sendToMobileApp(data: WearData) {
        val dataMap = PutDataMapRequest.create("/training_data").apply {
            dataMap.putString("start", data.start.toString())
            dataMap.putString("ending", data.ending.toString())
            dataMap.putInt("steps", data.steps)
            dataMap.putFloat("calories", data.calories)
            dataMap.putFloat("heartRate", data.heartRate)
            dataMap.putString("lastSync", data.lastSync.toString())
        }
        val request = dataMap.asPutDataRequest().setUrgent()
        Wearable.getDataClient(this).putDataItem(request)
            .addOnSuccessListener {
                Log.d("WearSync", "Datos enviados correctamente")
            }
            .addOnFailureListener {
                Log.e("WearSync", "Error al enviar datos", it)
            }
    }

    @Composable
    fun WaitForTriggerScreen() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text("Esperando entrenamiento...", style = MaterialTheme.typography.bodyLarge)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    val x = it.values[0]
                    val y = it.values[1]
                    val z = it.values[2]
                    val magnitude = sqrt((x * x + y * y + z * z).toDouble())
                    val delta = magnitude - previousMagnitude
                    previousMagnitude = magnitude
                    if (delta > threshold) {
                        stepCount++
                        _steps.value = stepCount
                        Log.d("SensorDebug", "Accelerometer: New _steps.value: ${_steps.value}") // ADD LOG
                    }
                }
                Sensor.TYPE_HEART_RATE -> {
                    _heartRate.value = it.values[0]
                    Log.d("SensorDebug", "Heart Rate: New _heartRate.value: ${_heartRate.value}") // ADD LOG
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun requestPermissionsIfNeeded() {
        if (!hasAllPermissions()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE)
        } else {
            registerSensors()
        }
    }

    private fun hasAllPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun registerSensors() {
        accelerometerSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        heartRateSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE &&
            grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            registerSensors()
        } else {
            Toast.makeText(this, "Se necesitan permisos para continuar", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }


    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("Permission", "Permiso de notificación concedido")
        } else {
            Toast.makeText(this, "Permiso de notificación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    // En la actividad
    private val startWorkoutReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            shouldStartWorkout.value = true
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(startWorkoutReceiver, IntentFilter("com.cardomomo.walkair.START_WORKOUT"))
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(startWorkoutReceiver)
    }
}
