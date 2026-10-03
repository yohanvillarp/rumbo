package tech.nikelyh.rumbo.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class RumboElevation(
    val flat: Dp = 0.dp,
    val subtle: Dp = 1.dp,
    val medium: Dp = 2.dp
)

val LocalRumboElevation = staticCompositionLocalOf { RumboElevation() }
