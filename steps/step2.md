# Guía de Desarrollo: 3 en Raya por Bluetooth (Kotlin)

## MANUAL DE JETPACK COMPOSE: Estados y Flujo Declarativo 

Este documento es una introducción práctica a **Jetpack Compose**. Antes de pintar una matriz de 3x3 para el juego, es fundamental asimilar cómo funciona el cambio de paradigma de interfaces estáticas a interfaces reactivas basadas puramente en el **Estado**.

---

## 1. El Cambio de Paradigma: ¿Qué es "Declarativo"?

Viniendo del desarrollo clásico (como .NET Windows Forms, Android antiguo con XML o manipulación directa del DOM con JavaScript), estás acostumbrado al modelo **Imperativo**:
1. Buscas el componente en la memoria: `val miEtiqueta = findViewById(R.id.texto)`
2. Modificas su valor directamente cuando ocurre un evento: `miEtiqueta.setText("Hola")`

En **Jetpack Compose** no modificas los componentes visuales de la pantalla. La interfaz es **Declarativa**:
1. Creas una estructura de datos (El Estado).
2. Defines la pantalla como una función matemática que "pinta" según los datos actuales.
3. Cuando los datos cambian, la pantalla completa se destruye en milisegundos y se vuelve a renderizar (**Recomposición**) para reflejar los nuevos valores de forma automática.

---

## 2. Los Tres Pilares de Jetpack Compose

Para crear cualquier interfaz reactiva, debemos dominar tres conceptos clave integrados en el compilador de Kotlin:

* **Funciones `@Composable`:** Cualquier función con la etiqueta `@Composable` le dice a Android que no procese lógica pura de retorno, sino que genere elementos gráficos en la pantalla. Componentes como `Column`, `Row`, `Text` y `Button` son simplemente funciones programadas por Google.
* **Sesión a Corto Plazo (`rememberSaveable`):** Como las vistas se destruyen y redibujan constantemente, las variables normales volverían a su valor inicial (`""` o `0`). El contenedor `rememberSaveable` guarda el valor de la variable en la memoria RAM del teléfono, protegiéndola incluso si el usuario gira la pantalla.
* **Sensores de Redibujo (`mutableStateOf`):** Envuelve tus datos en un contenedor con sensores. En el momento en que modificas el valor de un estado definido con `mutableStateOf`, este le envía un "evento" automáticamente a Compose obligándolo a redibujar únicamente los textos o botones que dependen de esa variable.

---

## 3. Ejercicio Práctico: Entrada de Texto Reactiva

Diseñaremos un escenario clásico: un campo de entrada de texto (*Input*) donde el usuario escribe su nombre y, de forma instantánea (letra por letra), un texto inferior se actualiza para saludarlo.

### Paso 3.1: Actualizar el Código Fuente (`MainActivity.kt`)

Reemplaza el archivo `app/src/main/java/com/ejemplo/tresenraya/MainActivity.kt` con el siguiente código experimental de aprendizaje:

```kotlin
package com.ejemplo.tresenraya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme // Heredar los colores oficiales
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface // El contenedor de fondo
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
            // Esto repara los colores automáticamente según el modo (claro/oscuro)
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
```

### Paso 3.2: El Ciclo de Datos Unidireccional (UDF)
Para entender cómo funciona el backend de este código, analiza la lógica secuencial que se ejecuta cuando presionas, por ejemplo, la tecla "A" en el teclado físico de tu móvil:

[ Teclado: Presionas "A" ] 
          │
          ▼
[ Se ejecuta el bloque 'onValueChange' ] 
          │
          ▼
[ La variable de estado 'nombre' cambia de "" a "A" ] 
          │
          ▼
[ El sensor 'mutableStateOf' avisa al compilador ] 
          │
          ▼
[ RECOMPOSICIÓN: Compose destruye la interfaz actual y la vuelve a evaluar ] 
          │
          ▼
[ 'OutlinedTextField' lee que 'nombre' es "A" y lo dibuja ]
[ 'Text' detecta que no está vacío y renderiza el saludo final ]

---

## 4. Manual de Resolución de Problemas (Troubleshooting de Consola)

Durante la compilación en contenedores Docker y el despliegue mediante USB, pueden ocurrir excepciones de entorno de infraestructura. Usa estas recetas quirúrgicas de terminal:

### Escenario A: Error Crítico de Recursos / Caída del Demonio de Kotlin

Si la terminal arroja una traza larga indicando `Daemon compilation failed: Could not connect to Kotlin compile daemon` debido al desbordamiento de memoria RAM dentro del contenedor, ejecuta:

```bash
./gradlew --stop  # Detiene los procesos zombies en segundo plano de Gradle
./gradlew clean   # Elimina los binarios temporales corruptos
```

### Escenario B: Excepción de Puerto Desconectado (DeviceException: No connected devices!)

Si la compilación se detiene en el último segundo porque ADB ha perdido el rastro del cable físico o el demonio se ha reiniciado, gestiona el flujo de reconexión así:

#### Opción B1: Compilación Local (sin dispositivo)

Si estás trabajando sin un teléfono físico conectado, valida que la sintaxis y los imports de Compose sean correctos compilando únicamente en local (genera el archivo .apk sin enviarlo):

```bash
./gradlew assembleDebug
```

#### Opción B2: Reconexión del Dispositivo Físico

Si tienes el teléfono listo para la acción, reconéctalo limpiamente ejecutando:

```bash
adb kill-server  # Resetea el puente USB colgado de Docker
adb devices      # Despierta el escaneo y valida que aparezca el identificador
```

> **Nota:** Asegúrate de marcar el cuadro de diálogo flotante "Permitir siempre desde este ordenador" en la pantalla del teléfono si reaparece.

Una vez que el dispositivo esté en modo de escucha, inyecta la aplicación final con:

```bash
./gradlew installDebug
```