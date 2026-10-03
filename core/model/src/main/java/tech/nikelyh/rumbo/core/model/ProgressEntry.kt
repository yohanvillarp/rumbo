package tech.nikelyh.rumbo.core.model

data class ProgressEntry(
    val id: String,
    val processId: String,
    val dateEpochMillis: Long,
    val progressLevel: Int,
    val note: String? = null
) {
    init {
        require(processId.isNotBlank()) { "ProgressEntry must belong to a valid processId" }
        require(progressLevel in 0..100) { "Progress level must be between 0 and 100" }
    }
}
