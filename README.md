# FocusGuard

> A context-aware productivity timer for Android that monitors physical distractions in real time.

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Min SDK](https://img.shields.io/badge/minSdk-26-blue.svg)](https://developer.android.com/studio/releases/platforms)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.x-purple.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg)](https://developer.android.com/jetpack/compose)

FocusGuard is an Android app — part personal productivity tool, part Android development playground — that lets you start timed focus sessions and alerts you when it detects physical distractions like movement (accelerometer) or ambient noise (microphone). Sessions are stored locally with Room, synced to a remote API via WorkManager, and protected by Firebase Authentication with support for anonymous and Google Sign-In.

---

<p align="center">
  <img width="32%" src="https://github.com/user-attachments/assets/6e3ad83d-8e3d-4399-8a11-ccd6a6e6a0f1" />
  <img width="32%" src="https://github.com/user-attachments/assets/4a4113a3-b143-4462-996c-5a4658f89cba" />
  <img width="32%" src="https://github.com/user-attachments/assets/06fee44f-c615-4ab2-9f1e-526132c71a05" />

</p>

<p align="center">
  <img width="32%" alt="Light Mode 1" src="https://github.com/user-attachments/assets/c2b31253-36d5-4e79-aa49-3bff06adb449" />
  <img width="32%" alt="Light Mode 2" src="https://github.com/user-attachments/assets/47af9d30-3617-4231-baf0-10cc01d12e5a" />
  <img width="32%" alt="Light Mode 3" src="https://github.com/user-attachments/assets/06b41b58-3766-4e48-afad-d4815495b41b" />
</p



---

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [Running Tests](#running-tests)
- [Key Design Decisions](#key-design-decisions)

---

## Features

- **Focus Timer** — Start, pause, resume and stop timed focus sessions
- **Distraction Detection** — Real-time monitoring via accelerometer (movement > 2.5 m/s²) and microphone (noise ≥ 70 dB)
- **Distraction Notifications** — Rate-limited system notifications (2 s debounce window) alerting the user without spamming
- **Session History** — Grouped, annotated list of past sessions with per-session stats (duration, distraction count) and an at-a-glance summary header (total sessions, total focus minutes, avg distractions)
- **Offline-First Sync** — Sessions saved to Room immediately; background WorkManager job syncs to remote API whenever connectivity is available
- **Firebase Authentication** — Anonymous sign-in out of the box; optional upgrade to a full Google account with account-linking support
- **Mixed UI Patterns** — Login screen uses **MVI** (`LoginContract` with State/Intent/Effect); Home and History screens use **MVVM** (`StateFlow` + `Channel` events)

---

## Architecture

FocusGuard follows **Clean Architecture** with a strict three-layer separation and **Hilt** for dependency injection.

```
┌──────────────────────────────────────────────────────────────────────┐
│                            Presentation                              │
│  LoginScreen (MVI)   │  HomeScreen (MVVM)   │  HistoryScreen (MVVM)  │
│  LoginViewModel      │  HomeViewModel       │  HistoryViewModel      │
└────────────────────────────┬─────────────────────────────────────────┘
                             │ Use Cases
┌────────────────────────────▼────────────────────────────┐
│                          Domain                         │
│  StartFocusSessionUseCase  │  StopFocusSessionUseCase   │
│  GetHistoryUseCase         │  Auth Use Cases (×4)       │
│  FocusRepository (i/f)     │  DistractionMonitor (i/f)  │
│  AuthRepository (i/f)      │  TimeProvider (i/f)        │
└────────────────────────────┬────────────────────────────┘
                             │ Implementations
┌────────────────────────────▼────────────────────────────┐
│                          Data                           │
│  Room (SessionDao, AppDatabase)                         │
│  Retrofit + OkHttp (FocusRetrofitApi)                   │
│  WorkManager (SyncSessionsWorker)                       │
│  SensorManager (AccelerometerDistractionMonitor)        │
│  MediaRecorder (MicrophoneDistractionMonitor)           │
│  Firebase Auth (AuthRepositoryImpl)                     │
│  CredentialManager (GoogleCredentialDataSource)         │
└─────────────────────────────────────────────────────────┘
```

### Pattern Breakdown

| Screen | Pattern | State holder | One-shot events |
|--------|---------|-------------|----------------|
| Login | **MVI** | `MutableStateFlow<LoginContract.State>` | `Channel<LoginContract.Effect>` |
| Home | **MVVM** | `MutableStateFlow<HomeUiState>` | `Channel<HomeEvent>` |
| History | **MVVM** | `StateFlow<HistoryUiState>` (stateIn) | — |

---

## Tech Stack

| Category | Technology |
|----------|------------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | Clean Architecture, MVVM, MVI |
| DI | Hilt |
| Async | Coroutines + Flow |
| Local DB | Room |
| Networking | Retrofit + OkHttp + Gson |
| Background Work | WorkManager (`CoroutineWorker`, `@HiltWorker`) |
| Authentication | Firebase Auth (Anonymous + Google Sign-In via CredentialManager) |
| Crash Reporting | Firebase Crashlytics |
| Sensors | SensorManager (Accelerometer), MediaRecorder (Microphone) |
| Notifications | NotificationManager + NotificationChannel |
| Testing | JUnit 4, MockK, kotlinx-coroutines-test, Compose UI Test |

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- JDK 17
- A Firebase project with **Authentication** and **Crashlytics** enabled

### 1. Clone the repository

```bash
git clone https://github.com/facucastro/FocusGuard.git
cd FocusGuard
```

### 2. Firebase setup

1. Create a project at [Firebase Console](https://console.firebase.google.com/)
2. Add an Android app with package name `com.facucastro.focusguard`
3. Download `google-services.json` and place it in `app/`
4. Enable **Anonymous** and **Google** sign-in methods in Firebase Auth

### 3. Google Sign-In (Credential Manager)

1. In your Firebase project, copy the **Web Client ID** from the Google Sign-In provider settings
2. Open `local.properties` and add:
   ```properties
   GOOGLE_WEB_CLIENT_ID=your-web-client-id-here
   ```

### 4. Build & run

```bash
./gradlew assembleDebug
```

Or open the project in Android Studio and run the `app` configuration on a device or emulator (API 26+).

> **Note:** The app currently uses `FakeFocusApiServiceImpl` for remote API calls. No real backend is required to run the app.

---

## Running Tests

```bash
# Unit tests
./gradlew test

# Instrumented (UI) tests — requires connected device or emulator
./gradlew connectedAndroidTest
```

---

## Key Design Decisions

**Offline-first sync**  
Sessions are always written to Room first. A `WorkManager` `OneTimeWorkRequest` with `NetworkType.CONNECTED` constraint and exponential backoff is enqueued after every save, guaranteeing sync even if the device is offline at the time of the session.

**Sensor monitoring in ViewModel scope**  
Sensor collection runs in `viewModelScope` for simplicity. This is a known trade-off: the OS may kill the session if the app is backgrounded for an extended period. A `ForegroundService` is the correct long-term solution (tracked in the roadmap).

**MVI for Login, MVVM for Home/History**  
Login has a strict request→response lifecycle (idle → loading → success/error) which maps naturally to MVI's unidirectional flow and explicit `Effect` for one-time navigation. Home and History have more continuous, evolving state that is well-served by MVVM with a `StateFlow` and a side-effect `Channel`.

**Anonymous-to-Google account linking**  
When an anonymous user signs in with Google, `AuthRepositoryImpl` first attempts `linkWithCredential`. If the Google account already exists (`FirebaseAuthUserCollisionException`), it falls back to a direct `signInWithCredential`, preserving a seamless UX.

**Composite sensor monitor**  
`CompositeDistractionMonitor` merges the `SharedFlow`s from both `AccelerometerDistractionMonitor` and `MicrophoneDistractionMonitor` using `Flow.merge()`, exposing a single `DistractionMonitor` interface to the ViewModel.

---

## Roadmap

- [ ] Migrate sensor collection to a bound `ForegroundService`
- [ ] Adopt Navigation Compose with a proper `NavHost`
- [ ] Implement real backend integration (replace `FakeFocusApiServiceImpl`)
- [ ] Pomodoro-style configurable intervals
