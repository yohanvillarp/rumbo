package tech.nikelyh.rumbo.core.model

data class WorkSession(
    val id: String,
    val processId: String,
    val taskId: String? = null,
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long? = null,
    val durationMillis: Long = 0L,
    val note: String? = null
) {
    init {
        require(processId.isNotBlank()) { "WorkSession must belong to a valid processId" }
        require(startTimeEpochMillis > 0) { "Start time must be greater than zero" }
        if (endTimeEpochMillis != null) {
            require(endTimeEpochMillis >= startTimeEpochMillis) { "End time cannot be before start time" }
        }
    }

    val isActive: Boolean
        get() = endTimeEpochMillis == null

    fun finishSession(endTime: Long, note: String? = null): WorkSession {
        require(endTime >= startTimeEpochMillis) { "End time cannot be before start time" }
        val duration = endTime - startTimeEpochMillis
        return copy(
            endTimeEpochMillis = endTime,
            durationMillis = duration,
            note = note ?: this.note
        )
    }
}
