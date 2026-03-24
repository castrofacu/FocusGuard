# FocusGuard

> A context-aware productivity timer for Android that monitors physical distractions in real time.

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Min SDK](https://img.shields.io/badge/minSdk-26-blue.svg)](https://developer.android.com/studio/releases/platforms)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.x-purple.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg)](https://developer.android.com/jetpack/compose)
[![Spring Boot](https://img.shields.io/badge/Backend-Spring%20Boot-green.svg)](https://spring.io/projects/spring-boot)

FocusGuard is an Android app — part personal productivity tool, part Android development playground — that lets you start timed focus sessions and alerts you when it detects physical distractions like movement (accelerometer) or ambient noise (microphone). Sessions are stored locally with Room, synced to a remote API via WorkManager, and protected by Firebase Authentication with support for anonymous and Google Sign-In.

---

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [Running Tests](#running-tests)
- [Backend](#backend)
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
| Backend | Spring Boot 4 (Kotlin), Spring Data JPA, H2 (dev), Hibernate Validator |
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
cd android
./gradlew assembleDebug
```

Or open the project in Android Studio and run the `app` configuration on a device or emulator (API 26+).

> **Note:** The app currently uses `FakeFocusApiServiceImpl` for remote API calls. To use the real backend, see the [Backend](#backend) section.

---

## Running Tests

```bash
cd android

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

## Backend

The `backend` directory contains a Spring Boot REST API that the Android app syncs sessions to.

### Stack

| Layer | Technology |
|-------|------------|
| Framework | Spring Boot 4 (Kotlin) |
| Persistence | Spring Data JPA + Hibernate |
| Database | H2 in-memory (dev) |
| Validation | Hibernate Validator (`spring-boot-starter-validation`) |

### API

| Method | Path | Description | Success | Error |
|--------|------|-------------|---------|-------|
| `POST` | `/sessions` | Create a session | 201 Created | 400 Bad Request |
| `GET` | `/sessions` | List all sessions | 200 OK | — |
| `GET` | `/sessions/{id}` | Get session by ID | 200 OK | 404 Not Found |

The client (Android) generates the session ID before local Room storage. The server accepts that ID as-is, consistent with the offline-first architecture.

### Running locally

```bash
cd backend
./gradlew bootRun
```

The API is available at `http://localhost:8080`. An H2 web console is available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:focusguarddb`, username: `sa`, no password).

---

## Roadmap

- [ ] Migrate sensor collection to a bound `ForegroundService`
- [ ] Adopt Navigation Compose with a proper `NavHost`
- [ ] Wire the app to the real backend (replace `FakeFocusApiServiceImpl`)
- [ ] Glance API home-screen widget
- [ ] Pomodoro-style configurable intervals
- [ ] Migrate backend DB from H2 to PostgreSQL for production
