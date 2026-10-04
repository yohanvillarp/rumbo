package tech.nikelyh.rumbo.core.designsystem.theme

import androidx.compose.ui.graphics.Color

object ProcessColors {
    val Teal = Color(0xFF0F5257)
    val Blue = Color(0xFF1976D2)
    val Purple = Color(0xFF7B1FA2)
    val Amber = Color(0xFFF57C00)
    val Emerald = Color(0xFF2E7D32)

    val options = listOf(
        "teal" to Teal,
        "blue" to Blue,
        "purple" to Purple,
        "amber" to Amber,
        "emerald" to Emerald
    )

    fun getColor(colorOrVisualId: String?): Color {
        return when (colorOrVisualId?.lowercase()) {
            "teal" -> Teal
            "blue" -> Blue
            "purple" -> Purple
            "amber" -> Amber
            "emerald" -> Emerald
            else -> Teal
        }
    }
}
