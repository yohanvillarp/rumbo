# Rumbo — Serene Android Activity & Process Tracker

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
- **`:core:model`**: Pure Kotlin domain models (`Process`, `Task`, `Milestone`, `WorkSession`, `ProgressEntry`, `WeeklyGoal`, `UserProfile`, `AppLanguage`). Zero Android or Room framework dependencies.
- **`:core:database`**: Local persistence using Room 2.8 (`RumboDatabase`, DAOs, Entities, Room TypeConverters).
- **`:core:data`**: Repositories (`ProcessRepository`, `TaskRepository`, etc.), model mappers, and `RumboPreferencesDataSource` (DataStore Preferences).
- **`:core:designsystem`**: Reusable Design System tokens (`Color`, `Typography`, `Shape`, `Spacing`, `Elevation`, `AnimationTokens`), brand `RumboLogo` & Mascot, and comprehensive UI components (celebration cards, empty states, task items, process cards).
- **`:core:navigation`**: Strongly-typed navigation contracts and route definitions.
- **`:feature:onboarding`**: Streamlined onboarding experience with brand welcome panel and interactive tutorial steps.
- **`:feature:home`**: Dynamic time-based greeting, featured active process card, top active processes, and quick actions.
- **`:feature:processes`**: Process hierarchy (parent and sub-processes), empty-process completion validation, star favorites, and detail lifecycle management.
- **`:feature:tasks`**: Task management with process ownership rules (including default "General" process), status filtering, and due date validation.
- **`:feature:progress`**: Work session timer with adjustment controls, direct task completion toggle, qualitative progress logging, and contribution analytics.
- **`:feature:settings`**: App configuration, dark theme modes, and multi-language support (Spanish, English, Portuguese) with interactive switcher and manual confirmation.

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
- **Testing Architecture**: JUnit 4, Turbine 1.2.0, Google Truth 1.4.4, Coroutines Test
- **Localization**: Multi-language support (Spanish, English, Portuguese) with in-app runtime switching

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
