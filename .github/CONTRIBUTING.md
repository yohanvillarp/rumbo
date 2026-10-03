# Contributing to Rumbo

Thank you for your interest in contributing to **Rumbo**! To maintain code quality, architectural consistency, and seamless collaboration, please follow the guidelines outlined in this document.

---

## 1. Architecture Overview

Rumbo follows a strict multi-module, clean architecture pattern built with **Kotlin**, **Jetpack Compose**, **Material 3**, **Navigation Compose**, **Room**, **DataStore Preferences**, and **Hilt**.

```
:app                     -> Application, MainActivity, Root NavHost, Hilt setup
:core:model              -> Pure domain models (Process, Task, UserProgress, UserSettings)
:core:common             -> Result<T> sealed interface, Coroutine dispatchers
:core:designsystem       -> RumboTheme, Material 3 UI components, Vector icons
:core:navigation         -> Navigation routes & destination contracts
:core:database           -> Room database, entities, DAOs, converters
:core:data               -> Repositories (interfaces & impls), DataStore, Mappers
:feature:<feature_name>  -> Feature UI Composables, ViewModel, UiState, UiEvent
```

---

## 2. Mandatory Coding Rules

1. **No Emojis in the UI**:
   - All user-facing interfaces must use vector icons (`ImageVector` from Material Symbols or custom vector drawables). Emojis are strictly prohibited in layouts and UI strings.
2. **Encapsulate Room Entities & DAOs**:
   - ViewModels and Screen Composables must **never** reference Room entities (`*Entity`) or DAOs directly.
   - ViewModels interact exclusively with Repository interfaces defined in `:core:data`.
3. **Pure Domain Layer (`:core:model`)**:
   - Models in `:core:model` must have zero dependencies on Android, Room, or Compose framework classes.
4. **Dependency Injection**:
   - Use **Hilt** for all dependency injection (`@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`, `@Inject constructor`).
5. **State Management**:
   - ViewModels expose UI state using `StateFlow` and process user actions via sealed `UiEvent` interfaces.

---

## 3. Git Branching & Workflow Strategy

We enforce a structured Git branching strategy:

$$\text{feature/your-feature-name} \longrightarrow \text{develop} \longrightarrow \text{main}$$

- **`main`**: Production-ready, stable codebase.
- **`develop`**: Integration branch for upcoming features and releases.
- **`feature/<feature-name>`**: Branch for developing specific tasks or modules.

### Steps to Contribute:
1. Create a feature branch off `develop`:
   ```bash
   git checkout develop
   git pull
   git checkout -b feature/your-feature-name
   ```
2. Commit your changes in modular, logical commits.
3. Verify your build passes cleanly:
   ```bash
   ./gradlew assembleDebug
   ```
4. Push your branch and open a Pull Request targeting `develop`.

---

## 4. Commit Message Conventions

Commit messages must be clear, concise, and modular. Use structured prefixes:

- `feat(module): description` — New feature or component
- `fix(module): description` — Bug fix
- `refactor(module): description` — Code refactoring without changing functionality
- `docs: description` — Documentation changes
- `build(gradle): description` — Build configuration or dependency updates

### Examples:
- `feat(core:model): add Process and Task domain models`
- `feat(feature:home): implement HomeScreen layout and HomeViewModel`
- `build(gradle): configure Hilt and Room dependencies`

---

Thank you for keeping Rumbo clean, modular, and maintainable!
