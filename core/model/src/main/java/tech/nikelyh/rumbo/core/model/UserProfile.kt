package tech.nikelyh.rumbo.core.model

data class UserProfile(
    val id: String,
    val name: String,
    val createdAtEpochMillis: Long
) {
    init {
        require(name.isNotBlank()) { "User profile name cannot be blank" }
        require(createdAtEpochMillis > 0) { "User profile creation date must be positive" }
    }
}
