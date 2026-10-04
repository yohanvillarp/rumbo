package tech.nikelyh.rumbo.core.model

/**
 * Defines the sorting criteria for tasks across lists and process details.
 */
enum class TaskSortOrder {
    /**
     * Orders tasks primarily by upcoming due date (nearest deadline first),
     * placing tasks without a due date at the end, then breaking ties by priority.
     */
    DUE_DATE,

    /**
     * Orders tasks by their creation timestamp in descending order (most recently created first).
     */
    RECENT,

    /**
     * Orders tasks by priority level in descending order (HIGH -> MEDIUM -> LOW),
     * then breaking ties by upcoming due date.
     */
    PRIORITY
}

