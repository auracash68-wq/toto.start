# TOTO Smart BMS Client

A production-ready Android application built with Jetpack Compose, Kotlin Coroutines, Flow, and Material Design 3. This client connects with smart Battery Management Systems (BMS) over Bluetooth Low Energy (BLE) to display real-time telemetry, and synchronizes system status, alerts, and metrics to a secure cloud database in real-time.

## Features

- **Bluetooth Low Energy (BLE) BMS Integration**: Automated background scanning, parsing, and telemetry streaming for smart BMS hardware.
- **Real-Time Telemetry Dashboard**: Modern M3 dashboard displaying voltage, current, state of charge (SoC), individual cell voltages, and active protection alerts.
- **Secure Cloud Sync (Supabase)**: Real-time user authentication and telemetry data persistence. Automatically falls back gracefully to local preview mode if credentials are omitted.
- **Secured Credentials**: All API credentials are injected at compile-time via AI Studio Secrets (`.env` and `BuildConfig`), keeping sensitive keys safe and untracked.

## Project Architecture

The codebase adheres to the **Model-View-ViewModel (MVVM)** design pattern and **Clean Architecture** principles:

```
├── app
│   ├── src
│   │   └── main
│   │       ├── AndroidManifest.xml
│   │       ├── java/com/example
│   │       │   ├── MainActivity.kt           # App container and theme setup
│   │       │   ├── data                      # Data Layer (BLE, Supabase, Storage)
│   │       │   │   ├── BluetoothBmsManager.kt # Core BLE scanning and parsing engine
│   │       │   │   └── SupabaseManager.kt     # Real-time Cloud Auth & Telemetry sync
│   │       │   └── ui                        # Presentation Layer
│   │       │       ├── screens               # Screen composables (Dashboard, Login, Logs)
│   │       │       └── theme                 # Theme, colors, typography (M3)
│   │       └── res                           # Android Resource xml files
│   └── build.gradle.kts                      # App-module Gradle build config
├── gradle
│   ├── libs.versions.toml                    # Centralized Version Catalog
│   └── wrapper                               # Standard Gradle Wrapper distribution
├── build.gradle.kts                          # Project-level Gradle build config
├── settings.gradle.kts                       # Settings and repository catalog
└── gradle.properties                         # Gradle JVM configurations
```

## Getting Started

### Configuration
Credentials like `SUPABASE_URL` and `SUPABASE_ANON_KEY` are read securely from the environment using **Secrets Management**.
1. Create a `.env` file at the root.
2. Define your variables as follows:
   ```env
   SUPABASE_URL=https://your-project.supabase.co
   SUPABASE_ANON_KEY=your-anon-key
   ```

### Building the Project
You can build the project from Android Studio or using standard Gradle:

- **Assemble Debug APK**:
  ```bash
  ./gradlew assembleDebug
  ```

- **Assemble Release APK**:
  ```bash
  ./gradlew assembleRelease
  ```
