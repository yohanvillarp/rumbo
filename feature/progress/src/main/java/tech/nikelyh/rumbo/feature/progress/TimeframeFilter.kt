package tech.nikelyh.rumbo.feature.progress

enum class TimeframeFilter(val days: Int, val label: String) {
    DAYS_7(7, "7 días"),
    DAYS_30(30, "30 días"),
    DAYS_90(90, "90 días")
}
