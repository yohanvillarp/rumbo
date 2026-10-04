package tech.nikelyh.rumbo.core.model

enum class ProcessSortOrder {
    RECENT,        // Más recientes
    NAME,          // Alfabético (A-Z)
    ACCUMULATED_COST // Mayor presupuesto o inversión
}

enum class ProcessTypeFilter {
    ALL,           // Todos
    MAIN,          // Procesos principales
    SUBPROCESS     // Subprocesos
}
