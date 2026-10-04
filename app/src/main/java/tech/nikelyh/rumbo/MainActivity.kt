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

            RumboApp(
                hasCompletedOnboarding = hasCompletedOnboarding,
                isDarkMode = isDarkMode,
                activeSession = activeSession
            )
        }
    }
}
