package tech.nikelyh.rumbo.feature.onboarding

import androidx.compose.ui.graphics.vector.ImageVector
import tech.nikelyh.rumbo.core.designsystem.component.MascotState

/**
 * Modelo de datos escalable para pasos de tutorial y guía de uso.
 * Permite incorporar nuevas funcionalidades y explicaciones a medida que el sistema evoluciona.
 */
data class TutorialStep(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val keyPoints: List<String>,
    val icon: ImageVector,
    val mascotState: MascotState = MascotState.DEFAULT
)
