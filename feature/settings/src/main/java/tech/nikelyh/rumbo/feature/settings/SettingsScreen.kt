package tech.nikelyh.rumbo.feature.settings

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tech.nikelyh.rumbo.core.common.LocaleHelper
import tech.nikelyh.rumbo.core.designsystem.component.RumboCard
import tech.nikelyh.rumbo.core.designsystem.component.RumboLoadingWheel
import tech.nikelyh.rumbo.core.designsystem.component.RumboOutlinedButton
import tech.nikelyh.rumbo.core.designsystem.theme.RumboTheme
import tech.nikelyh.rumbo.core.model.AppLanguage
import tech.nikelyh.rumbo.core.model.UserSettings

@Composable
fun SettingsRoute(
    modifier: Modifier = Modifier,
    onResetCompleted: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.resetCompleted.collect {
            onResetCompleted()
        }
    }

    SettingsScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

@Composable
internal fun SettingsScreen(
    uiState: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        when (uiState) {
            SettingsUiState.Loading -> RumboLoadingWheel()
            is SettingsUiState.Success -> {
                val settings = uiState.userSettings
                val isSystemDark = isSystemInDarkTheme()
                val isDarkActive = settings.isDarkModeEnabled ?: isSystemDark
                val themeModeSubtitle = when (settings.isDarkModeEnabled) {
                    null -> stringResource(R.string.settings_theme_system)
                    true -> stringResource(R.string.settings_theme_dark)
                    false -> stringResource(R.string.settings_theme_light)
                }

                // General Preferences: Dark Mode & Notifications
                RumboCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.settings_dark_mode),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = themeModeSubtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Switch(
                            checked = isDarkActive,
                            onCheckedChange = { onEvent(SettingsUiEvent.ToggleDarkMode(it)) }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.settings_notifications),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Switch(
                            checked = settings.isNotificationsEnabled,
                            onCheckedChange = { onEvent(SettingsUiEvent.ToggleNotifications(it)) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Language Preferences Section
                SettingsLanguageCard(
                    selectedLanguageCode = settings.languageCode,
                    onLanguageSelected = { language ->
                        onEvent(SettingsUiEvent.ChangeLanguage(language))
                        LocaleHelper.applyLanguage(context, language.code)
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Reset Application Section
                Text(
                    text = stringResource(R.string.settings_reset_zone),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                RumboCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.settings_reset_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        RumboOutlinedButton(
                            onClick = { showResetDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isResetting
                        ) {
                            if (uiState.isResetting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.height(20.dp).width(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.settings_resetting),
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.settings_reset_button),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.settings_reset_dialog_title)) },
            text = { Text(stringResource(R.string.settings_reset_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        onEvent(SettingsUiEvent.ResetApplicationData)
                    }
                ) {
                    Text(
                        text = stringResource(R.string.settings_reset_dialog_confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.settings_reset_dialog_cancel))
                }
            }
        )
    }
}

/**
 * Clean, modular card component for selecting the application's active language.
 *
 * @param selectedLanguageCode Currently saved ISO language tag, or null for system default.
 * @param onLanguageSelected Callback invoked when a user chooses an [AppLanguage].
 * @param modifier Optional [Modifier] for layout adjustments.
 */
@Composable
private fun SettingsLanguageCard(
    selectedLanguageCode: String?,
    onLanguageSelected: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentLanguage = AppLanguage.fromCode(selectedLanguageCode)

    RumboCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.settings_language),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = stringResource(R.string.settings_language_subtitle),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = currentLanguage == AppLanguage.SYSTEM,
                    onClick = { onLanguageSelected(AppLanguage.SYSTEM) },
                    label = { Text(stringResource(R.string.settings_language_system)) }
                )
                FilterChip(
                    selected = currentLanguage == AppLanguage.SPANISH,
                    onClick = { onLanguageSelected(AppLanguage.SPANISH) },
                    label = { Text(stringResource(R.string.settings_language_spanish)) }
                )
                FilterChip(
                    selected = currentLanguage == AppLanguage.ENGLISH,
                    onClick = { onLanguageSelected(AppLanguage.ENGLISH) },
                    label = { Text(stringResource(R.string.settings_language_english)) }
                )
                FilterChip(
                    selected = currentLanguage == AppLanguage.PORTUGUESE,
                    onClick = { onLanguageSelected(AppLanguage.PORTUGUESE) },
                    label = { Text(stringResource(R.string.settings_language_portuguese)) }
                )
            }
        }
    }
}

@Preview(name = "Settings Light", showBackground = true)
@Composable
private fun SettingsScreenPreviewLight() {
    RumboTheme(darkTheme = false) {
        SettingsScreen(
            uiState = SettingsUiState.Success(UserSettings()),
            onEvent = {}
        )
    }
}
