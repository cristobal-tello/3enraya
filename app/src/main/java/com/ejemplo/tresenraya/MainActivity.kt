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

// LÓGICA DE DOMINIO: función pura, fuera de la interfaz.
// Devuelve "X", "O" o null. No sabe nada de botones ni de Compose.
fun calculateWinner(cells: List<String>): String? {
    val lines = listOf(
        listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // filas
        listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // columnas
        listOf(0, 4, 8), listOf(2, 4, 6)                   // diagonales
    )
    for ((a, b, c) in lines) {
        if (cells[a].isNotEmpty() && cells[a] == cells[b] && cells[a] == cells[c]) {
            return cells[a]
        }
    }
    return null
}

@Composable
fun GameBoard() {
    // 1. EL ESTADO PRIMARIO
    val cells = remember { mutableStateListOf("", "", "", "", "", "", "", "", "") }
    var isXTurn by remember { mutableStateOf(true) }

    // 2. ESTADO DERIVADO: se recalculan solos en cada recomposición
    val winner = calculateWinner(cells)          // "X", "O" o null
    val isBoardFull = cells.none { it == "" }
    val isGameOver = winner != null || isBoardFull

    // 3. EFECTO SECUNDARIO: cuando la partida acaba (victoria O empate),
    // esperamos y reiniciamos. La victoria se muestra más tiempo que el empate.
    LaunchedEffect(isGameOver) {
        if (isGameOver) {
            delay(if (winner != null) 1500 else 500)
            cells.fill("")
            isXTurn = true
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 4. MENSAJE CONDICIONAL: victoria > empate > turno normal
        Text(
            text = when {
                winner != null -> "¡Gana $winner! 🎉"
                isBoardFull    -> "¡Empate!"
                isXTurn        -> "Turno de: X"
                else           -> "Turno de: O"
            },
            fontSize = 28.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // 5. LA MATRIZ (Grid de 3x3)
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
                        // Solo se marca si NO hay ganador y la casilla está vacía.
                        // El bloqueo por ganador evita seguir jugando tras la victoria.
                        if (winner == null && cells[index] == "") {
                            cells[index] = if (isXTurn) "X" else "O"
                            isXTurn = !isXTurn
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // 6. BOTÓN DE REINICIO MANUAL
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