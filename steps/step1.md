# Guía de Desarrollo: 3 en Raya por Bluetooth (Kotlin)

## MANUAL DE ENTORNO: VS Code + Docker + Depuración en Dispositivo

Este documento ayuda con la configuración del entorno de desarrollo utilizando Docker, la instalación automatizada de extensiones en VS Code, el despliegue en un dispositivo físico y la configuración del depurador.

---

## 1. Configuración de Docker y VS Code (`.devcontainer`)

Utilizaremos la extensión **Dev Containers** de VS Code. Toda la compilación sucederá dentro de la imagen `thyrlian/android-sdk`.

Crea una carpeta llamada `.devcontainer` en la raíz de tu proyecto y dentro genera el archivo `devcontainer.json` (no olvides el punto inicial) con la siguiente configuración:

```json
{
  "name": "Android Kotlin Development",
  "image": "thyrlian/android-sdk:latest",

  // Instalación AUTOMÁTICA de extensiones dentro del contenedor
  "customizations": {
    "vscode": {
      "extensions": [
        "fwcd.kotlin",
        "vscjava.vscode-gradle"
      ]
    }
  },

  // IMPORTANTE: Mapea los puertos USB de tu PC real al contenedor de Docker
  // Esto permite que el comando 'adb' del contenedor detecte tu móvil físico
  "runArgs": [
    "--privileged",
    "-v", "/dev/bus/usb:/dev/bus/usb"
  ],

  "remoteUser": "root"
}
```

En VS Code, haz clic en el botón azul en la esquina inferior izquierda y selecciona la opción **Dev Containers: Reopen in Container** (Reabrir en contenedor).

---

## 2. Archivos Mínimos de un Proyecto Android (Estructura base)

Para solucionar problemas de dependencias y seguir las buenas prácticas globales, dividimos la configuración de Gradle en la raíz y en el módulo específico de la aplicación (`app`).

### Paso 2.1: Inicializar Gradle en la carpeta vacía

Antes de crear los archivos, abre la terminal integrada de VS Code (dentro de Docker) y ejecuta el comando global para generar el entorno base y obtener el script ejecutable `gradlew`:

```bash
gradle init --type basic --dsl kotlin
chmod +x gradlew
```

A partir de este momento, generamos el esqueleto de carpetas obligatorio para Android:

```bash
mkdir -p app/src/main/java/com/ejemplo/tresenraya
mkdir -p app/src/main
touch settings.gradle.kts
touch build.gradle.kts
touch gradle.properties
touch app/build.gradle.kts
```

### Paso 2.2: Configurar los Repositorios (`settings.gradle.kts`)

Crea o edita `settings.gradle.kts` en la raíz del proyecto. Aquí le indicamos a Gradle que busque las herramientas en los servidores de Google:

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "3enraya"
include(":app")
```

### Paso 2.3: Configuración de la Raíz (`build.gradle.kts`)

Edita el archivo `build.gradle.kts` de la raíz del proyecto. En la arquitectura moderna, aquí solo se declaran las versiones globales sin aplicarlas de inmediato:

```kotlin
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
}
```

### Paso 2.4: Configuración del Módulo de la App (`app/build.gradle.kts`)

Edita el archivo `build.gradle.kts` ubicado dentro de la subcarpeta `app/`. Este archivo gestiona la compilación de la app y añade el soporte para Jetpack Compose:

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.ejemplo.tresenraya"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ejemplo.tresenraya"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.8.1")
}
```

### Paso 2.5: Manifiesto de Android (`app/src/main/AndroidManifest.xml`)

Este archivo le dice al sistema operativo cuál es la pantalla principal que debe arrancar. Créalo dentro de `app/src/main/`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:label="3 En Raya Test"
        android:theme="@android:style/Theme.Material.NoActionBar">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

### Paso 2.6: gradle.properties
```kotlin
# Habilita el uso de las librerías modernas AndroidX
android.useAndroidX=true

# Optimiza el uso de memoria de Gradle en el contenedor
org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m
```

---

## 3. Código de Prueba: El "Hello World" Interactivo

Crea el archivo del código fuente en la ruta `app/src/main/java/com/ejemplo/tresenraya/MainActivity.kt`. Añadiremos un botón que cambia un texto para poder testear la interacción y el depurador.

```kotlin
package com.ejemplo.tresenraya

import android.os.Bundle
import android.util.Log
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

    fun registrarPulsacion(valorActual: Int): Int {
        val nuevoValor = valorActual + 1
        Log.d("DEPURACION", "Incrementando contador a: $nuevoValor") // PON TU BREAKPOINT AQUÍ
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
```

### 3.1 Validar Compilación Local (Generar APK)

Si solo quieres asegurar que el código es correcto y genera el binario `.apk` en segundo plano sin enviarlo a ningún dispositivo físico, ejecuta:

```bash
./gradlew clean assembleDebug
```

Al finalizar con éxito, encontrarás el binario en `app/build/outputs/apk/debug/app-debug.apk`.

---

## 4. Preparación del Dispositivo Físico (Android)

Como no utilizaremos emuladores pesados dentro de Docker, un teléfono o tablet Android físico será el entorno de ejecución real.

1. En el dispositivo, ve a **Ajustes > Información del teléfono**.
2. Busca el **Número de compilación** (Build number) y pulsa 7 veces seguidas sobre él hasta que aparezca el mensaje *"Ya eres desarrollador"*.
3. Vuelve al menú principal de **Ajustes**, busca **Opciones de desarrollador** y activa:
   - **Depuración por USB** (USB Debugging).
4. Conecta el teléfono a tu ordenador mediante un cable USB de buena calidad.
5. Si aparece un mensaje en la pantalla del teléfono pidiendo permisos (*¿Permitir depuración por USB?*), marca **"Permitir siempre"** y acepta.

---

## 5. Guía Paso a Paso: Instalar la App en el Móvil

Una vez que tengas el cable conectado, sigue estos pasos en VS Code:

1. Abre la terminal integrada de VS Code (dentro de Docker) y comprueba que el contenedor detecta tu hardware USB:

```bash
adb devices
```

Deberías ver una línea con el identificador de tu dispositivo y la palabra `device`.

2. Para compilar el código Kotlin e inyectarlo directamente en tu teléfono conectado, ejecuta:

```bash
./gradlew installDebug
```

Verás cómo Gradle procesa las tareas y la app se abrirá automáticamente en la pantalla de tu móvil.

---

## 6. Guía de Depuración: Añadir Breakpoints

A diferencia de un entorno local donde pulsas "Play", en Docker nos "engancharemos" (*Attach*) al proceso de Java/Kotlin que ya se está ejecutando en el teléfono mediante el protocolo JDWP.

### Paso 6.1: Crear el archivo de lanzamiento

Crea una carpeta llamada `.vscode` en la raíz del proyecto y dentro genera el archivo `launch.json`:

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "kotlin",
      "request": "attach",
      "name": "Kotlin: Vincular a Teléfono Android",
      "port": 5005,
      "hostName": "localhost",
      "projectRoot": "${workspaceFolder}",
      "sourcePaths": [
        "${workspaceFolder}/app/src/main/java"
      ],
      "timeout": 30000
    }
  ]
}
```

### Paso 6.2: Compilar e instalar la app

Hazlo cada vez que cambies el código fuente:

```bash
./gradlew installDebug
```

### Paso 6.3: Obtener el ID de proceso (PID) real de tu juego

IMPORTANTE: Con la app abierta en la pantalla del móvil:

```bash
adb shell pidof com.ejemplo.tresenraya
```

Te devolverá un número, por ejemplo: `18420`.

### Paso 6.4: Destruir puentes residuales y abrir el canal limpio

Usa el número del paso anterior:

```bash
adb forward --remove-all
adb forward tcp:5005 jdwp:18420
```

### Paso 6.5: Validar el puente (opcional)

Para estar 100% seguro de que el puerto escucha:

```bash
adb forward --list
```

### Paso 6.6: Flujo para detener la ejecución (Breakpoint de Prueba)

1. Abre tu archivo `app/src/main/java/com/ejemplo/tresenraya/MainActivity.kt`.
2. Coloca un **Breakpoint** haciendo clic en el margen izquierdo del número de línea (aparecerá el clásico punto rojo). Ponlo exactamente en la línea donde se incrementa el contador: `contador++`.
3. Asegúrate de que la app está corriendo en tu teléfono tras haber ejecutado el `./gradlew installDebug`.
4. En VS Code, ve a la pestaña de **Run and Debug** (`Ctrl+Shift+D`), selecciona **"Kotlin: Vincular a Teléfono Android"** en el desplegable superior y dale al botón verde de **Play**.
5. **Prueba el flujo:** Pulsa el botón "Presióname" en la pantalla de tu teléfono físico. Verás que la pantalla del móvil se "congela" momentáneamente y VS Code resalta la línea en amarillo, deteniendo el tiempo. Ahora puedes inspeccionar el valor actual de `contador` y de `mensaje` en el panel izquierdo de VS Code exactamente igual que haces en .NET con Visual Studio o Symfony con Xdebug.

---

## 7. Próximo Paso Técnico

Con este entorno validado y la certeza de que podemos instalar y depurar código en tiempo real, estamos listos para comenzar a diseñar formalmente la cuadrícula de 3x3 y la lógica interna de turnos para el juego.

### Equivalencias Tecnológicas

| Concepto | En tu mundo (.NET / Symfony) | En este proyecto (Kotlin / VS Code) |
| --- | --- | --- |
| IDE | Visual Studio / PhpStorm | VS Code + Extensiones Kotlin |
| Gestor de Paquetes | NuGet / Composer | Gradle (`settings.gradle.kts` y `build.gradle.kts`) |
| Punto de Entrada | `Program.cs` / `index.php` | `MainActivity.kt` |
| Configuración Global | `appsettings.json` / `.env` | `AndroidManifest.xml` |