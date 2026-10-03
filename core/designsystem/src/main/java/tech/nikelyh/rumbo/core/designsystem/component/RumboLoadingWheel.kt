package tech.nikelyh.rumbo.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun RumboLoadingWheel(
    modifier: Modifier = Modifier
) {
    RumboLoadingState(modifier = modifier, isLoading = true)
}
