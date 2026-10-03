# Rumbo Home Feature Implementation

The Home feature for **Rumbo** has been fully implemented in `:feature:home`, prioritizing the central user question: **"¿Qué debería continuar ahora?"** ("What should I continue now?").

## 1. Feature Components & Sections (`:feature:home`)

1. **User Greeting Header**:
   - Time-based greeting ("Buenos días, {nombre}", "Buenas tardes, {nombre}", "Buenas noches, {nombre}") calculated purely from local hour (`05:00-11:59`, `12:00-18:59`, `19:00-04:59`).
   - Retrieves saved user name from DataStore/UserProfile via `SettingsRepository`.
   - Strictly NO emojis.

2. **Section 1: Continue (Featured Process Card)**:
   - Displays the active process with the most relevant next action (`nextAction`).
   - Shows name, next action description, and primary `"Continuar"` button with `Icons.AutoMirrored.Filled.ArrowForward`.

3. **Section 2: Active Processes**:
   - Displays up to 3 active processes using `RumboProcessCard` (showing name, status, direct cost if > 0, and next action).

4. **Section 3: Today Tasks**:
   - Displays relevant pending tasks for today using `RumboTaskItem` (showing title, process, estimated duration if present, cost ONLY if > 0, and status checkbox).

5. **Section 4: Quick Actions**:
   - Clean, serene action buttons with Material Symbols icons:
     - `+ Tarea` (`Icons.Default.AddTask`)
     - `+ Proceso` (`Icons.Default.FolderOpen`)
     - `Evaluar Progreso` (`Icons.Default.TrendingUp`)
     - `Iniciar Sesión` (`Icons.Default.PlayArrow`)

6. **Architecture & State Management**:
   - `HomeUiState` sealed interface (`Loading`, `Content`, `Empty`, `Error`).
   - `HomeViewModel` inherits zero Room/DAO dependencies and interacts exclusively with Repository interfaces (`ProcessRepository`, `TaskRepository`, `SettingsRepository`).

---

## 2. Previews & Unit Tests
- Added Light and Dark Mode `@Preview` wrappers (`HomeScreenPreviewLight`, `HomeScreenPreviewDark`).
- Added ViewModel unit tests (`HomeViewModelTest.kt` with fake repositories):
  - Greeting time selection logic for morning, afternoon, and night.
  - Empty state when no processes or tasks exist.
  - Active processes filtering (max 3) and Continue featured process selection.
  - Task completion toggling.
- **Test Results**: **4 passed, 0 failed**.
- **Build Result**: `./gradlew :app:assembleDebug` **BUILD SUCCESSFUL**.

---

## 3. Active Feature Branch
- Branch: **`feature/home-screen`**
- Pushed to `origin/feature/home-screen` ready for manual Pull Request creation to `develop`.
- Zero direct pushes/merges to `develop` or `main`.
