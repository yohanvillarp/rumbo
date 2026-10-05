package tech.nikelyh.rumbo.core.common

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import androidx.core.os.ConfigurationCompat
import java.util.Locale

import tech.nikelyh.rumbo.core.model.AppLanguage

/**
 * Utility helper providing standard Android locale resolution and per-app language management.
 */
object LocaleHelper {

    /**
     * Applies the selected language code.
     * On Android 13+ (API 33+), uses system [LocaleManager.setApplicationLocales].
     * On older Android versions, applies configuration updates to resources.
     * Pass null or empty to revert to the device's system default language.
     */
    fun applyLanguage(context: Context, languageCode: String?) {
        val currentEffective = getEffectiveLanguageCode(context).lowercase().take(2)
        val target = languageCode?.lowercase()?.take(2)

        // If target matches what is already running, do nothing to prevent activity recreation
        if (target != null && target == currentEffective) {
            return
        }
        if (target == null && getCurrentLanguageCode(context) == null) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(LocaleManager::class.java)
            if (localeManager != null) {
                val localeList = if (languageCode.isNullOrBlank()) {
                    LocaleList.getEmptyLocaleList()
                } else {
                    LocaleList.forLanguageTags(languageCode)
                }
                localeManager.applicationLocales = localeList
            }
        }

        // Compatibility fallback for pre-API 33 devices and immediate runtime display updates
        val targetLocale = if (languageCode.isNullOrBlank()) {
            val sysLocales = ConfigurationCompat.getLocales(Resources.getSystem().configuration)
            if (sysLocales.isEmpty) Locale.getDefault() else sysLocales[0] ?: Locale.getDefault()
        } else {
            Locale.forLanguageTag(languageCode)
        }
        Locale.setDefault(targetLocale)
        val resources = context.resources
        val config = Configuration(resources.configuration)
        config.setLocale(targetLocale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    /**
     * Retrieves the current per-app language code, or null if following system default.
     */
    fun getCurrentLanguageCode(context: Context): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(LocaleManager::class.java)
            val locales = localeManager?.applicationLocales
            if (locales != null && !locales.isEmpty) {
                return locales.toLanguageTags().split(",").firstOrNull()
            }
        }
        return null
    }

    /**
     * Resolves the active ISO language code, falling back to system configuration or device default.
     */
    fun getEffectiveLanguageCode(context: Context): String {
        getCurrentLanguageCode(context)?.let { return it }
        val currentLocales = ConfigurationCompat.getLocales(context.resources.configuration)
        return if (!currentLocales.isEmpty) {
            currentLocales[0]?.language ?: Locale.getDefault().language
        } else {
            Locale.getDefault().language
        }
    }

    /**
     * Resolves the effective [AppLanguage] supported by Rumbo, detecting device locale
     * if no explicit language is stored or if [AppLanguage.SYSTEM] is configured.
     */
    fun resolveEffectiveLanguage(context: Context, selectedLanguageCode: String?): AppLanguage {
        if (!selectedLanguageCode.isNullOrBlank()) {
            val resolved = AppLanguage.fromCode(selectedLanguageCode)
            if (resolved != AppLanguage.SYSTEM) return resolved
        }
        val code = getEffectiveLanguageCode(context).lowercase()
        return when {
            code.startsWith("pt") -> AppLanguage.PORTUGUESE
            code.startsWith("en") -> AppLanguage.ENGLISH
            else -> AppLanguage.SPANISH
        }
    }

    /**
     * Finds the host [Activity] from a given [Context].
     */
    fun findActivity(context: Context): Activity? {
        var current = context
        while (current is ContextWrapper) {
            if (current is Activity) return current
            current = current.baseContext
        }
        return null
    }
}
