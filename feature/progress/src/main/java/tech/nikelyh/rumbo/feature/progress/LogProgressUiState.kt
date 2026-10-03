package tech.nikelyh.rumbo.feature.progress

import tech.nikelyh.rumbo.core.model.Process
import tech.nikelyh.rumbo.core.model.ProgressLevel

data class LogProgressUiState(
    val selectedProcessId: String = "general",
    val availableProcesses: List<Process> = emptyList(),
    val progressLevel: ProgressLevel = ProgressLevel.MEDIUM,
    val note: String = "",
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false
)
