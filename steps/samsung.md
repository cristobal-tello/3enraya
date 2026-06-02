# Depuración USB en Samsung

## 1. Habilitar la Depuración USB en tu Samsung

En los terminales Samsung, la ruta exacta para activar el entorno de desarrollo es esta:

1. Ve a **Ajustes** (el icono del engranaje).
2. Baja hasta el fondo del todo y entra en **Acerca del teléfono**.
3. Pulsa sobre **Información de software**.
4. Busca **Número de compilación** y pulsa 7 veces seguidas sobre esa línea. Te pedirá el PIN o patrón de desbloqueo de tu móvil para confirmar.
5. Vuelve hacia atrás al menú principal de **Ajustes**. Ahora, abajo del todo, habrá aparecido una sección nueva llamada **Opciones de desarrollador**. Entra ahí.
6. Asegúrate de activar el interruptor de **Depuración por USB**.

## 2. ¡Cuidado con la pantalla de bloqueo de Samsung!

Los dispositivos Samsung son muy estrictos con la seguridad física por USB.

1. Cuando conectes el cable del móvil al ordenador, **desbloquea la pantalla** de tu Galaxy.
2. Te saltará un aviso flotante preguntando: **¿Permitir depuración por USB?**

> **Truco indispensable:** marca la casilla que dice _"Permitir siempre desde este ordenador"_ antes de darle a **Aceptar**. Si no marcas esa casilla, el contenedor de Docker perderá el acceso cada vez que el cable se mueva un milímetro.
