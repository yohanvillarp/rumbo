package tech.nikelyh.rumbo.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import tech.nikelyh.rumbo.core.common.LocaleHelper
import tech.nikelyh.rumbo.core.designsystem.component.RumboButton
import tech.nikelyh.rumbo.core.designsystem.component.RumboLogo
import tech.nikelyh.rumbo.core.designsystem.component.RumboOutlinedButton
import tech.nikelyh.rumbo.core.designsystem.theme.RumboTheme
import tech.nikelyh.rumbo.core.model.AppLanguage

@Composable
fun TutorialRoute(
    onFinishTutorial: () -> Unit,
    modifier: Modifier = Modifier,
    showLanguageSelector: Boolean = false,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    TutorialScreen(
        onFinishTutorial = onFinishTutorial,
        showLanguageSelector = showLanguageSelector,
        selectedLanguageCode = uiState.selectedLanguageCode,
        onLanguageSelected = { lang ->
            val currentEffective = LocaleHelper.resolveEffectiveLanguage(context, uiState.selectedLanguageCode)
            if (lang != currentEffective) {
                viewModel.onEvent(OnboardingUiEvent.ChangeLanguage(lang))
                LocaleHelper.applyLanguage(context, lang.code)
            }
        },
        modifier = modifier
    )
}

/**
 * Multi-step interactive onboarding tutorial screen highlighting core workflow paradigms in Rumbo.
 *
 * @param onFinishTutorial Callback invoked when the user reaches the end and confirms completion.
 * @param modifier Optional [Modifier] for layout adjustments.
 * @param showLanguageSelector Whether to display the language selection card on applicable steps.
 * @param selectedLanguageCode Currently selected language code, or null for system default.
 * @param onLanguageSelected Callback invoked when the user selects an [AppLanguage] on the final step.
 * @param steps Sequence of [TutorialStep] pages to display in the horizontal carousel.
 */
@Composable
fun TutorialScreen(
    onFinishTutorial: () -> Unit,
    modifier: Modifier = Modifier,
    showLanguageSelector: Boolean = false,
    selectedLanguageCode: String? = null,
    onLanguageSelected: (AppLanguage) -> Unit = {},
    steps: List<TutorialStep> = TutorialContent.steps
) {
    val pagerState = rememberPagerState(pageCount = { steps.size })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Carousel Header: Indicator dots
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(steps.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(8.dp)
                        .width(if (isSelected) 24.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }

        // Pager Content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { pageIndex ->
            val step = steps[pageIndex]
            TutorialStepPage(
                step = step,
                pageIndex = pageIndex,
                totalPages = steps.size,
                showLanguageSelector = showLanguageSelector,
                selectedLanguageCode = selectedLanguageCode,
                onLanguageSelected = onLanguageSelected
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation Footer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isFirstPage = pagerState.currentPage == 0
            val isLastPage = pagerState.currentPage == steps.size - 1

            if (!isFirstPage) {
                RumboOutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.tutorial_back))
                }
            }

            RumboButton(
                onClick = {
                    if (isLastPage) {
                        onFinishTutorial()
                    } else {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier.weight(if (isFirstPage) 2f else 1f)
            ) {
                if (isLastPage) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.tutorial_finish))
                } else {
                    Text(stringResource(R.string.tutorial_next))
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun TutorialStepPage(
    step: TutorialStep,
    pageIndex: Int,
    totalPages: Int,
    showLanguageSelector: Boolean,
    selectedLanguageCode: String?,
    onLanguageSelected: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Mascot & Category Icon
        Box(
            modifier = Modifier.size(100.dp),
            contentAlignment = Alignment.Center
        ) {
            RumboLogo(
                size = 96.dp,
                state = step.mascotState,
                showSubtitle = false
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = step.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.tutorial_step_indicator, pageIndex + 1, totalPages),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(step.titleRes),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(step.subtitleRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(step.descriptionRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Key Points Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                step.keyPointsRes.forEach { pointRes ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(pointRes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Language Selector Card (Displayed only if allowed and on the designated step)
        if (showLanguageSelector && step.showsLanguageSelector) {
            Spacer(modifier = Modifier.height(16.dp))
            TutorialLanguageSelector(
                selectedLanguageCode = selectedLanguageCode,
                onLanguageSelected = onLanguageSelected
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Clean, interactive language selector card shown on the final tutorial step.
 */
@Composable
fun TutorialLanguageSelector(
    selectedLanguageCode: String?,
    onLanguageSelected: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLanguage = remember(selectedLanguageCode) {
        LocaleHelper.resolveEffectiveLanguage(context, selectedLanguageCode)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.tutorial_language_label),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = currentLanguage == AppLanguage.SPANISH,
                    onClick = {
                        if (currentLanguage != AppLanguage.SPANISH) {
                            onLanguageSelected(AppLanguage.SPANISH)
                        }
                    },
                    label = { Text(stringResource(R.string.tutorial_language_spanish)) }
                )
                FilterChip(
                    selected = currentLanguage == AppLanguage.ENGLISH,
                    onClick = {
                        if (currentLanguage != AppLanguage.ENGLISH) {
                            onLanguageSelected(AppLanguage.ENGLISH)
                        }
                    },
                    label = { Text(stringResource(R.string.tutorial_language_english)) }
                )
                FilterChip(
                    selected = currentLanguage == AppLanguage.PORTUGUESE,
                    onClick = {
                        if (currentLanguage != AppLanguage.PORTUGUESE) {
                            onLanguageSelected(AppLanguage.PORTUGUESE)
                        }
                    },
                    label = { Text(stringResource(R.string.tutorial_language_portuguese)) }
                )
            }
        }
    }
}

@Preview(name = "Tutorial Light", showBackground = true)
@Composable
private fun TutorialScreenPreview() {
    RumboTheme(darkTheme = false) {
        TutorialScreen(onFinishTutorial = {})
    }
}
