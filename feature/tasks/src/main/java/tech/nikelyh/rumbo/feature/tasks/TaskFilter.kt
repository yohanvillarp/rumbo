package tech.nikelyh.rumbo.feature.tasks

/**
 * Filter categories for task list views based on status and deadline.
 */
enum class TaskFilter {
    /** Shows all tasks regardless of status. */
    ALL,

    /** Shows only active, incomplete tasks. */
    PENDING,

    /** Shows incomplete tasks scheduled for the current calendar day. */
    TODAY,

    /** Shows incomplete tasks whose deadline has already elapsed. */
    OVERDUE,

    /** Shows tasks that have been marked as completed. */
    COMPLETED
}

