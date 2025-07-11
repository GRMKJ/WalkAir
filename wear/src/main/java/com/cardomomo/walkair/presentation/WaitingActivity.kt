package com.cardomomo.walkair.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.wear.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.cardomomo.walkair.presentation.theme.WalkAirWearTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay

class WaitingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Empezar el conteo para lanzar el entrenamiento automáticamente
        lifecycleScope.launch {
            delay(3000) // Esperar 3 segundos
            val intent = Intent(this@WaitingActivity, MainActivity::class.java).apply {
                putExtra("start_training", true)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }

        setContent {
            WalkAirWearTheme {
                WaitingScreen()
            }
        }
    }

    @Composable
    fun WaitingScreen() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), // opcional para evitar que el texto quede pegado a los bordes
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Preparando entrenamiento...", style = MaterialTheme.typography.labelLarge)
                CircularProgressIndicator(modifier = Modifier.padding(top = 12.dp))
            }
        }
    }
}