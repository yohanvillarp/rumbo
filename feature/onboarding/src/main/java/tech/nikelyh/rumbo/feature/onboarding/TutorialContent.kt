package tech.nikelyh.rumbo.feature.onboarding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import tech.nikelyh.rumbo.core.designsystem.component.MascotState

/**
 * Proveedor de contenido del tutorial interactivo y guía de Rumbo.
 * Diseñado bajo el patrón de registro escalable para admitir fácilmente
 * nuevos pasos a medida que el sistema agregue funcionalidades.
 */
object TutorialContent {
    val steps: List<TutorialStep> = listOf(
        TutorialStep(
            id = "processes",
            title = "Procesos y Subprocesos",
            subtitle = "Estructura tus metas y actividades",
            description = "Los procesos son tus proyectos o áreas clave de vida. Puedes anidar subprocesos, asignar colores e hitos para darles orden.",
            keyPoints = listOf(
                "Proceso General siempre listo para tus actividades diarias",
                "Crea jerarquías de procesos principales y secundarios",
                "Define costos iniciales y hitos de avance",
                "Puedes finalizar y reabrir procesos cuando lo necesites"
            ),
            icon = Icons.Default.AccountTree,
            mascotState = MascotState.FOCUSED
        ),
        TutorialStep(
            id = "tasks",
            title = "Tareas y Límites",
            subtitle = "Acciones claras con día y hora de cierre",
            description = "Cada tarea pertenece a un proceso. Puedes fijar fecha y hora límite, costos directos y prioridades para saber qué sigue.",
            keyPoints = listOf(
                "Fecha y hora límite configurables con un solo toque",
                "Asignación de costos para monitorear tu inversión",
                "Filtrado rápido por prioridad y proceso",
                "Cálculo automático de duración total al culminar"
            ),
            icon = Icons.AutoMirrored.Filled.Assignment,
            mascotState = MascotState.DEFAULT
        ),
        TutorialStep(
            id = "sessions",
            title = "Sesiones de Enfoque",
            subtitle = "Medición real de tu tiempo invertido",
            description = "Inicia el temporizador en cualquier tarea para registrar el tiempo que realmente dedicas. Tu tiempo sigue corriendo incluso si sales de la aplicación.",
            keyPoints = listOf(
                "Cronómetro persistente en segundo plano",
                "Registra notas de lo que lograste en cada bloque",
                "El tiempo acumulado se respeta como base al marcar completada",
                "Permite pausar o cancelar sesiones sin perder el control"
            ),
            icon = Icons.Default.Timer,
            mascotState = MascotState.FOCUSED
        ),
        TutorialStep(
            id = "progress",
            title = "Métricas e Inversión",
            subtitle = "Conoce a fondo a dónde va tu tiempo",
            description = "Visualiza gráficos de inversión de tiempo y dinero por proceso, facilitando la toma de decisiones serenas y conscientes.",
            keyPoints = listOf(
                "Resumen diario y semanal de tiempo trabajado",
                "Distribución de costos acumulados por proyecto",
                "Historial detallado de todas tus sesiones de enfoque"
            ),
            icon = Icons.Default.TrendingUp,
            mascotState = MascotState.SUCCESS
        )
    )
}
