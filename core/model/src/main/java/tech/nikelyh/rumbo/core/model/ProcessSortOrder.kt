package tech.nikelyh.rumbo.core.model

/**
 * Defines the sorting criteria for processes.
 */
enum class ProcessSortOrder {
    /**
     * Orders processes by their creation timestamp descending (newest first),
     * preserving system processes at the top.
     */
    RECENT,

    /**
     * Orders processes alphabetically by name (A to Z),
     * preserving system processes at the top.
     */
    NAME,

    /**
     * Orders processes by accumulated direct cost descending (highest investment first),
     * preserving system processes at the top.
     */
    ACCUMULATED_COST
}

/**
 * Defines hierarchy filtering criteria for processes.
 */
enum class ProcessTypeFilter {
    /**
     * Shows all processes regardless of hierarchy.
     */
    ALL,

    /**
     * Shows only top-level (root) processes (no parent process assigned).
     */
    MAIN,

    /**
     * Shows only nested subprocesses (processes that belong to a parent process).
     */
    SUBPROCESS
}

