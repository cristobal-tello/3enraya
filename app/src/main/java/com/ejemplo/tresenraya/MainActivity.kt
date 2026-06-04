package com.ejemplo.tresenraya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 1. EL ESTADO
            var nombre by rememberSaveable { mutableStateOf("") }

            // 2. EL CONTENEDOR DE ESTILO (Material Theme + Surface)
            // Esto repara los colores automáticamente según el modo (claro/oscuro) de tu Samsung
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background // Adapta el fondo al sistema
                ) {
                    // 3. EL CONTENEDOR VISUAL (Ahora dentro de Surface)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 4. EL COMPONENTE ENTRADA DE TEXTO
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nuevoTexto -> 
                                nombre = nuevoTexto 
                            },
                            label = { Text("Escribe tu nombre") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // 5. EL COMPONENTE REACTIVO
                        if (nombre.isEmpty()) {
                            Text(text = "Por favor, escribe algo...", fontSize = 20.sp)
                        } else {
                            Text(text = "¡Hola, $nombre! 👋", fontSize = 24.sp)
                        }
                    }
                }
            }
        }
    }
}