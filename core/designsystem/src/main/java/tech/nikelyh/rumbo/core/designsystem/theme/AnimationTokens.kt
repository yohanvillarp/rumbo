package tech.nikelyh.rumbo.core.designsystem.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween

object RumboAnimationTokens {
    const val DurationFast = 150
    const val DurationMedium = 300
    const val DurationSlow = 450

    val DefaultEasing = FastOutSlowInEasing
    val DecelerateEasing = LinearOutSlowInEasing

    fun <T> fastTween() = tween<T>(durationMillis = DurationFast, easing = DefaultEasing)
    fun <T> mediumTween() = tween<T>(durationMillis = DurationMedium, easing = DefaultEasing)
    fun <T> slowTween() = tween<T>(durationMillis = DurationSlow, easing = DefaultEasing)
}
