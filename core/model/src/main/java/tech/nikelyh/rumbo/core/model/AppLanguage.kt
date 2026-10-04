package tech.nikelyh.rumbo.core.model

/**
 * Supported application languages.
 *
 * @property code ISO 639-1 language tag, or null for system default.
 */
enum class AppLanguage(val code: String?) {
    SYSTEM(null),
    SPANISH("es"),
    ENGLISH("en"),
    PORTUGUESE("pt");

    companion object {
        /**
         * Resolves an [AppLanguage] from its ISO language code, defaulting to [SYSTEM].
         */
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: SYSTEM
    }
}
