# FocusGuard

Context-aware focus mode app for Android. Detects distractions via accelerometer and microphone, tracks focus sessions, and persists history across app restarts.

## Architecture

Clean Architecture + MVVM with Jetpack Compose, Hilt, and Coroutines/Flow.
```
domain/      — models, interfaces, use cases (no Android deps)
data/        — sensors, remote, local, repository impl
di/          — Hilt modules
presentation/ — Compose screens + ViewModels
notification/ — FocusNotificationManager
```

## Tech Decisions

### Persistence: DataStore + Gson vs Room

<!-- TODO: fill in -->

### API abstraction: FakeFocusApiServiceImpl vs MockInterceptor

<!-- TODO: fill in -->

### Navigation: simple state vs NavGraph

<!-- TODO: fill in -->

### Background / Lifecycle: viewModelScope vs ForegroundService

<!-- TODO: fill in -->

### Sensor: MediaRecorder vs AudioRecord

<!-- TODO: fill in -->

### Notifications: debounce(2000L) throttle strategy

<!-- TODO: fill in -->

### Accessibility

<!-- TODO: fill in -->



CompositeDistractionMonitor -> add a list of distraction monitors

SENSOR_DELAY_NORMAL -> For not drain battery

Guardo la data de la session en los 2 pero nunca hago get de la api

Data store para ahorrrar tiempo

## Permissions

| Permission | Reason |
|---|---|
| `POST_NOTIFICATIONS` | Distraction and session-complete alerts |
| `INTERNET` | Remote API calls via Retrofit |
| `RECORD_AUDIO` | Microphone-based noise detection |

## Building

```bash
./gradlew assembleDebug
```
