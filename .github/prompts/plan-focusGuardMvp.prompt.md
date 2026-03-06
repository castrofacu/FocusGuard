# Plan: FocusGuard - Context-Aware Focus Mode MVP

**TL;DR:** Clean Architecture + MVVM con Jetpack Compose, Hilt, Coroutines/Flow. Core: sesión de foco con timer (Start/Pause/Stop), detección de distracción via acelerómetro y micrófono con contador de eventos, notificaciones throttleadas, persistencia con DataStore + Gson, historial en pantalla y accesibilidad WCAG integrada. API abstracta con `FakeFocusApiServiceImpl`. Navegación via estado simple — sin overhead de NavGraph para dos pantallas.

---

## Dependencias a agregar

- `hilt-android` + `hilt-compiler` (KSP)
- `lifecycle-viewmodel-compose` + `lifecycle-runtime-compose`
- `kotlinx-coroutines-android`
- `retrofit` + `converter-gson` + `okhttp` ← Gson reutilizado para DataStore
- `datastore-preferences`
- `mockk` + `kotlinx-coroutines-test` *(solo si llega a tests)*
- Plugin `com.google.devtools.ksp` *(solo para Hilt)*

> **Removidas vs iteraciones anteriores:** `navigation-compose` (reemplazada por estado simple), `MockInterceptor` (reemplazado por `FakeFocusApiServiceImpl`)

---

## Estructura de paquetes

```
domain/
  model/        ← FocusSession, SessionStatus, DistractionEvent
  sensor/       ← DistractionMonitor (interface)
  repository/   ← FocusRepository (interface)
  usecase/      ← StartFocusSessionUseCase, StopFocusSessionUseCase

data/
  remote/       ← FocusApiService (interface), FakeFocusApiServiceImpl, dto/
  sensor/       ← AccelerometerDistractionMonitor, MicrophoneDistractionMonitor, CompositeDistractionMonitor
  local/        ← SessionDataStore
  repository/   ← FocusRepositoryImpl (conoce remote + local, mapea DTOs, expone modelos de dominio)

di/             ← AppModule, SensorModule
presentation/   ← home/, history/
notification/   ← FocusNotificationManager
```

---

## Steps

### Fase 1 — Setup + README inicial (≈15 min)

1. Actualizar `gradle/libs.versions.toml` y `app/build.gradle.kts` + `build.gradle.kts` con KSP, Hilt, Retrofit, DataStore
2. Agregar permisos en `app/src/main/AndroidManifest.xml`: `POST_NOTIFICATIONS`, `INTERNET`, `RECORD_AUDIO`
3. Crear `FocusGuardApp.kt` (`@HiltAndroidApplication`) y registrarlo en Manifest
4. Anotar `MainActivity` con `@AndroidEntryPoint`
5. Crear `README.md` con secciones vacías — completar a medida que se toman decisiones

---

### Fase 2 — Domain Layer (≈20 min)

6. Crear `domain/model/FocusSession.kt` (data class: `id: Long`, `startTime: Long`, `durationSeconds: Int`, `distractionCount: Int`)
7. Crear `domain/model/SessionStatus.kt` (sealed class: `Idle`, `Running`, `Paused`)
8. Crear `domain/model/DistractionEvent.kt` (sealed class: `Movement`, `Noise(val decibels: Float)`)
9. Crear `domain/sensor/DistractionMonitor.kt` (interface: `val events: SharedFlow<DistractionEvent>`, `fun start(scope: CoroutineScope)`, `fun stop()`)
10. Crear `domain/repository/FocusRepository.kt` (interface: `startSession(): FocusSession`, `stopSession(id: Long, distractionCount: Int)`, `getHistory(): Flow<List<FocusSession>>`, `getSessionById(id: Long): FocusSession?`)
11. Crear use cases en `domain/usecase/`: `StartFocusSessionUseCase`, `StopFocusSessionUseCase` con `operator fun invoke()`

---

### Fase 3 — Data / Sensor Layer (≈35 min)

12. Crear `data/sensor/AccelerometerDistractionMonitor.kt` implementando `DistractionMonitor`:
    - `SensorManager` + `TYPE_ACCELEROMETER`
    - Calcula delta con el último valor; si supera threshold → emite `DistractionEvent.Movement`
    - Desregistra en `stop()`

13. Crear `data/sensor/MicrophoneDistractionMonitor.kt` implementando `DistractionMonitor`:
    - `MediaRecorder` con `AudioSource.MIC`, polling en coroutine cada 500ms via `getMaxAmplitude()`
    - Calcula decibeles: `20 * log10(maxAmplitude / 32767.0)`
    - Si supera threshold (~70dB) → emite `DistractionEvent.Noise(decibels)`
    - Llama `release()` en `stop()`
    - Trade-off documentado en README: `MediaRecorder` es más simple que `AudioRecord` pero no permite análisis fino del buffer de audio; suficiente para detectar ruido ambiente

14. Crear `data/sensor/CompositeDistractionMonitor.kt` (`@Singleton`) implementando `DistractionMonitor`:
    - Combina ambos con `merge()` de sus `SharedFlow`
    - **Único binding expuesto al ViewModel**

15. Crear `data/remote/dto/FocusSessionDto.kt` con campos mapeables al modelo de dominio (`FocusSession`)

16. Crear `data/remote/FocusApiService.kt` — interfaz que vive en data:
    ```
    suspend fun createSession(dto: FocusSessionDto): Result<FocusSessionDto>
    suspend fun getSessions(): Result<List<FocusSessionDto>>
    suspend fun getSessionById(id: Long): Result<FocusSessionDto>
    ```

17. Crear `data/remote/FakeFocusApiServiceImpl.kt` implementando `FocusApiService`:
    - Cada método con `delay(500)` y `Result.success(FocusSessionDto(...))`
    - En prod, Hilt swappearía a la impl real de Retrofit sin tocar nada fuera de `data/`

18. Crear `data/local/SessionDataStore.kt`:
    - `val Context.sessionDataStore by preferencesDataStore(name = "sessions")`
    - `val SESSIONS_KEY = stringPreferencesKey("focus_sessions")`
    - `fun readSessions(): Flow<List<FocusSession>>` — deserializa con Gson (`TypeToken<List<FocusSession>>`)
    - `suspend fun writeSessions(sessions: List<FocusSession>)` — serializa con Gson, escribe en DataStore

19. Crear `data/repository/FocusRepositoryImpl.kt`:
    - Lee/escribe via `SessionDataStore` — datos sobreviven kills de la app
    - `startSession()` → genera `FocusSession` (ID = `System.currentTimeMillis()`), persiste, llama `FocusApiService.createSession()`
    - `stopSession()` → actualiza la sesión en DataStore, llama API
    - `getHistory()` → retorna `Flow<List<FocusSession>>` de DataStore
    - `getSessionById()` → busca en DataStore; si no está, llama `FocusApiService.getSessionById()`
    - `FocusRepositoryImpl` es el único que conoce `FocusApiService` y mapea `FocusSessionDto ↔ FocusSession`

20. Crear `di/AppModule.kt` + `di/SensorModule.kt`:
    - Binding `FocusRepository → FocusRepositoryImpl`
    - Binding `FocusApiService → FakeFocusApiServiceImpl` ← swap a impl real en prod sin tocar nada más
    - Binding `DistractionMonitor → CompositeDistractionMonitor`
    - `SessionDataStore` provisto con `applicationContext`

---

### Fase 4 — Permisos en runtime (≈10 min)

21. En `MainActivity`: `ActivityResultContracts.RequestMultiplePermissions` para `RECORD_AUDIO` + `POST_NOTIFICATIONS` (solo `TIRAMISU+`) juntos
22. Resultado propagado al `HomeViewModel`:
    - `RECORD_AUDIO` denegado → `MicrophoneDistractionMonitor` no arranca, solo acelerómetro (degradación elegante)
    - `POST_NOTIFICATIONS` denegado → banner en UI como fallback, sin crash

---

### Fase 5 — Home Screen: timer + controles + accesibilidad (≈35 min) ← núcleo evaluativo

23. Crear `presentation/home/HomeUiState.kt`:
    ```kotlin
    data class HomeUiState(
        val status: SessionStatus = SessionStatus.Idle,
        val elapsedSeconds: Int = 0,
        val distractionCount: Int = 0,
        val lastDistractionEvent: DistractionEvent? = null
    )
    ```

24. Crear `presentation/home/HomeViewModel.kt` (`@HiltViewModel`):
    - Timer con `tickerFlow` interno activo solo cuando `status == Running`
    - Collect de `DistractionMonitor.events`:
        - `distractionCount` sube con **cada evento** (sin throttle)
        - Notificaciones con `.debounce(2000L)` — máximo una por cada 2s de ruido/movimiento continuo
    - `onStartClicked()`, `onPauseClicked()`, `onStopClicked()`
    - Sensores y timer en `viewModelScope` → documentado en README

25. Crear `presentation/home/HomeScreen.kt`:
    - Timer central `MM:SS` con `contentDescription = "Tiempo transcurrido: X minutos Y segundos"`
    - Contador visible "Distracciones: N" con `contentDescription` descriptivo
    - `AnimatedContent` para botones (`Idle` → Start; `Running` → Pause + Stop; `Paused` → Resume + Stop)
    - Card de alerta: "Movimiento detectado" / "Ruido: Xdb"
    - Todos los botones con `contentDescription` + `role = Role.Button`
    - Colores solo via `MaterialTheme.colorScheme` (WCAG AA garantizado)
    - Textos en `sp` — preview con `fontScale = 1.5f`
    - Botón "Ver historial" con `contentDescription`

---

### Fase 6 — Notifications (≈10 min)

26. Crear `notification/FocusNotificationManager.kt` (`@Singleton`):
    - `createChannel()` desde `FocusGuardApp.onCreate()`
    - `notifyDistraction(event: DistractionEvent)` — "Movimiento detectado" vs "Ruido: Xdb"
    - `notifySessionComplete(durationSeconds: Int, distractionCount: Int)`
    - `checkSelfPermission(POST_NOTIFICATIONS)` antes de notificar

---

### Fase 7 — History Screen + Navegación simple (≈15 min)

27. Crear `presentation/history/HistoryViewModel.kt` (`@HiltViewModel`) con `GetSessionHistoryUseCase`
28. Crear `presentation/history/HistoryScreen.kt`:
    - `LazyColumn` con duración, timestamp y chip con cantidad de distracciones
    - Empty state con `contentDescription = "No hay sesiones registradas"`
    - Items con `Modifier.semantics(mergeDescendants = true) {}`
29. Navegación en `MainActivity` via estado simple:
    ```kotlin
    enum class Screen { Home, History }
    var currentScreen by remember { mutableStateOf(Screen.Home) }
    when (currentScreen) {
        Screen.Home -> HomeScreen(onNavigateToHistory = { currentScreen = Screen.History })
        Screen.History -> HistoryScreen(onBack = { currentScreen = Screen.Home })
    }
    ```

---

### Fase 8 — Unit Tests (si sobra tiempo, ≈20 min)

30. `HomeViewModelTest.kt` — timer, throttling, contador, transiciones de estado
31. `StopFocusSessionUseCaseTest.kt`

---

### Fase 9 — Cerrar README (≈10 min)

32. Completar: DataStore vs Room, `FakeFocusApiServiceImpl` vs MockInterceptor, navegación simple vs NavGraph, Foreground Service descartado, accesibilidad integrada, throttling

---

## Verification

- `./gradlew assembleDebug` sin errores
- Start → timer → Stop → historial → matar app → reabrir → **historial persiste**
- Denegar `RECORD_AUDIO` → solo acelerómetro, sin crash
- Denegar `POST_NOTIFICATIONS` → banner en UI, sin crash
- Ruido continuo 10s → una notificación (throttle), contador sube normalmente
- Preview `fontScale = 1.5f` → UI no se rompe

---

## Decisions

- **`FakeFocusApiServiceImpl` sobre `MockInterceptor`**: cumple "Retrofit + Repository abstraction + separation of concerns" sin JSON a mano; el binding de Hilt hace el swap transparente en producción
- **`FocusApiService` en `data/remote/`**: el dominio es agnóstico a la red; solo conoce `FocusRepository`. `FocusRepositoryImpl` es el único que conoce la API y mapea DTOs al modelo de dominio
- **Estado simple sobre `navigation-compose`**: dos pantallas no justifican el overhead de un grafo de navegación; ahorra una dependencia y tiempo de setup
- **DataStore + Gson**: persistencia real en disco, Gson ya incluido por Retrofit, implementable en ~10 min
- **Throttle en notificaciones con `debounce(2000L)`, no en contador**: UX correcto — cada evento se registra, el system tray no se satura
- **`MediaRecorder` sobre `AudioRecord`**: más simple de implementar, `getMaxAmplitude()` es suficiente para detectar ruido ambiente; en prod se usaría `AudioRecord` para análisis fino del buffer
- **`CompositeDistractionMonitor`**: ViewModel agnóstico a cuántos sensores hay detrás (Open/Closed principle)
- **Lifecycle/background**: sensores y timer viven en `viewModelScope` → se pausan en background. Decisión intencional para el timebox; en prod sería `ForegroundService`
