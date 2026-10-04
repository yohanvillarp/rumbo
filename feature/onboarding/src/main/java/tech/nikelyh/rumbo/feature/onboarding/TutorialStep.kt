package tech.nikelyh.rumbo.feature.onboarding

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import tech.nikelyh.rumbo.core.designsystem.component.MascotState

/**
 * Scalable data model representing an interactive onboarding tutorial step.
 * Uses Android string resource IDs adhering to standard platform localization.
 *
 * @property id Unique identifier for the step.
 * @property titleRes String resource ID for the step title.
 * @property subtitleRes String resource ID for the step subtitle.
 * @property descriptionRes String resource ID for the detailed explanation.
 * @property keyPointsRes List of string resource IDs for key takeaway points.
 * @property icon Vector icon associated with the step.
 * @property mascotState Mascot facial expression and mood.
 * @property showsLanguageSelector Whether this step displays the language switch option.
 */
data class TutorialStep(
    val id: String,
    @get:StringRes val titleRes: Int,
    @get:StringRes val subtitleRes: Int,
    @get:StringRes val descriptionRes: Int,
    val keyPointsRes: List<Int>,
    val icon: ImageVector,
    val mascotState: MascotState = MascotState.DEFAULT,
    val showsLanguageSelector: Boolean = false
)
