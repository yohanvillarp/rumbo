package tech.nikelyh.rumbo.core.model

enum class ProgressLevel(val numericValue: Int, val label: String) {
    LOW(33, "Bajo"),
    MEDIUM(66, "Medio"),
    HIGH(100, "Alto");

    companion object {
        fun fromValue(value: Int): ProgressLevel {
            return when {
                value <= 40 -> LOW
                value <= 75 -> MEDIUM
                else -> HIGH
            }
        }
    }
}
