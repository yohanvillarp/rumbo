package tech.nikelyh.rumbo.feature.settings

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
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
import tech.nikelyh.rumbo.core.designsystem.component.RumboButton
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
 * Dynamic, modular card component for selecting the application's active language
 * with left/right navigation controls and instant reactive switching.
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
    val languages = remember {
        listOf(
            AppLanguage.SPANISH,
            AppLanguage.ENGLISH,
            AppLanguage.PORTUGUESE
        )
    }

    val context = LocalContext.current
    val currentSavedLanguage = remember(selectedLanguageCode) {
        LocaleHelper.resolveEffectiveLanguage(context, selectedLanguageCode)
    }

    var pendingLanguage by remember(currentSavedLanguage) {
        mutableStateOf(currentSavedLanguage)
    }

    val currentIndex = languages.indexOf(pendingLanguage).coerceAtLeast(0)

    val currentLabel = when (pendingLanguage) {
        AppLanguage.SPANISH -> stringResource(R.string.settings_language_spanish)
        AppLanguage.ENGLISH -> stringResource(R.string.settings_language_english)
        AppLanguage.PORTUGUESE -> stringResource(R.string.settings_language_portuguese)
        AppLanguage.SYSTEM -> stringResource(R.string.settings_language_spanish)
    }

    val currentSubtitle = when (pendingLanguage) {
        AppLanguage.SPANISH -> "Español (es)"
        AppLanguage.ENGLISH -> "English (en)"
        AppLanguage.PORTUGUESE -> "Português (pt)"
        AppLanguage.SYSTEM -> "Español (es)"
    }

    fun selectPrevious() {
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else languages.size - 1
        pendingLanguage = languages[prevIndex]
    }

    fun selectNext() {
        val nextIndex = if (currentIndex < languages.size - 1) currentIndex + 1 else 0
        pendingLanguage = languages[nextIndex]
    }

    val isModified = pendingLanguage != currentSavedLanguage

    RumboCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.settings_language),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.settings_language_subtitle),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Dynamic Stepper Selector with Left & Right Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { selectPrevious() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Idioma anterior",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = currentLabel,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = currentSubtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { selectNext() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Siguiente idioma",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Indicator Dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                languages.forEachIndexed { index, _ ->
                    val isSelected = index == currentIndex
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(6.dp)
                            .width(if (isSelected) 18.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            RumboButton(
                onClick = { onLanguageSelected(pendingLanguage) },
                enabled = isModified,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.settings_apply_language))
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
