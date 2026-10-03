package tech.nikelyh.rumbo.core.designsystem.component

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import tech.nikelyh.rumbo.core.designsystem.theme.RumboTheme

@Composable
fun RumboLoadingState(
    modifier: Modifier = Modifier,
    isLoading: Boolean = true,
    content: @Composable () -> Unit = {}
) {
    Crossfade(
        targetState = isLoading,
        label = "loadingCrossfade"
    ) { loading ->
        if (loading) {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        } else {
            content()
        }
    }
}

@Preview(name = "LoadingState Light", showBackground = true)
@Composable
private fun RumboLoadingStatePreviewLight() {
    RumboTheme(darkTheme = false) {
        RumboLoadingState(isLoading = true)
    }
}
