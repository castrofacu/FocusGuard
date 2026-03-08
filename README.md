# FocusGuard

Context-aware focus mode app for Android. Detects distractions via accelerometer and microphone, tracks focus sessions, and persists history across app restarts.

## Architecture

Clean Architecture + MVVM with Jetpack Compose, Hilt, and Coroutines/Flow.
```
domain/      — models, interfaces, use cases (no Android deps)
data/        — sensors, remote, local, repository impl
di/          — Hilt modules
presentation/ — Compose screens + ViewModels
```

### Layer responsibilities

**Domain** is pure Kotlin — zero Android imports. `DistractionMonitor` and `FocusRepository` are interfaces; the domain use cases (`StartFocusSessionUseCase`, `StopFocusSessionUseCase`) depend only on those interfaces, making them unit-testable with fakes.

**Data** owns all platform concerns: `SensorManager` for motion, `MediaRecorder` for audio, DataStore for persistence, and Retrofit for the remote API. Each sensor wraps into a `DistractionMonitor` implementation that emits `SharedFlow<DistractionEvent>`. `CompositeDistractionMonitor` merges all monitors into a single flow the ViewModel subscribes to, so adding a new sensor (camera, GPS, …) never touches the ViewModel.

**Presentation** uses `StateFlow<HomeUiState>` for the rendering state and a `Channel<HomeEvent>` (exposed as `receiveAsFlow()`) for one-shot side effects (open permission dialog, show snackbar). This keeps the ViewModel side-effect-free and makes UI events easy to test by collecting from the channel in a `TestScope`.

---

## Tech Decisions & Trade-offs

### Persistence: DataStore + Gson vs Room

DataStore was chosen to minimise setup time. Sessions are serialised as a single JSON string with Gson and stored under one `stringPreferencesKey`. The whole list is read, a new item appended, and the result written back atomically via `dataStore.edit { }`.

**Trade-off:** This is fine for a small number of sessions but it is very slow. Room, is the correct choice at any real scale. Migration would be straightforward because `FocusSession` is a plain data class with no Android dependencies.

### API abstraction: FakeFocusApiServiceImpl

`FakeFocusApiServiceImpl` is active because there was no time to build a real backend — it was the fastest way to keep the rest of the data flow working end-to-end. It implements the same `FocusApiService` interface as `RetrofitFocusApiServiceImpl`, so switching to a real server is a single-line `@Binds` change in `AppModule` with no impact on the ViewModel or repository. The repository always writes to local DataStore first and then fires the remote call. `saveSession` returns `Result<Unit>`: on success the local write has already happened; on failure the session is still persisted locally and `HomeViewModel` receives the failure to notify the user.

### Navigation: manual state vs NavGraph

Navigation is a `remember { mutableStateOf(Tab.Timer) }` enum in `MainActivity`. There are only two top-level screens (Timer, History), and neither requires deep linking, back-stack management, or arguments. A full `NavHost` + `NavGraph` would add boilerplate with no benefit at this scope.

**With more time:** as the feature set grows (e.g., session detail screen, onboarding, settings) Jetpack Navigation Compose with typed routes becomes worth the cost. The current structure makes that refactor simple — each screen is already a standalone stateless Composable.

### Background / Lifecycle: viewModelScope vs ForegroundService

All sensor collection and the timer loop run in `viewModelScope`. This was sufficient given the feature scope and keeps the manifest, permissions, and service lifecycle simple.

**The deliberate trade-off:** if the user navigates away, the OS may kill the process and monitoring stops. A `ForegroundService` with a sticky notification would keep the session alive in the background. That is the correct production behaviour for a focus-mode app, and it is the main thing that would be added with more time. The `POST_NOTIFICATIONS` permission is already declared and requested precisely because this upgrade is one step away.

### Sensor: MediaRecorder vs AudioRecord

`MediaRecorder` was chosen for noise detection. The recorder's output is `"/dev/null"` — audio is never written to disk. Every 500 ms the coroutine reads `maxAmplitude`, converts to dBFS, and emits `DistractionEvent.Noise` when the result is >= 70 dB.

**Why not `AudioRecord`:** `AudioRecord` gives raw PCM and requires a manual read-buffer loop, making it more complex to implement correctly for simple amplitude thresholding. `MediaRecorder` surfaces `maxAmplitude` directly, with no need to handle audio frames or threading. The downside is that `MediaRecorder` is heavier, does not support concurrent use, and gives less control over the signal.

### Accelerometer: SENSOR_DELAY_NORMAL and threshold

The accelerometer registers at `SENSOR_DELAY_NORMAL` rather than `SENSOR_DELAY_FASTEST`. Each event computes the Euclidean distance between consecutive (x, y, z) triples; a delta > 2.5 m/s² fires `DistractionEvent.Movement`. This rate is intentionally chosen to limit wakeups and battery drain, a higher rate is unnecessary for detecting distraction gestures like picking up the phone.

### CompositeDistractionMonitor

`CompositeDistractionMonitor` holds a list of `DistractionMonitor` instances injected by Hilt. `start()` delegates to all of them and then launches a single coroutine that uses Kotlin's `merge()` operator on their `SharedFlow`s. This means:

- Adding a new sensor type is a one-line change in `SensorModule`.
- The ViewModel observes exactly one flow and is decoupled from every concrete sensor.
- Each monitor manages its own lifecycle independently.

**What still needs to change:** right now the two monitors are injected as individual constructor parameters (`AccelerometerDistractionMonitor`, `MicrophoneDistractionMonitor`), not as a `List<DistractionMonitor>`. To make the composite truly open for extension without modification, the constructor should accept a `List<DistractionMonitor>`.

### Notifications: throttle strategy

`FocusNotificationManager` owns the notification channel (`focus_distraction_channel`) and posts a `NotificationCompat` alert whenever `notifyDistraction(event)` is called. The channel is created once in `FocusGuardApp.onCreate()`, which runs before any Activity or ViewModel is initialized.

In `HomeViewModel`, two separate coroutines collect from `distractionMonitor.events`:
- `sensorJob` — counts every event and updates `HomeUiState`, unthrottled.
- `notificationJob` — posts a notification on the first event, then suppresses further notifications for 2 seconds (`throttleFirst` pattern via a `lastNotifiedAt` timestamp). This prevents alert flooding when noise or motion is continuous, while still informing the user promptly on the first detection.

### Accessibility

Every interactive element has a `contentDescription`. `SessionControls` adds `semantics { role = Role.Button }` to all buttons. `DistractionEventCard` uses `Modifier.semantics(mergeDescendants = true)` so TalkBack reads the card as a single unit. `TimerDisplay` announces elapsed time in a human-readable format ("Elapsed time: X minutes Y seconds") rather than raw "MM:SS".

---

## What Was Intentionally Deprioritized

- **History screen** — `HistoryScreen` is a placeholder. The full data pipeline exists (`FocusRepository.getHistory()` returns a `Flow<List<FocusSession>>`), but no `HistoryViewModel` or list UI was wired up.
- **Error handling** — basic error handling is in place: network failures surface as a `ShowSnackbar` event to the user. There is no retry logic or offline queue. And there is not error types.
- **Tests** — MockK and `kotlinx-coroutines-test` are in the build graph; the test files are stubs. The architecture was designed with testing in mind (pure domain interfaces, injectable fakes), but no actual test cases were written.

---

## What I Would Improve With More Time

1. **ForegroundService** — move sensor collection and the timer into a bound `ForegroundService` with a persistent notification so sessions survive backgrounding. The ViewModel binds to the service and continues to observe state via a shared `StateFlow`.
2. **History screen** — `HistoryViewModel` collecting from `FocusRepository.getHistory()`, displayed in a `LazyColumn` with session duration, timestamp, and distraction count.
3. **Room migration** — replace DataStore + Gson with Room for proper querying, indexes, and multi-table support (e.g., storing individual `DistractionEvent` rows per session).
4. **Real API integration** — swap `FakeFocusApiServiceImpl` for `RetrofitFocusApiServiceImpl` in `AppModule` and implement a retry / offline queue. The `Result<Unit>` is basic, add error types in the future.
5. **Unit tests** — `HomeViewModelTest` using `TestCoroutineScheduler` + `FakeDistractionMonitor` + `FakeFocusRepository`; use-case tests with fakes; DataStore tests with an in-memory `PreferenceDataStore`.

---

## Native Resource Handling

Both sensors acquire OS-level resources that must be explicitly released:

- **Accelerometer** — `SensorManager.registerListener()` is called in `start()` at `SENSOR_DELAY_NORMAL` to limit CPU wakeups. `unregisterListener()` is called in `stop()`. `CompositeDistractionMonitor` ensures `stop()` is called when the ViewModel is cleared (`HomeViewModel.onCleared()` → `distractionMonitor.stop()`), preventing the listener from leaking after the ViewModel is destroyed.
- **Microphone** — `MediaRecorder.start()` acquires the audio focus. `stop()` / `release()` are called inside a `try/finally` block to guarantee the recorder is released even if an exception is thrown mid-session. The output file is `/dev/null` — no audio data is ever written to storage or transmitted.
- **Coroutine scopes** — all coroutines are launched on the `CoroutineScope` passed to `start()` (the ViewModel's `viewModelScope`). When the scope is cancelled, all sensor collection coroutines are cancelled automatically. `stop()` additionally cancels any residual jobs and calls the hardware release methods for deterministic cleanup.

---

## Testability

The architecture was built around three principles that enable testing without an Android device:

1. **Dependency inversion** — `DistractionMonitor`, `FocusRepository`, `FocusApiService`, and `LocalSessionDataSource` are all interfaces. Any test can inject a fake/mock via Hilt's `@TestInstallIn` or by constructing the class under test directly.
2. **No side effects in domain or ViewModels** — use cases are pure functions over their injected repositories. `HomeViewModel` never touches `Context`, `SensorManager`, or any Android system service directly. All hardware interaction is behind `DistractionMonitor`, which is trivially faked: a `FakeDistractionMonitor` that emits events from a `MutableSharedFlow` in a `TestScope` is all that is needed to drive the ViewModel.
---

## Production Scaling Considerations

| Concern | Current state | Production approach |
|---|---|---|
| **Session persistence** | DataStore + Gson, full-list rewrite on each save | Room with a `sessions` table; index on `startTime`; pagination via `PagingSource` |
| **Remote sync** | Fire-and-forget `createSession()`, result ignored | WorkManager `UploadSessionWorker` with exponential backoff; optimistic local write + server reconciliation |
| **Sensor processing** | In-process, `viewModelScope` | Bound `ForegroundService`; sensor collection on a dedicated `Dispatchers.Default` coroutine |
| **Distraction detection** | Fixed hardcoded thresholds | Per-user calibration baseline |
| **Multi-sensor extensibility** | `CompositeDistractionMonitor` with DI-injected list | Plugin registry of `DistractionMonitor` providers; feature flags to enable/disable per sensor |
| **Observability** | None | Firebase Crashlytics for crashes; custom events to Analytics for session start/stop/distraction; server-side session aggregation |
| **Battery** | `SENSOR_DELAY_NORMAL`, 500 ms mic polling | Adaptive polling rate based on battery state; `WorkManager` for deferred uploads |

---

## Permissions

| Permission | Reason |
|---|---|
| `POST_NOTIFICATIONS` | Distraction and session-complete alerts |
| `INTERNET` | Remote API calls via Retrofit |
| `RECORD_AUDIO` | Microphone-based noise detection |
