# Apuntate 💶

App Android para **repartir gastos de grupo** en actividades deportivas (esquí, rutas, partidos, etc.).

Crea un grupo, añade a los participantes, registra los gastos (coche, comida, hotel…) indicando quién pagó cada uno, y la app calcula el **total**, el **coste por persona** y **quién debe pagar a quién** para saldar las cuentas.

Toda la interfaz está en **español** y los importes en **euros (€)**.

---

## ✨ Funcionalidades

- **Grupos**: crea un grupo por actividad/viaje (ej. "Esquí Sierra Nevada").
- **Participantes**: añade a las personas del grupo.
- **Gastos**: registra cada gasto con concepto, importe y quién lo pagó.
- **Balance**:
  - Total gastado y coste por persona.
  - Lista de pagos sugeridos para saldar cuentas (algoritmo tipo *Splitwise*).
- **Almacenamiento local** en el dispositivo (base de datos Room). No necesita internet ni cuenta.

## 🧮 Cómo se reparte el gasto

1. Se suma el total de todos los gastos.
2. Se divide entre el número de participantes (coste por persona). Los céntimos
   sobrantes se reparten entre los primeros participantes para que la suma cuadre al céntimo.
3. Para cada persona: `saldo = lo que pagó − lo que le corresponde`.
   - Saldo **positivo** → le deben dinero.
   - Saldo **negativo** → debe dinero.
4. Un algoritmo voraz empareja a quien más debe con quien más le deben para
   generar el **mínimo de transferencias** necesarias.

Todo el cálculo se hace en **céntimos (enteros)** para evitar errores de redondeo con decimales.

## 🛠️ Stack técnico

- **Kotlin**
- **Jetpack Compose** (Material 3) para la interfaz
- **Room** para la base de datos local
- **ViewModel + StateFlow** para el estado
- **Navigation Compose** para la navegación

## 🚀 Cómo compilar y ejecutar

### Requisitos
- [Android Studio](https://developer.android.com/studio) (versión Ladybug o superior recomendada)
- JDK 17
- Un emulador o dispositivo Android con **Android 7.0 (API 24) o superior**

### Pasos
1. Abre **Android Studio** → *Open* → selecciona la carpeta `apuntate`.
2. En la primera sincronización, Android Studio descargará las dependencias y
   **generará automáticamente el Gradle wrapper** (`gradle/wrapper/gradle-wrapper.jar`).
3. Pulsa **Run ▶** con un emulador o dispositivo conectado.

> **Nota:** este proyecto incluye los scripts del wrapper (`gradlew`, `gradlew.bat`)
> y `gradle-wrapper.properties`, pero **no** el binario `gradle-wrapper.jar`.
> Android Studio lo genera en la primera sincronización. Si prefieres hacerlo por
> línea de comandos y tienes Gradle instalado, ejecuta una vez:
> ```bash
> gradle wrapper --gradle-version 8.11.1
> ```
> y después ya podrás usar `./gradlew assembleDebug`.

## 📂 Estructura del proyecto

```
app/src/main/java/com/apuntate/
├── MainActivity.kt                 # Punto de entrada
├── data/                           # Capa de datos (Room)
│   ├── Entities.kt                 # Grupo, Participante, Gasto
│   ├── ApuntateDao.kt              # Consultas a la BD
│   ├── ApuntateDatabase.kt         # Base de datos Room
│   └── ApuntateRepository.kt       # Repositorio
├── domain/
│   └── Liquidacion.kt              # Lógica de cálculo del balance y liquidación
└── ui/                             # Interfaz (Jetpack Compose)
    ├── ApuntateApp.kt              # Navegación
    ├── Moneda.kt                   # Formateo de euros
    ├── GruposViewModel.kt
    ├── DetalleGrupoViewModel.kt
    ├── theme/Theme.kt
    └── screens/
        ├── GruposScreen.kt         # Lista de grupos
        ├── DetalleGrupoScreen.kt   # Participantes / Gastos / Balance
        ├── DialogoGasto.kt         # Diálogo para registrar un gasto
        └── Componentes.kt          # Componentes reutilizables
```

## 📝 Ejemplo de uso

> Grupo "Esquí" con 4 personas. Ana paga el hotel (400 €), Luis el coche (80 €),
> Marta la comida (60 €) y Juan no paga nada.
>
> - Total: **540 €** → **135 € por persona**.
> - La app indica, por ejemplo, que *Juan paga 135 € a Ana* y *Marta/Luis* ajustan
>   el resto, con el mínimo número de transferencias.
