# FocusGuard (MVP)

Context-aware focus mode app for Android. Detects distractions (motion/noise), tracks sessions, and syncs history.

# Architecture
**Clean Architecture + MVVM** using Jetpack Compose, Hilt, and Coroutines/Flow.
- **Domain:** Pure Kotlin. Use cases depend on interfaces (`DistractionMonitor`, `FocusRepository`).
- **Data:** Platform implementations (`SensorManager`, `MediaRecorder`, Room, Retrofit, WorkManager sync).
- **Presentation:** Jetpack Compose UI reacting to `StateFlow` (UI state) and `Channel` (one-shot side effects like Snackbars).


# Native Resource Handling
- **Accelerometer:** Uses `SENSOR_DELAY_NORMAL` to detect movement (> 2.5 m/s²) while minimizing battery drain.
- **Microphone:** Uses `MediaRecorder.maxAmplitude` polled every 500ms (> 70 dB threshold).
- **Lifecycle:** Resources are explicitly acquired on `start()` and released in `stop()` via `try/finally` blocks when the `viewModelScope` is cleared.
- **Notifications:** Posts to a dedicated channel (`focus_distraction_channel`). Uses a custom `throttleFirst` (2-second window) to prevent spam during continuous distractions.

# Testability
- **Dependency Inversion:** All hardware and framework components (sensors, APIs, storage) are hidden behind interfaces.
- **No Android Dependencies in Logic:** The Domain and ViewModels can be tested purely on the JVM using fakes (e.g., `FakeDistractionMonitor`, `FakeFocusRepository`).

# Trade-offs Made
- **Offline-first Room storage:** Sessions are saved to Room first and synced to the API in the background. Trade-off: More moving parts than direct API writes, but resilient to flaky connectivity.
- **viewModelScope vs ForegroundService:** Sensor monitoring runs in the ViewModel. Trade-off: The OS will kill the session if the app is backgrounded for too long.
- **Fake API:** Used a fake repository implementation (`FakeFocusApiServiceImpl`) to simulate network calls without blocking the UI flow.
- **Manual State vs NavGraph:** Navigation relies on a simple enum state (remember { mutableStateOf(Tab) }) instead of Jetpack Navigation. Trade-off: Perfectly adequate and removes boilerplate for a simple 2-screen MVP, but lacks deep-linking and back-stack management which would be needed as the app grows.
- **MediaRecorder vs AudioRecord:** Selected MediaRecorder for noise detection as it directly surfaces maxAmplitude without manual byte-buffer reading. Trade-off: Heavier and offers less granular signal control than AudioRecord, but significantly faster to implement securely for basic threshold-based detection.

# Intentionally Deprioritized
- **History UI:** The data pipeline exists (`getHistory()`), but the Compose screen and ViewModel for the list were omitted.
- **Unit Tests Implementation:** The architecture is fully decoupled for testing, but the actual test cases were not written due to time constraints.

# Future Improvements & Scaling
If given more time and preparing for a production environment, I would prioritize:

- **Foreground Service:** Move sensor collection into a bound `ForegroundService` with a persistent notification to survive backgrounding.
- **Room Evolution:** Add indexes, migrations, data reconciliation, and richer history queries as session volume grows.
- **Real API Integration:** Replace the fake API implementation currently bound in DI and keep WorkManager for guaranteed offline-to-online syncing.
- **Debounce/Throttling Enhancements:** Move the distraction throttling logic further upstream into the domain layer.
- **Write Tests:** Implement tests for ViewModels and Use Cases using `kotlinx-coroutines-test`.
