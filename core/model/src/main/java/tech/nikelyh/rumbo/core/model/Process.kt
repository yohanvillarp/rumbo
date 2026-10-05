package tech.nikelyh.rumbo.core.model

data class Process(
    val id: String,
    val name: String,
    val description: String? = null,
    val status: ProcessStatus = ProcessStatus.ACTIVE,
    val createdAtEpochMillis: Long,
    val finishedAtEpochMillis: Long? = null,
    val colorOrVisualId: String,
    val accumulatedDirectCost: Double = 0.0,
    val isStarred: Boolean = false,
    val parentProcessId: String? = null
) {
    val isSubProcess: Boolean
        get() = parentProcessId != null

    val isSystem: Boolean
        get() = id == GENERAL_PROCESS_ID

    val isFinished: Boolean
        get() = status == ProcessStatus.COMPLETED || status == ProcessStatus.ARCHIVED

    val isActive: Boolean
        get() = status == ProcessStatus.ACTIVE

    val isPaused: Boolean
        get() = status == ProcessStatus.PAUSED

    fun toggleStarred(): Process = copy(isStarred = !isStarred)

    fun addDirectCost(amount: Double): Process {
        require(amount >= 0) { "Direct cost amount cannot be negative" }
        return copy(accumulatedDirectCost = accumulatedDirectCost + amount)
    }

    fun pause(): Process {
        require(status == ProcessStatus.ACTIVE) { "Only ACTIVE processes can be paused" }
        return copy(status = ProcessStatus.PAUSED)
    }

    fun resume(): Process {
        require(status == ProcessStatus.PAUSED) { "Only PAUSED processes can be resumed" }
        return copy(status = ProcessStatus.ACTIVE)
    }

    fun finish(finishedAt: Long): Process {
        require(finishedAt >= createdAtEpochMillis) { "Finish timestamp cannot be before creation timestamp" }
        return copy(
            status = ProcessStatus.COMPLETED,
            finishedAtEpochMillis = finishedAt
        )
    }

    fun archive(): Process {
        return copy(status = ProcessStatus.ARCHIVED)
    }

    fun reopen(): Process {
        return copy(
            status = ProcessStatus.ACTIVE,
            finishedAtEpochMillis = null
        )
    }

    companion object {
        const val GENERAL_PROCESS_ID = "general"

        fun createGeneralProcess(createdAt: Long = 0L): Process = Process(
            id = GENERAL_PROCESS_ID,
            name = "General",
            description = "Espacio para actividades cotidianas y tareas varias",
            status = ProcessStatus.ACTIVE,
            createdAtEpochMillis = createdAt,
            colorOrVisualId = "system_default",
            accumulatedDirectCost = 0.0
        )
    }
}
