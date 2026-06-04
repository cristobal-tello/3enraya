package com.ejemplo.tresenraya

import android.os.Bundle
import android.util.Log // 🌟 IMPORTANTE: Añade este import
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {

    // 🌟 1. CREA ESTA FUNCIÓN TRADICIONAL AQUÍ (FUERA DE COMPOSE)
    fun registrarPulsacion(valorActual: Int): Int {
        val nuevoValor = valorActual + 1
        Log.d("DEPURACION", "Incrementando contador a: $nuevoValor") // 🔴 PON TU BREAKPOINT AQUÍ
        return nuevoValor
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var mensaje by rememberSaveable { mutableStateOf("Hola Mundo desde Kotlin") }
            var contador by rememberSaveable { mutableStateOf(0) }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = mensaje, fontSize = 24.sp)

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    
                    contador = registrarPulsacion(contador)
                    mensaje = "¡Botón pulsado $contador veces!"
                }) {
                    Text(text = "Presióname")
                }
            }
        }
    }
}