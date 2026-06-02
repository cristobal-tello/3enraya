package com.ejemplo.tresenraya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var mensaje by remember { mutableStateOf("Hola Mundo desde Kotlin") }
            var contador by remember { mutableStateOf(0) }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = mensaje, fontSize = 24.sp)

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    contador++
                    // NOTA: Pon aquí tu Breakpoint para la prueba de depuración
                    mensaje = "¡Botón pulsado $contador veces!"
                }) {
                    Text(text = "Presióname")
                }
            }
        }
    }
}