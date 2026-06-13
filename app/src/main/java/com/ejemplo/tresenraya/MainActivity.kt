package com.ejemplo.tresenraya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GameBoard()
                }
            }
        }
    }
}

@Composable
fun GameBoard() {
    // 1. EL ESTADO: Una lista reactiva de 9 strings (inicialmente vacíos)
    val cells = remember { mutableStateListOf("", "", "", "", "", "", "", "", "") }

    // 2. ESTADO AUXILIAR: Alternador de turnos
    var isXTurn by remember { mutableStateOf(true) }

    // 3. ESTADO DERIVADO: ¿Está el tablero completo?
    // No es una variable nueva: se recalcula solo cada vez que cambia 'cells'.
    val isBoardFull = cells.none { it == "" }

    // 4. EFECTO SECUNDARIO: Reinicio automático diferido
    // La clave es 'isBoardFull'. En cuanto pasa de false a true, esta corrutina
    // se lanza: espera 0,5 s SIN congelar la pantalla y limpia el tablero.
    LaunchedEffect(isBoardFull) {
        if (isBoardFull) {
            delay(500) // medio segundo mostrando el mensaje
            cells.fill("")
            isXTurn = true
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 5. MENSAJE CONDICIONAL: Si el tablero está lleno mostramos el aviso;
        // en caso contrario, el indicador normal de turno.
        Text(
            text = if (isBoardFull) "¡Tablero completo! Reiniciando…"
                   else if (isXTurn) "Turno de: X" else "Turno de: O",
            fontSize = 28.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // 6. LA MATRIZ (Grid de 3x3)
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.width(300.dp).height(300.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(9) { index ->
                CellButton(
                    value = cells[index],
                    onClick = {
                        // Solo permitimos marcar si la casilla está vacía
                        if (cells[index] == "") {
                            cells[index] = if (isXTurn) "X" else "O"
                            isXTurn = !isXTurn
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // 7. BOTÓN DE REINICIO MANUAL (sigue disponible)
        Button(onClick = {
            cells.fill("")
            isXTurn = true
        }) {
            Text("Reiniciar Juego")
        }
    }
}

@Composable
fun CellButton(value: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(90.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (value == "") Color.Gray else MaterialTheme.colorScheme.primary
        )
    ) {
        Text(text = value, fontSize = 32.sp)
    }
}