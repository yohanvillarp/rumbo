package tech.nikelyh.rumbo.feature.onboarding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Timer
import tech.nikelyh.rumbo.core.designsystem.component.MascotState

/**
 * Content provider for the onboarding tutorial and interactive user guide.
 * Employs standard Android string resource references for automatic locale adaptation.
 */
object TutorialContent {
    val steps: List<TutorialStep> = listOf(
        TutorialStep(
            id = "processes",
            titleRes = R.string.tutorial_process_title,
            subtitleRes = R.string.tutorial_process_subtitle,
            descriptionRes = R.string.tutorial_process_description,
            keyPointsRes = listOf(
                R.string.tutorial_process_point_1,
                R.string.tutorial_process_point_2,
                R.string.tutorial_process_point_3,
                R.string.tutorial_process_point_4
            ),
            icon = Icons.Default.AccountTree,
            mascotState = MascotState.FOCUSED,
            showsLanguageSelector = false
        ),
        TutorialStep(
            id = "tasks",
            titleRes = R.string.tutorial_tasks_title,
            subtitleRes = R.string.tutorial_tasks_subtitle,
            descriptionRes = R.string.tutorial_tasks_description,
            keyPointsRes = listOf(
                R.string.tutorial_tasks_point_1,
                R.string.tutorial_tasks_point_2,
                R.string.tutorial_tasks_point_3,
                R.string.tutorial_tasks_point_4
            ),
            icon = Icons.AutoMirrored.Filled.Assignment,
            mascotState = MascotState.DEFAULT,
            showsLanguageSelector = false
        ),
        TutorialStep(
            id = "sessions",
            titleRes = R.string.tutorial_sessions_title,
            subtitleRes = R.string.tutorial_sessions_subtitle,
            descriptionRes = R.string.tutorial_sessions_description,
            keyPointsRes = listOf(
                R.string.tutorial_sessions_point_1,
                R.string.tutorial_sessions_point_2,
                R.string.tutorial_sessions_point_3,
                R.string.tutorial_sessions_point_4
            ),
            icon = Icons.Default.Timer,
            mascotState = MascotState.FOCUSED,
            showsLanguageSelector = false
        ),
        TutorialStep(
            id = "progress",
            titleRes = R.string.tutorial_progress_title,
            subtitleRes = R.string.tutorial_progress_subtitle,
            descriptionRes = R.string.tutorial_progress_description,
            keyPointsRes = listOf(
                R.string.tutorial_progress_point_1,
                R.string.tutorial_progress_point_2,
                R.string.tutorial_progress_point_3
            ),
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            mascotState = MascotState.SUCCESS,
            showsLanguageSelector = true
        )
    )
}
