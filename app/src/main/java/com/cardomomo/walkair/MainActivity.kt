package com.cardomomo.walkair

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.light(Color.BLACK, Color.WHITE))
        setContent {
            MaterialTheme {
                MainScreen()
            }
        }
    }

    @Composable
    fun MainScreen() {
        val navController = rememberNavController()
        val screens = listOf(Screen.Home, Screen.Registro, Screen.Ajustes)
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

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
    fun Inicio() {
        val context = LocalContext.current

        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

        // Leer los valores del DataStore
        val username by SettingsDataStore.usernameFlow(context).collectAsState(initial = "Usuario")
        val stepGoal by SettingsDataStore.stepGoalFlow(context).collectAsState(initial = 6000)
        val caloriesGoal by SettingsDataStore.caloriesGoalFlow(context).collectAsState(initial = 300f)

        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = isSystemInDarkTheme()

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LargeTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
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
                caloriesGoal = caloriesGoal.toInt()
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Registro() {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = isSystemInDarkTheme()
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

            topBar = {
                LargeTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
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
            RegistroContent(innerPadding)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Ajustes(){
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = isSystemInDarkTheme()
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

            topBar = {
                LargeTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
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


    @Composable
    private fun NavBar(){
        var selectedItem by remember { mutableIntStateOf(0) }
        val items = listOf("Inicio", "Registro", "Ajustes")
        val selectedIcons = listOf(Icons.Filled.Home, Icons.Filled.Menu, Icons.Filled.Settings)
        val unselectedIcons =
            listOf(Icons.Outlined.Home, Icons.Outlined.Menu, Icons.Outlined.Settings)

        NavigationBar {
            items.forEachIndexed { index, item ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            if (selectedItem == index) selectedIcons[index] else unselectedIcons[index],
                            contentDescription = item,
                        )
                    },
                    label = { Text(item) },
                    selected = selectedItem == index,
                    onClick = { selectedItem = index },
                )
            }
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
        caloriesGoal: Int
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            InfoCard("Pasos", R.drawable.outline_steps_24, 50, "pasos", stepGoal, true)
            InfoCard("Calorías", R.drawable.baseline_local_fire_department_24, 500, "cal", caloriesGoal, true)
            InfoCard("Frecuencia Cardiaca", R.drawable.outline_favorite_24, 120, "bpm", 0, false)

            Button(onClick = { /* acción */ }) {
                Text("Sincronizar")
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
    @Composable
    private fun RegistroContent(x0: PaddingValues){

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
