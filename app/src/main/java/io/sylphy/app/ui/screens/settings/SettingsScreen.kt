package io.sylphy.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.sylphy.app.data.model.ThemeMode
import io.sylphy.app.ui.components.shared.SylphyDivider
import io.sylphy.app.ui.theme.ContainerCorner
import io.sylphy.app.ui.theme.Layout
import io.sylphy.app.ui.theme.Spacing
import io.sylphy.app.ui.theme.SylphyType

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = Spacing.md, vertical = Spacing.lg),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                "SETTINGS",
                style = SylphyType.Display,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = Spacing.sm)
            )
        }

        Spacer(Modifier.height(Spacing.xl))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            SettingsSection(title = "Appearance") {
                ThemeOption(
                    title = "Monochrome Dark",
                    description = "Deep charcoal and white",
                    selected = settings?.themeMode == ThemeMode.MONOCHROME_DARK,
                    onClick = { viewModel.setThemeMode(ThemeMode.MONOCHROME_DARK) }
                )
                ThemeOption(
                    title = "Monochrome Light",
                    description = "Paper white and charcoal",
                    selected = settings?.themeMode == ThemeMode.MONOCHROME_LIGHT,
                    onClick = { viewModel.setThemeMode(ThemeMode.MONOCHROME_LIGHT) }
                )
                ThemeOption(
                    title = "Nothing OS",
                    description = "Pure black with red accents",
                    selected = settings?.themeMode == ThemeMode.NOTHING_OS,
                    onClick = { viewModel.setThemeMode(ThemeMode.NOTHING_OS) }
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            title.uppercase(),
            style = SylphyType.CodeSmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = Spacing.md)
        )
        content()
        Spacer(Modifier.height(Spacing.md))
        SylphyDivider()
    }
}

@Composable
private fun ThemeOption(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = SylphyType.Code,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
            )
            Text(
                description,
                style = SylphyType.BodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        if (selected) {
            Text(
                "ACTIVE",
                style = SylphyType.CodeSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .border(1.dp, MaterialTheme.colorScheme.primary, ContainerCorner)
                    .padding(horizontal = Spacing.sm, vertical = 2.dp)
            )
        }
    }
}
