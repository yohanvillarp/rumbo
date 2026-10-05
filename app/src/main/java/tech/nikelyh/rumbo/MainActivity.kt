package tech.nikelyh.rumbo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import tech.nikelyh.rumbo.ui.RumboApp

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Rumbo)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainUiState by viewModel.uiState.collectAsStateWithLifecycle()
            val successState = mainUiState as? MainUiState.Success
            val hasCompletedOnboarding = successState?.hasCompletedOnboarding
            val isDarkMode = successState?.isDarkMode
            val activeSession = successState?.activeSession
            val languageCode = successState?.languageCode

            val context = androidx.compose.ui.platform.LocalContext.current
            val effectiveLanguage = androidx.compose.runtime.remember(languageCode) {
                tech.nikelyh.rumbo.core.common.LocaleHelper.resolveEffectiveLanguage(context, languageCode)
            }
            val targetLocale = androidx.compose.runtime.remember(effectiveLanguage) {
                when (effectiveLanguage) {
                    tech.nikelyh.rumbo.core.model.AppLanguage.SPANISH -> java.util.Locale("es")
                    tech.nikelyh.rumbo.core.model.AppLanguage.ENGLISH -> java.util.Locale("en")
                    tech.nikelyh.rumbo.core.model.AppLanguage.PORTUGUESE -> java.util.Locale("pt")
                    tech.nikelyh.rumbo.core.model.AppLanguage.SYSTEM -> java.util.Locale.getDefault()
                }
            }

            androidx.compose.runtime.LaunchedEffect(languageCode) {
                tech.nikelyh.rumbo.core.common.LocaleHelper.applyLanguage(this@MainActivity, languageCode)
            }

            val configuration = androidx.compose.ui.platform.LocalConfiguration.current
            val localizedConfiguration = androidx.compose.runtime.remember(configuration, targetLocale) {
                android.content.res.Configuration(configuration).apply {
                    setLocale(targetLocale)
                }
            }

            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalConfiguration provides localizedConfiguration
            ) {
                RumboApp(
                    hasCompletedOnboarding = hasCompletedOnboarding,
                    isDarkMode = isDarkMode,
                    activeSession = activeSession
                )
            }
        }
    }
}
