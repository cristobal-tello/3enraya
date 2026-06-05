# Guía de Desarrollo: 3 en Raya por Bluetooth (Kotlin)

## FASE 3: El Tablero Reactivo (Estructuras de Datos y Cuadrículas)

Tras dominar el flujo de un solo estado, el siguiente reto es gestionar el **Tablero**. En Jetpack Compose, no creamos 9 botones independientes de forma manual; creamos una **estructura de datos** de 9 posiciones y dejamos que Compose "dibuje" la cuadrícula basándose en esa lista.

---

## 1. La Estructura de Datos: Listas Mutables

Para el 3 en raya, necesitamos representar el estado de cada casilla (Vacío, "X" u "O"). En lugar de 9 variables sueltas, utilizaremos una **Lista de Estados**.

* **`mutableStateListOf()`**: Es una versión especial de las listas de Kotlin diseñada para Compose. Actúa como un array con observadores incorporados. Si modificas el índice de un elemento de esta lista, Compose detectará qué posición cambió e iniciará el redibujo exclusivo de esa porción de la pantalla.

---

## 2. El Layout: Matriz Visual con 'LazyVerticalGrid'

Viniendo del backend, piensa en esto como un generador dinámico de filas y columnas (similar a un renderizado en bucle en plantillas web). Usaremos `LazyVerticalGrid` con un número fijo de columnas (3), lo que distribuirá automáticamente nuestros 9 elementos en una matriz perfecta de 3x3 sin necesidad de anidar tablas ni layouts pesados.

---

## 3. Código Fuente: Tablero Interactivo (`MainActivity.kt`)

Reemplaza el contenido de tu archivo `app/src/main/java/com/ejemplo/tresenraya/MainActivity.kt` con este código estructurado. Incluye una lógica simple de "Turno" local para validar la interactividad táctil del hardware:

```kotlin
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
    // ⚠️ NOTA TÉCNICA: 'remember' retiene la lista en memoria RAM ordinaria.
    // Al girar la pantalla la app se destruirá y se reseteará. En fases posteriores
    // delegaremos esta persistencia a una arquitectura estructurada (ViewModel).
    val cells = remember { mutableStateListOf("", "", "", "", "", "", "", "", "") }

    // 2. ESTADO AUXILIAR: Alternador de turnos
    var isXTurn by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Indicador dinámico de estado de turno
        Text(
            text = if (isXTurn) "Turno de: X" else "Turno de: O",
            fontSize = 28.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // 3. LA MATRIZ (Grid de 3x3)
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
                        // Lógica de negocio: Solo permitimos marcar si la casilla está vacía
                        if (cells[index] == "") {
                            cells[index] = if (isXTurn) "X" else "O"
                            isXTurn = !isXTurn // Inversión booleana de turno
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // 4. BOTÓN DE REINICIO (Limpia la estructura de datos iterando los índices)
        Button(onClick = {
            for (i in 0..8) cells[i] = ""
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
```

---

## 4. Análisis del Flujo de Datos en el Tablero

Este código implementa un patrón de **Izado de Estado (State Hoisting)**:

* **`GameBoard` es el "cerebro" (Smart Component)**: contiene la lista de las 9 casillas y la lógica de quién mueve o cómo se mutan las propiedades del negocio.
* **`CellButton` es un componente "tonto" (Dumb Component)**: no sabe qué posición ocupa en la matriz ni de quién es el turno. Solo recibe un texto para mostrar y avisa cuando lo pulsan.
* Al pulsar un botón, la función `onClick` viaja hacia arriba mediante un callback, modifica la lista en el "cerebro", y Compose automáticamente redibuja el botón afectado con la nueva "X" u "O".

---

## 5. Enfoque Compose (Declarativo) vs Enfoque Clásico (Imperativo)

En C# (.NET) o JavaScript tradicional, los botones son "Objetos Vivos" pesados instanciados en memoria RAM de manera estática mediante una matriz: `Button[] misBotones = new Button[9]`. Capturas el evento y manipulas directamente la instancia: `misBotones[4].Text = "X"`.

En Jetpack Compose, las funciones `@Composable` no retienen instancias en memoria; son **"Píxeles temporales"**. Se comportan como una plantilla de renderizado del backend que procesa datos de entrada y genera salidas gráficas directamente en la GPU para luego desaparecer de la ejecución.

**Cuando tú pulsas una casilla:**

1. Se modifica el backend del estado: `cells[index] = "X"`.
2. Como `cells` es reactivo (`mutableStateListOf`), el sensor avisa al compilador.
3. **Recomposición**: Compose invalida la pantalla actual, vuelve a evaluar la función `GameBoard()` desde arriba en milisegundos y re-ejecuta el bucle de renderizado.
4. Las funciones `CellButton` se procesan con los nuevos valores actualizados pasados por parámetro.

---

## 6. Justificación Arquitectónica del Componente `CellButton`

Separar la casilla en su propia función desacoplada obedece a tres principios de ingeniería de software:

* **Motivo A — Encapsulación de Estilos**: evitamos ensuciar la lógica del tablero (`GameBoard`) con parámetros de diseño cosmético como tamaños de fuentes (`fontSize`), formas geométricas (`shape`), márgenes (`Modifier.size`) o paletas de color condicionales (`containerColor`).
* **Motivo B — Desacoplamiento de Vista/Negocio**: permite cambiar la interfaz de la casilla por completo en el futuro (por ejemplo, sustituir los textos por iconos vectoriales o añadir animaciones) modificando únicamente la función `CellButton` sin alterar una sola línea de la lógica del juego.
* **Motivo C — Optimización del Compilador (Smart Recomposition)**: Compose compara los parámetros de entrada. Si la casilla 4 pasa de `""` a `"X"`, pero las casillas de la 0 a la 3 siguen recibiendo un string vacío `""`, Compose salta la ejecución de las funciones tontas cuyos parámetros no han mutado, ahorrando drásticamente CPU y batería del dispositivo móvil.