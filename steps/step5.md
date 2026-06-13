# Guía de Desarrollo: 3 en Raya por Bluetooth (Kotlin)

## FASE 5: Detección de Ganador y Empate (Lógica de Dominio Pura)

En la Fase 4 el tablero aprendió a reaccionar solo: detectaba cuándo estaba **completo** y se reiniciaba. Pero dejamos un cabo suelto a propósito: la app no sabe **quién gana**. Para ella, tres "X" en fila no significan nada; simplemente espera a que se llenen las 9 casillas.

El objetivo de esta fase es dotar al juego de su primera **regla de negocio real**: comprobar las 8 combinaciones ganadoras del 3 en raya, anunciar al ganador (o el empate) con un mensaje y reiniciar automáticamente.

> 🔑 **Concepto clave de esta fase**: separar la **lógica de dominio** (el algoritmo que decide quién gana) de la **capa de presentación** (los botones y textos). Viniendo del backend, es la misma frontera que separas entre un *Service* con las reglas de negocio y un *Controller* que solo pinta la respuesta.

---

## 1. El Problema: ¿Qué significa "ganar"?

En un tablero numerado del 0 al 8 así:

```
 0 | 1 | 2
-----------
 3 | 4 | 5
-----------
 6 | 7 | 8
```

Existen exactamente **8 líneas ganadoras**: 3 filas, 3 columnas y 2 diagonales. Alguien gana cuando las tres casillas de **cualquiera** de esas líneas contienen el mismo símbolo (y no están vacías).

En lugar de escribir 8 condiciones `if` gigantes y repetitivas, modelamos las líneas como **datos**: una lista de tríos de índices. Luego un único bucle recorre esos datos y aplica la misma comprobación a todos. Esto es **dirigir el código mediante datos** (*data-driven*), y es mucho más mantenible que copiar y pegar condiciones.

---

## 2. La Herramienta: Una Función Pura

La comprobación del ganador **no debe vivir dentro de un `@Composable`**. No tiene nada que ver con dibujar: es lógica matemática. La extraemos a una **función pura**:

* **Pura** significa que su resultado depende **solo** de sus parámetros de entrada, sin tocar estado externo ni provocar efectos secundarios. Con las mismas 9 casillas, siempre devuelve lo mismo.
* Recibe la lista de casillas y devuelve un `String?` (un *nullable*): `"X"` o `"O"` si hay ganador, o `null` si todavía no lo hay.

```kotlin
fun calculateWinner(cells: List<String>): String? {
    val lines = listOf(
        listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // filas
        listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // columnas
        listOf(0, 4, 8), listOf(2, 4, 6)                   // diagonales
    )
    for ((a, b, c) in lines) {
        // Si la primera casilla NO está vacía y las tres coinciden -> hay ganador
        if (cells[a].isNotEmpty() && cells[a] == cells[b] && cells[a] == cells[c]) {
            return cells[a]
        }
    }
    return null // Nadie ha ganado todavía
}
```

> Fíjate en `for ((a, b, c) in lines)`: Kotlin **desestructura** cada trío `listOf(0, 1, 2)` en tres variables de golpe, igual que `val (a, b, c) = ...`. Es azúcar sintáctico que hace el bucle legible.

El `String?` con `?` es el sistema de **tipos nulos seguros** de Kotlin: el compilador te obliga a contemplar el caso "todavía no hay ganador" (`null`), evitando los temidos `NullPointerException`.

---

## 3. Código Fuente: Tablero con Ganador y Empate (`MainActivity.kt`)

Reemplaza el contenido de `app/src/main/java/com/ejemplo/tresenraya/MainActivity.kt`. Respecto a la Fase 4 se añade: la función `calculateWinner`, el estado derivado `winner`, el bloqueo del tablero tras la victoria y un mensaje que distingue victoria, empate y turno.

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
```

---

## 4. Análisis del Flujo: ¿Qué ocurre al colocar la ficha ganadora?

1. **Pulsación**: el jugador completa `0,1,2` con "X". `onClick` ejecuta `cells[2] = "X"`.
2. **Recomposición**: como `cells` es reactiva, Compose vuelve a evaluar `GameBoard()`.
3. **Recálculo de derivados**: `calculateWinner(cells)` recorre las líneas, encuentra la fila `0,1,2` con tres "X" y devuelve `"X"`. La variable `winner` pasa de `null` a `"X"`, e `isGameOver` pasa a `true`.
4. **Mensaje**: el `when` superior muestra "¡Gana X! 🎉".
5. **Bloqueo**: a partir de aquí, cualquier pulsación sobre otra casilla se ignora, porque `onClick` exige `winner == null`.
6. **Efecto diferido**: `LaunchedEffect` ve que su clave cambió a `true`, espera 1,5 s sin congelar la pantalla y vacía el tablero.
7. **Nueva partida**: al limpiarse `cells`, `winner` vuelve a `null`, `isGameOver` a `false`, el mensaje vuelve al turno y el tablero queda listo.

Si en cambio se llenan las 9 casillas **sin** ninguna línea, `winner` sigue siendo `null` pero `isBoardFull` es `true`: se muestra "¡Empate!" durante 0,5 s y se reinicia.

---

## 5. La Frontera Dominio / Presentación: ¿Por qué una función aparte?

Podríamos haber metido el bucle de las líneas directamente dentro de `GameBoard`. No lo hacemos por los mismos motivos que en el backend separas la lógica de negocio del controlador:

* **Testeable de forma aislada**: `calculateWinner` es una función pura sin dependencias de Android. Puedes escribir un test unitario (`assertEquals("X", calculateWinner(listOf("X","X","X", "","","", "","","")))`) que corre en milisegundos **sin emular un teléfono**.
* **Reutilizable**: cuando lleguemos al modo Bluetooth, el rival jugará en otro móvil y su jugada llegará por la red. Esa **misma** función decidirá el ganador sin tocar una línea, porque no depende de quién ni cómo se rellenó el tablero.
* **Legible**: `GameBoard` describe *qué se ve*; `calculateWinner` describe *qué significa ganar*. Cada función tiene una única responsabilidad.

---

## 6. Tres Estados Derivados Encadenados

Esta fase consolida la regla de oro de la Fase 4: **si un dato se puede calcular, derívalo; no lo guardes como estado nuevo.** Aquí encadenamos tres derivaciones a partir de un único estado primario (`cells`):

| Estado derivado | Se calcula de | Significa |
| --- | --- | --- |
| `winner` | `cells` | Quién ha ganado (`"X"`, `"O"` o `null`) |
| `isBoardFull` | `cells` | No quedan casillas vacías |
| `isGameOver` | `winner` + `isBoardFull` | La partida ha terminado |

Ninguno puede desincronizarse, porque ninguno existe por sí mismo: todos se recalculan en cada recomposición a partir de la **única fuente de verdad**.

> ℹ️ **¿Por qué `isGameOver` y no simplemente `winner != null`?**
> Es tentador pensar que con saber si hay ganador basta para saber si la partida terminó. Pero el 3 en raya tiene **dos finales distintos**:
> - **Victoria** → `winner != null` (alguien completó una línea).
> - **Empate** → `isBoardFull && winner == null` (las 9 casillas llenas sin ninguna línea).
>
> Si usáramos solo `winner != null` como disparador del reinicio, una partida que acaba en tablas **nunca se reiniciaría**: se quedaría congelada con el tablero lleno y sin ganador. `isGameOver = winner != null || isBoardFull` engloba **ambos** finales en un único concepto, y por eso es la clave correcta del `LaunchedEffect`. En cambio, el **bloqueo** del tablero sí usa solo `winner == null`: tras un empate no hace falta bloquear nada porque ya no quedan casillas vacías que pulsar.

Esta es la base sobre la que, en la siguiente fase, conectaremos dos dispositivos por **Bluetooth** y sincronizaremos sus tableros.
