# Guía de Desarrollo: 3 en Raya por Bluetooth (Kotlin)

## FASE 4: Detección de Tablero Completo y Reinicio Automático (Efectos Secundarios)

En la fase anterior construimos un tablero reactivo donde el usuario marca casillas alternando turnos. Ahora la aplicación es **pasiva**: solo reacciona a las pulsaciones. El objetivo de esta fase es dotar al tablero de una primera reacción **autónoma**: detectar por sí mismo cuándo todas las casillas están ocupadas, avisar al usuario con un mensaje temporal de medio segundo y reiniciarse sin intervención humana.

> ⚠️ **Importante**: En esta fase **NO** detectamos quién gana (líneas de 3 en raya). Eso llegará más adelante. Aquí solo nos interesa una condición mucho más sencilla: que **no quede ninguna casilla vacía**.

---

## 1. El Concepto: Estado Derivado

Hasta ahora hemos tenido dos tipos de estado: la lista de casillas (`cells`) y el turno (`isXTurn`). Ambos son **estado primario**: el usuario los modifica directamente.

La pregunta "¿está el tablero completo?" no necesita una variable propia: es una **consecuencia** del estado de las casillas. Lo llamamos **estado derivado** porque se *calcula* a partir de otro estado existente.

* **`cells.none { it == "" }`**: recorre la lista y devuelve `true` solo si **ninguna** casilla contiene una cadena vacía. Es la traducción directa de "el tablero está lleno".
* Al ser `cells` una lista reactiva (`mutableStateListOf`), cada vez que cambia una casilla, Compose vuelve a evaluar `GameBoard()` y **recalcula** automáticamente este booleano. No hace falta "avisar" manualmente de nada.

Viniendo del backend, piénsalo como una propiedad calculada (un *getter* sin campo de respaldo) o una columna virtual de base de datos: no se almacena, se deduce en el momento en que se consulta.

---

## 2. El Reto: Reaccionar al Cambio sin Bloquear la Interfaz

Saber que el tablero está lleno es fácil; el problema es **qué hacer y cuándo**. Queremos:

1. Mostrar un mensaje.
2. Esperar 0,5 segundos.
3. Vaciar el tablero.

La tentación del programador imperativo es escribir `Thread.sleep(500)` dentro del `onClick`. **Esto es un error grave en una interfaz**: congelaría el hilo principal (UI Thread), la pantalla se quedaría helada medio segundo y Android podría mostrar el temido diálogo "La aplicación no responde" (ANR).

Además, una función `@Composable` debe ser **pura**: se ejecuta muchas veces por segundo durante la recomposición y no puede contener pausas ni esperas. Necesitamos un mecanismo especial para lanzar tareas con duración fuera del ciclo de dibujado.

---

## 3. La Herramienta: `LaunchedEffect` y Corrutinas

Compose ofrece los **Efectos Secundarios** (*Side Effects*) para ejecutar código que "vive" más allá de un único fotograma de dibujado, como esperas temporizadas, llamadas de red o, en nuestro caso, un reinicio diferido.

* **`LaunchedEffect(clave)`**: lanza una **corrutina** (una tarea ligera en segundo plano) vinculada a la pantalla. Recibe una *clave*: cada vez que esa clave **cambia de valor**, Compose cancela la corrutina anterior y arranca una nueva. Si la clave no cambia, el bloque no se vuelve a ejecutar aunque haya 100 recomposiciones.
* **`delay(500)`**: es la versión "educada" de `sleep`. Suspende **solo** esta corrutina durante 500 milisegundos **sin bloquear** el hilo de la interfaz, que sigue libre para animar y responder al usuario.

Usaremos como clave el propio booleano `isBoardFull`. Así, en el instante exacto en que el tablero pasa de "incompleto" a "completo", la clave cambia, la corrutina se dispara, espera medio segundo y limpia las casillas.

---

## 4. Código Fuente: Tablero con Auto-Reinicio (`MainActivity.kt`)

Reemplaza el contenido de tu archivo `app/src/main/java/com/ejemplo/tresenraya/MainActivity.kt` con este código. Respecto a la Fase 3 se añade: el cálculo de `isBoardFull`, el `LaunchedEffect` que gestiona la espera y el reinicio, y un mensaje condicional.

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
```

---

## 5. Análisis del Flujo: ¿Qué ocurre al rellenar la última casilla?

Sigamos el recorrido completo cuando el usuario marca la **novena** casilla:

1. **Pulsación**: `onClick` ejecuta `cells[8] = "O"`. El tablero ya no tiene huecos.
2. **Recomposición**: como `cells` es reactiva, Compose vuelve a evaluar `GameBoard()`.
3. **Recálculo del derivado**: `cells.none { it == "" }` ahora devuelve `true`. La variable `isBoardFull` pasa de `false` a `true`.
4. **Disparo del efecto**: `LaunchedEffect` detecta que su clave cambió y lanza la corrutina. El `Text` superior muestra "¡Tablero completo! Reiniciando…".
5. **Espera no bloqueante**: `delay(500)` suspende la corrutina medio segundo; la interfaz sigue viva.
6. **Limpieza**: el bucle vacía las 9 casillas y restaura el turno a "X".
7. **Nueva recomposición**: al vaciarse `cells`, `isBoardFull` vuelve a `false`, el mensaje desaparece y el tablero queda listo para una nueva partida.

---

## 6. Estado Primario vs Estado Derivado: Una Regla de Oro

Un error común del principiante sería crear una variable `var tableroLleno by remember { mutableStateOf(false) }` y actualizarla **a mano** dentro de cada `onClick`. Eso introduce dos fuentes de verdad que pueden desincronizarse: si olvidas actualizarla en algún punto, la app miente.

La regla de oro de Compose: **si un dato se puede calcular a partir de otro estado, no lo guardes como estado nuevo; derívalo.**

* **Estado primario** (`cells`, `isXTurn`): lo modifica el usuario. Es la **única fuente de verdad**.
* **Estado derivado** (`isBoardFull`): se calcula. Nunca puede contradecir al estado primario porque *no existe por sí mismo*.

Esto elimina toda una categoría de bugs de sincronización antes incluso de que aparezcan.

---

## 7. ¿Por qué `LaunchedEffect` y no un simple `if` en el cuerpo?

Podría parecer más simple escribir el reinicio directamente en el cuerpo del `@Composable`:

```kotlin
// ❌ INCORRECTO
if (isBoardFull) {
    delay(500)          // ERROR: delay es 'suspend', no compila aquí
    cells.fill("")
}
```

Esto está **prohibido** por dos motivos técnicos:

* **Motivo A — Las funciones `@Composable` no son suspendibles**: no pueden llamar a funciones `suspend` como `delay`. El cuerpo debe describir *qué se ve*, no *ejecutar tareas con duración*.
* **Motivo B — Se ejecutaría en cada recomposición**: el cuerpo de un `@Composable` corre decenas de veces. Modificar `cells` ahí dentro crearía un bucle de recomposición infinito. `LaunchedEffect`, en cambio, ejecuta su bloque **una sola vez** por cada cambio de la clave, dándonos un punto seguro y controlado para lanzar efectos.

`LaunchedEffect` es, por tanto, el puente oficial entre el mundo **declarativo** de la interfaz (lo que se dibuja) y el mundo **imperativo** de las tareas temporizadas (lo que sucede en el tiempo).
