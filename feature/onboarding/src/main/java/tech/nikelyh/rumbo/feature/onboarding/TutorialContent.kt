package tech.nikelyh.rumbo.feature.onboarding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Timer
import tech.nikelyh.rumbo.core.designsystem.component.MascotState

/**
 * Content provider for the onboarding tutorial and interactive user guide.
 * Employs accessible, empathetic phrasing tailored for everyday users across diverse backgrounds.
 */
object TutorialContent {
    val steps: List<TutorialStep> = listOf(
        TutorialStep(
            id = "processes",
            title = "¿Qué es un Proceso?",
            subtitle = "Tu gran objetivo dividido en pasos posibles",
            description = "Un proceso es una tarea grande que para completarse necesita de varias tareas más pequeñas. Si uno de esos pasos también es grande, se convierte en otro proceso dentro de él.",
            keyPoints = listOf(
                "Divide lo que parece difícil en pasos simples y alcanzables",
                "Si una tarea crece mucho, conviértela en un nuevo proceso",
                "Cuentas con un espacio General para tus tareas del día a día",
                "Puedes pausar, culminar y reactivar tus procesos cuando quieras"
            ),
            icon = Icons.Default.AccountTree,
            mascotState = MascotState.FOCUSED
        ),
        TutorialStep(
            id = "tasks",
            title = "Tus Tareas Diarias",
            subtitle = "Acciones concretas con día y hora de cierre",
            description = "Las tareas son los pasos prácticos de tu proceso. Cada una tiene su fecha y hora límite para que siempre sepas cuál es el siguiente paso y cuándo terminarlo.",
            keyPoints = listOf(
                "Elige la fecha y la hora límite de forma rápida y sencilla",
                "Puedes editar y ajustar tus tareas en cualquier momento",
                "Asigna un costo estimado si la actividad requiere alguna compra",
                "Calculamos automáticamente el tiempo que le dedicas"
            ),
            icon = Icons.AutoMirrored.Filled.Assignment,
            mascotState = MascotState.DEFAULT
        ),
        TutorialStep(
            id = "sessions",
            title = "Sesiones de Enfoque",
            subtitle = "Dedica tiempo con calma y concentración",
            description = "Inicia el temporizador en cualquier tarea para avanzar con serenidad. La aplicación cuidará tu tiempo en segundo plano mientras trabajas a tu propio ritmo.",
            keyPoints = listOf(
                "El cronómetro sigue corriendo aunque salgas de la app",
                "Anota tus aprendizajes o ideas al terminar cada sesión",
                "Al marcar la tarea como lista, respetamos todo el tiempo acumulado",
                "Pausa o cancela tu sesión libremente cuando lo necesites"
            ),
            icon = Icons.Default.Timer,
            mascotState = MascotState.FOCUSED
        ),
        TutorialStep(
            id = "progress",
            title = "Tu Progreso Real",
            subtitle = "Descubre a dónde va tu tiempo y energía",
            description = "Visualiza de forma clara y amable cuánto tiempo y dedicación le has entregado a cada proyecto. Cada minuto invertido te acerca a lo que deseas lograr.",
            keyPoints = listOf(
                "Resumen diario y semanal fácil de comprender",
                "Conoce el total invertido en cada uno de tus proyectos",
                "Celebra tus avances y reflexiona sobre tu ritmo de vida"
            ),
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            mascotState = MascotState.SUCCESS
        )
    )
}
