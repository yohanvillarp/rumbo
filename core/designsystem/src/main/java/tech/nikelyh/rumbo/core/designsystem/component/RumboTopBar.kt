package tech.nikelyh.rumbo.core.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import tech.nikelyh.rumbo.core.designsystem.theme.RumboTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RumboTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector? = null,
    navigationIconContentDescription: String? = null,
    onNavigationClick: () -> Unit = {},
    actionIcon: ImageVector? = null,
    actionIconContentDescription: String? = null,
    onActionClick: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        modifier = modifier,
        navigationIcon = {
            if (navigationIcon != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = navigationIcon,
                        contentDescription = navigationIconContentDescription
                    )
                }
            }
        },
        actions = {
            if (actionIcon != null) {
                IconButton(onClick = onActionClick) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = actionIconContentDescription
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Preview(name = "TopBar Light", showBackground = true)
@Composable
private fun RumboTopBarPreviewLight() {
    RumboTheme(darkTheme = false) {
        RumboTopBar(
            title = "Procesos",
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            actionIcon = Icons.Default.Settings
        )
    }
}

@Preview(name = "TopBar Dark", showBackground = true)
@Composable
private fun RumboTopBarPreviewDark() {
    RumboTheme(darkTheme = true) {
        RumboTopBar(
            title = "Procesos",
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            actionIcon = Icons.Default.Settings
        )
    }
}
