package com.cardomomo.walkair

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.view.WindowCompat
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.cardomomo.walkair.ui.theme.*
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.tasks.await




class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val context = LocalContext.current
            val darkMode by SettingsDataStore.darkModeFlow(context).collectAsState(initial = false)

            LaunchedEffect(darkMode) {
                Log.d("ThemeDebug", "darkMode = $darkMode")
                WindowCompat.getInsetsController(window, window.decorView)
                    ?.isAppearanceLightStatusBars = !darkMode
            }

            AppTheme(useDarkTheme = darkMode) {
                MainScreen()
            }
        }
    }

    @Composable
    fun MainScreen() {
        val navController = rememberNavController()
        val screens = listOf(Screen.Home, Screen.Registro, Screen.Ajustes)
        val context = LocalContext.current

        Scaffold(
            bottomBar = {
                NavigationBar  {
                    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
                    screens.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding  ->
            NavHost(navController, startDestination = Screen.Home.route, Modifier.padding(innerPadding )) {
                composable(Screen.Home.route) { Inicio() }
                composable(Screen.Registro.route) { Registro() }
                composable(Screen.Ajustes.route) { Ajustes() }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Inicio(viewModel: WearViewModel = viewModel()) {
        val context = LocalContext.current


        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

        // Leer los valores del DataStore
        val username by SettingsDataStore.usernameFlow(context).collectAsState(initial = "Usuario")
        val stepGoal by SettingsDataStore.stepGoalFlow(context).collectAsState(initial = 6000)
        val caloriesGoal by SettingsDataStore.caloriesGoalFlow(context).collectAsState(initial = 300f)

        // Leer los entrenamientos de SQLite
        val trainings by viewModel.getAll.collectAsState(initial = emptyList())

        // Calcular agregados
        val totalSteps = trainings.sumOf { it.steps }
        val totalCalories = trainings.sumOf { it.calories.toInt() }
        val averageHeartRate = if (trainings.isNotEmpty()) {
            trainings.map { it.heartRate }.average().toInt()
        } else 0


        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LargeTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = {
                        Text(
                            "Bienvenido $username, ¡Vamos a Caminar!",
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    scrollBehavior = scrollBehavior
                )
            }
        ) { innerPadding ->
            HomeContent(
                padding = innerPadding,
                stepGoal = stepGoal,
                caloriesGoal = caloriesGoal.toInt(),
                totalSteps = totalSteps,
                totalCalories = totalCalories,
                avgHeartRate = averageHeartRate
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Registro() {

        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

            topBar = {
                LargeTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = {
                        Text(
                            "Registro de Entrenamientos",
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    scrollBehavior = scrollBehavior
                )
            },
        ) { innerPadding ->
            RegistroContent(x0 = innerPadding, viewModel = viewModel())
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Ajustes(){
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

            topBar = {
                LargeTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = {
                        Text(
                            "Ajustes",
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    scrollBehavior = scrollBehavior
                )
            },
        ) { innerPadding ->
            AjustesContent(innerPadding)
        }
    }

    sealed class Screen(val route: String, val icon: ImageVector, val label: String){
        object Home : Screen("home", Icons.Filled.Home, "Inicio")
        object Registro : Screen("registro", Icons.Filled.Menu, "Registro")
        object Ajustes : Screen("ajustes", Icons.Filled.Settings, "Ajustes")
    }

    @Composable
    private fun HomeContent(
        padding: PaddingValues,
        stepGoal: Int,
        caloriesGoal: Int,
        totalSteps: Int,
        totalCalories: Int,
        avgHeartRate: Int
    ) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            InfoCard("Pasos", R.drawable.outline_steps_24, totalSteps, "pasos", stepGoal, true)
            InfoCard("Calorías", R.drawable.baseline_local_fire_department_24, totalCalories, "cal", caloriesGoal, true)
            InfoCard("Frecuencia Cardiaca Promedio", R.drawable.outline_favorite_24, avgHeartRate, "bpm", 0, false)

            Button(onClick = {
                scope.launch {
                    try {
                        val messageClient = Wearable.getMessageClient(context)
                        val nodes = Wearable.getNodeClient(context).connectedNodes.await()
                        for (node in nodes) {
                            messageClient.sendMessage(node.id, "/start_workout", null).await()
                            Log.d("PhoneToWear", "Mensaje enviado a nodo ${node.displayName}")
                        }
                    } catch (e: Exception) {
                        Log.e("PhoneToWear", "Error al enviar mensaje: ${e.message}")
                    }
                }
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_play_arrow_24),
                    contentDescription = "Iniciar entrenamiento"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Iniciar un Entrenamiento")
            }
        }
    }

    @Composable
    private fun ProgBar(now: Int, goal: Int) {
        val progress = (now.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
        val animatedProgress by animateFloatAsState(
            targetValue = progress,
            animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        )

        Column(
            modifier = Modifier.fillMaxWidth(.6f),
            horizontalAlignment = Alignment.Start
        ) {
            LinearProgressIndicator(
            progress = { animatedProgress }, color = ProgressIndicatorDefaults.linearColor,
            trackColor = ProgressIndicatorDefaults.linearTrackColor,
            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )
            Spacer(Modifier.requiredHeight(10.dp))
            Text("Meta: $goal")
        }
    }
    @Composable
    private fun InfoCard(category: String, icon: Int, value: Int, unit: String, goal: Int, progress: Boolean){
        ElevatedCard(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Row {
                Icon(
                    painter = painterResource(icon),
                    modifier = Modifier.padding(12.dp, 12.dp),
                    contentDescription = "Steps"
                )
                Text(
                    category,
                    modifier = Modifier.padding(6.dp),
                    style = MaterialTheme.typography.headlineMedium)

            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.padding(top = 5.dp).fillMaxWidth()
                ) {
                    if (progress) {
                        ProgBar(value, goal)
                    }
                    Text(
                        "$value $unit",
                        modifier = Modifier.padding(12.dp, 6.dp).fillMaxWidth(.8f),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
        }
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun RegistroContent(x0: PaddingValues, viewModel: WearViewModel = viewModel()){
        val trainings by viewModel.getAll.collectAsState(initial = emptyList())

        LazyColumn(
            modifier = Modifier
                .padding(x0)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(trainings) { training ->
                TrainingCard(training)
            }

            if (trainings.isEmpty()) {
                item {
                    Text(
                        "No hay entrenamientos registrados.",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

    }

    @Composable
    private fun TrainingCard(data: WearData) {
        val duration = Duration.between(data.start, data.ending)
        val hours = duration.toHours()
        val minutes = (duration.toMinutes() % 60)
        val seconds = (duration.seconds % 60)

        val formattedDuration = buildString {
            if (hours > 0) append("${hours}h ")
            if (minutes > 0) append("${minutes}m ")
            append("${seconds}s")
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_calendar_today_24),
                        contentDescription = "Fecha",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        data.start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_nest_clock_farsight_analog_24),
                        contentDescription = "Duración",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Duración: $formattedDuration")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_steps_24),
                        contentDescription = "Pasos",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Pasos: ${data.steps}")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_local_fire_department_24),
                        contentDescription = "Calorías",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Calorías: ${data.calories}")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_favorite_24),
                        contentDescription = "Frecuencia Cardiaca",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("FC: ${String.format("%.1f", data.heartRate)} bpm")
                }
            }
        }
    }

    @Composable
    private fun AjustesContent(x0: PaddingValues) {
        val context = LocalContext.current

        val darkMode by SettingsDataStore.darkModeFlow(context).collectAsState(initial = false)
        val username by SettingsDataStore.usernameFlow(context).collectAsState(initial = "Usuario")
        val stepGoal by SettingsDataStore.stepGoalFlow(context).collectAsState(initial = 6000)
        val caloriesGoal by SettingsDataStore.caloriesGoalFlow(context).collectAsState(initial = 300f)

        val scope = rememberCoroutineScope()

        var showNameDialog by remember { mutableStateOf(false) }
        var newName by remember { mutableStateOf("") }
        var stepGoalInput by remember { mutableStateOf(stepGoal.toString()) }
        var caloriesGoalInput by remember { mutableStateOf(caloriesGoal.toString()) }

        // Sincroniza inputs cuando cambian desde DataStore
        LaunchedEffect(stepGoal) {
            stepGoalInput = stepGoal.toString()
        }
        LaunchedEffect(caloriesGoal) {
            caloriesGoalInput = caloriesGoal.toString()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(x0)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Cambiar nombre
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Usuario: $username", modifier = Modifier.weight(1f))
                Button(onClick = {
                    newName = username
                    showNameDialog = true
                }) {
                    Text("Cambiar")
                }
            }
            // Meta de pasos
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = stepGoalInput,
                    onValueChange = { stepGoalInput = it },
                    label = { Text("Meta de pasos") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    stepGoalInput.toIntOrNull()?.let {
                        scope.launch { SettingsDataStore.setStepGoal(context, it) }
                        Toast.makeText(context, "Meta de pasos actualizada", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Guardar")
                }
            }

            // Meta de calorías
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = caloriesGoalInput,
                    onValueChange = { caloriesGoalInput = it },
                    label = { Text("Meta de calorías") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    caloriesGoalInput.toFloatOrNull()?.let {
                        scope.launch { SettingsDataStore.setCaloriesGoal(context, it) }
                        Toast.makeText(context, "Meta de calorías actualizada", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Guardar")
                }
            }

            // Dark Mode toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Modo oscuro")
                Switch(
                    checked = darkMode,
                    onCheckedChange = {
                        scope.launch { SettingsDataStore.setDarkMode(context, it) }
                    }
                )
            }



            // Borrar datos
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                onClick = {
                    scope.launch {
                        SettingsDataStore.clearAll(context)

                        val dao = WearDatabase.getDatabase(context).wearDataDao()
                        dao.clearAll()
                        Toast.makeText(context, "Datos borrados", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("Borrar todos los datos", color = MaterialTheme.colorScheme.onError)
            }
        }

        // Diálogo para cambiar nombre
        if (showNameDialog) {
            AlertDialog(
                onDismissRequest = { showNameDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            SettingsDataStore.setUsername(context, newName)
                            showNameDialog = false
                        }
                    }) { Text("Guardar") }
                },
                dismissButton = {
                    TextButton(onClick = { showNameDialog = false }) { Text("Cancelar") }
                },
                title = { Text("Cambiar nombre") },
                text = {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Nuevo nombre") }
                    )
                }
            )
        }
    }
}
