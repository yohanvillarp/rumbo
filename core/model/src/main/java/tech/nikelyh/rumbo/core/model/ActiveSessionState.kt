package tech.nikelyh.rumbo.core.model

data class ActiveSessionState(
    val processId: String? = null,
    val taskId: String? = null,
    val startTimeEpochMillis: Long = 0L,
    val lastResumeEpochMillis: Long = 0L,
    val accumulatedTimeMillis: Long = 0L,
    val isRunning: Boolean = false
) {
    val hasActiveSession: Boolean
        get() = startTimeEpochMillis > 0L
}
