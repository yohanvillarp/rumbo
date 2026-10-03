package tech.nikelyh.rumbo.core.designsystem.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import tech.nikelyh.rumbo.core.designsystem.theme.RumboTheme

@Composable
fun RumboButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.animateContentSize(),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        content = content
    )
}

@Composable
fun RumboOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.animateContentSize(),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        ),
        content = content
    )
}

@Preview(name = "Buttons Light", showBackground = true)
@Composable
private fun RumboButtonPreviewLight() {
    RumboTheme(darkTheme = false) {
        RumboButton(onClick = {}) {
            Text("Aceptar")
        }
    }
}

@Preview(name = "Buttons Dark", showBackground = true)
@Composable
private fun RumboButtonPreviewDark() {
    RumboTheme(darkTheme = true) {
        RumboOutlinedButton(onClick = {}) {
            Text("Cancelar")
        }
    }
}
