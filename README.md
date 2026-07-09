# TOTO Smart BMS Client

A production-ready Android application built with Jetpack Compose, Kotlin Coroutines, Flow, and Material Design 3. This client connects with smart Battery Management Systems (BMS) over Bluetooth Low Energy (BLE) to display real-time telemetry.

## Features

- **Bluetooth Low Energy (BLE) BMS Integration**: Automated background scanning, parsing, and telemetry streaming for smart BMS hardware.
- **Real-Time Telemetry Dashboard**: Modern M3 dashboard displaying voltage, current, state of charge (SoC), individual cell voltages, and active protection alerts.

## Project Architecture

The codebase adheres to the **Model-View-ViewModel (MVVM)** design pattern and **Clean Architecture** principles:

```
├── app
│   ├── src
│   │   └── main
│   │       ├── AndroidManifest.xml
│   │       ├── java/com/example
│   │       │   ├── MainActivity.kt           # App container and theme setup
│   │       │   ├── data                      # Data Layer (BLE, Storage)
│   │       │   │   └── BluetoothBmsManager.kt # Core BLE scanning and parsing engine
│   │       │   └── ui                        # Presentation Layer
│   │       │       ├── screens               # Screen composables (Dashboard, Scan)
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
