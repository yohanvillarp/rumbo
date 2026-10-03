# Guía de Contribución para Rumbo

¡Gracias por tu interés en contribuir a **Rumbo**! Para mantener la calidad del código, la consistencia arquitectónica y una colaboración eficiente, sigue las pautas descritas en este documento.

---

## 1. Visión General de la Arquitectura

Rumbo sigue un patrón estricto de arquitectura limpia multi-módulo construido con **Kotlin**, **Jetpack Compose**, **Material 3**, **Navigation Compose**, **Room**, **DataStore Preferences** y **Hilt**.

```
:app                     -> Application, MainActivity, NavHost principal, configuración Hilt
:core:model              -> Modelos de dominio puros (Process, Task, UserProgress, UserSettings)
:core:common             -> Interfaz sellada Result<T>, proveedores de dispatchers de Corrutinas
:core:designsystem       -> RumboTheme, componentes de UI Material 3, Iconos vectoriales
:core:navigation         -> Rutas de navegación y contratos de destino
:core:database           -> Base de datos Room, entidades, DAOs, convertidores
:core:data               -> Repositorios (interfaces e impls), DataStore, Mappers
:feature:<nombre>        -> Composables de UI, ViewModel, UiState, UiEvent
```

---

## 2. Reglas Obligatorias de Código

1. **Sin Emojis en la Interfaz (UI)**:
   - Todas las interfaces deben utilizar iconos vectoriales (`ImageVector` de Material Symbols o drawables vectoriales personalizados). Queda estrictamente prohibido el uso de emojis en diseños y textos de UI.
2. **Encapsulamiento de Entidades Room y DAOs**:
   - Los ViewModels y Composables de pantalla **nunca** deben hacer referencia a entidades de Room (`*Entity`) ni DAOs directamente.
   - Los ViewModels interactúan exclusivamente con las interfaces de repositorio definidas en `:core:data`.
3. **Capa de Dominio Pura (`:core:model`)**:
   - Los modelos en `:core:model` no deben tener ninguna dependencia de clases de Android, Room o Compose.
4. **Inyección de Dependencias**:
   - Usar **Hilt** para toda la inyección de dependencias (`@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`, `@Inject constructor`).
5. **Gestión de Estado**:
   - Los ViewModels exponen el estado de UI mediante `StateFlow` y procesan las acciones del usuario a través de interfaces selladas `UiEvent`.

---

## 3. Flujo de Git y Estrategia de Ramas

Mantenemos un flujo de trabajo estructurado en Git:

$$\text{feature/nombre-de-tu-feature} \longrightarrow \text{develop} \longrightarrow \text{main}$$

- **`main`**: Código estable y listo para producción.
- **`develop`**: Rama de integración para próximas funcionalidades.
- **`feature/<nombre>`**: Rama de desarrollo para tareas o módulos específicos.

### Pasos para Contribuir:
1. Crea una rama de feature partiendo de `develop`:
   ```bash
   git checkout develop
   git pull
   git checkout -b feature/nombre-de-tu-feature
   ```
2. Realiza tus commits de manera modular y lógica.
3. Verifica que la compilación pase limpiamente:
   ```bash
   ./gradlew assembleDebug
   ```
4. Sube tu rama y abre un Pull Request dirigido a `develop`.

---

## 4. Convención de Mensajes de Commit

Los mensajes de commit deben ser claros, concisos y modulares. Utiliza prefijos estructurados:

- `feat(módulo): descripción` — Nueva característica o componente
- `fix(módulo): descripción` — Corrección de errores
- `refactor(módulo): descripción` — Refactorización de código sin cambiar funcionalidad
- `docs: descripción` — Cambios en la documentación
- `build(gradle): descripción` — Configuración de build o actualización de dependencias

### Ejemplos:
- `feat(core:model): agregar modelos de dominio Process y Task`
- `feat(feature:home): implementar pantalla principal y HomeViewModel`
- `build(gradle): configurar dependencias de Hilt y Room`

---

¡Gracias por mantener Rumbo limpio, modular y mantenible!
