package tech.nikelyh.rumbo.feature.onboarding

import androidx.compose.ui.graphics.vector.ImageVector
import tech.nikelyh.rumbo.core.designsystem.component.MascotState

/**
 * Scalable data model representing an interactive onboarding tutorial step.
 * Designed to accommodate new educational flows and feature highlights as Rumbo evolves.
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
