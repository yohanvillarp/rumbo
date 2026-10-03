# 🐺 Rumbo — Serene Android Activity & Process Tracker

[![Android CI](https://github.com/yohanvillarp/rumbo/actions/workflows/android_ci.yml/badge.svg)](https://github.com/yohanvillarp/rumbo/actions/workflows/android_ci.yml)
[![CodeQL](https://github.com/yohanvillarp/rumbo/actions/workflows/codeql.yml/badge.svg)](https://github.com/yohanvillarp/rumbo/actions/workflows/codeql.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-teal.svg)](LICENSE)

**Rumbo** is a minimalist, tranquil, and modern Android application designed for long-term process tracking, concentration work sessions, and qualitative progress evaluation.

Unlike traditional task managers or gamified habit trackers, Rumbo focuses on answering the fundamental question: **"¿Qué debería continuar ahora?"** (*"What should I continue now?"*).

---

## 🏛️ Architecture & Module Breakdown

Rumbo is built following **Now in Android** best practices with a strict multi-module architecture:

```
                      +-------------------+
                      |       :app        |
                      +---------+---------+
                                |
        +-----------------------+-----------------------+
        |                       |                       |
+-------v-------+       +-------v-------+       +-------v-------+
|  :feature:*   |       | :core:design- |       | :core:navig-  |
| (home, tasks, |       |    system     |       |    ation      |
| processes,    |       +-------+-------+       +-------+-------+
| onboarding,   |               |                       |
| progress,     |               |                       |
| settings)     |               |                       |
+-------+-------+               |                       |
        |                       |                       |
        +-----------------------+-----------------------+
                                |
                        +-------v-------+
                        |  :core:data   |
                        +-------+-------+
                                |
               +----------------+----------------+
               |                                 |
       +-------v-------+                 +-------v-------+
       | :core:database|                 |  :core:model  |
       |  (Room DAOs)  |                 | (Pure Domain) |
       +---------------+                 +---------------+
```

### Modules Map:
- **`:app`**: Application entry point, `MainActivity`, `MainViewModel`, and responsive layout containers (`NavigationBar` vs `NavigationRail`).
- **`:core:model`**: Pure Kotlin domain models (`Process`, `Task`, `Milestone`, `WorkSession`, `ProgressEntry`, `WeeklyGoal`, `UserProfile`). Zero Android or Room framework dependencies.
- **`:core:database`**: Local persistence using Room 2.8 (`RumboDatabase`, DAOs, Entities).
- **`:core:data`**: Repositories (`ProcessRepository`, `TaskRepository`, etc.), model mappers, and `RumboPreferencesDataSource` (DataStore Preferences).
- **`:core:designsystem`**: Reusable Design System tokens (`Color`, `Typography`, `Shape`, `Spacing`, `Elevation`, `AnimationTokens`), `RumboMascot` (minimalist explorer wolf vector), and 12 UI components.
- **`:core:navigation`**: Strongly-typed navigation contracts and route definitions.
- **`:feature:onboarding`**: Streamlined single-screen onboarding experience.
- **`:feature:home`**: Dynamic time-based greeting, featured active process card, top active processes, and quick actions.
- **`:feature:processes`**: Process list, creation form, and process detail screen with explicit lifecycle management.
- **`:feature:tasks`**: Task management with process ownership rules, status filtering, and cost presentation rules.
- **`:feature:progress`**: Work session timer, standalone qualitative progress logging, contribution calendar, and separated task vs process analytics.
- **`:feature:settings`**: App configuration and user profile settings.

---

## 🛠️ Technology Stack

- **Language**: Kotlin 2.1.10
- **Build System**: Gradle 9.2.1 + Version Catalog (`libs.versions.toml`)
- **UI Framework**: Jetpack Compose + Material 3
- **Asynchronous Flow**: Kotlin Coroutines 1.9 + `StateFlow` / `Flow`
- **Database**: Room 2.8.5 with Database Callbacks
- **Preferences**: DataStore Preferences 1.1.1
- **Dependency Injection**: Hilt 2.60.1 + KSP 2.1.10
- **Navigation**: Jetpack Navigation Compose 2.8.3

---

## 🚀 Building & Running

### Prerequisites
- Android Studio Ladybug (2024.2) or newer.
- JDK 17 or JDK 21.
- Android SDK 36 (Min SDK 26).

### Gradle Commands
```bash
# Build Debug APK
./gradlew :app:assembleDebug

# Run Unit Tests across all modules
./gradlew testDebugUnitTest

# Run Android Lint
./gradlew lintDebug
```

---

## 📜 License & Security

- Distributed under the **MIT License**. See [`LICENSE`](LICENSE) for details.
- Security policies and vulnerability reporting guidelines can be found in [`SECURITY.md`](SECURITY.md).
