package io.sylphy.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.sylphy.app.core.util.toMmSs
import io.sylphy.app.ui.components.shared.ButtonVariant
import io.sylphy.app.ui.components.shared.SylphyButton
import io.sylphy.app.ui.theme.ActiveBackground
import io.sylphy.app.ui.theme.ActiveForeground
import io.sylphy.app.ui.theme.BgBase
import io.sylphy.app.ui.theme.BgElevated
import io.sylphy.app.ui.theme.Black
import io.sylphy.app.ui.theme.BorderDefault
import io.sylphy.app.ui.theme.ChipCorner
import io.sylphy.app.ui.theme.FgMuted
import io.sylphy.app.ui.theme.FgPrimary
import io.sylphy.app.ui.theme.FgSecondary
import io.sylphy.app.ui.theme.FgSubtle
import io.sylphy.app.ui.theme.Layout
import io.sylphy.app.ui.theme.Spacing
import io.sylphy.app.ui.theme.SylphyType

@Composable
fun EqScreen(viewModel: AudioSettingsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings = uiState.settings
    val freqs = listOf("32", "64", "125", "250", "500", "1k", "2k", "4k", "8k", "16k")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
            .padding(Spacing.lg),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("EQ", style = SylphyType.Heading, color = FgPrimary, modifier = Modifier.weight(1f))
            ToggleChip(if (settings.eqEnabled) "ON" else "OFF", settings.eqEnabled) {
                viewModel.setEqEnabled(!settings.eqEnabled)
            }
        }
        Spacer(Modifier.height(Spacing.lg))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            items(EqPresets.keys.toList()) { preset ->
                ToggleChip(preset.uppercase(), settings.eqPreset == preset) { viewModel.setPreset(preset) }
            }
        }
        Spacer(Modifier.height(Spacing.xl))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("+12", style = SylphyType.CodeSmall, color = FgMuted)
            Text("0 dB", style = SylphyType.CodeSmall, color = FgMuted)
            Text("-12", style = SylphyType.CodeSmall, color = FgMuted)
        }
        Box(Modifier.fillMaxWidth().height(280.dp)) {
            Box(Modifier.align(Alignment.Center).fillMaxWidth().height(Spacing.px1).background(FgSubtle))
            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                settings.eqBands.take(10).forEachIndexed { index, value ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Slider(
                            value = value,
                            onValueChange = { viewModel.setEqBand(index, it) },
                            valueRange = -12f..12f,
                            modifier = Modifier
                                .height(160.dp)
                                .width(36.dp)
                                .rotate(-90f),
                            colors = SliderDefaults.colors(
                                thumbColor = FgPrimary,
                                activeTrackColor = if (value >= 0f) FgPrimary else FgSecondary,
                                inactiveTrackColor = BgElevated,
                            ),
                        )
                        Text(freqs[index], style = SylphyType.CodeSmall, color = FgMuted, textAlign = TextAlign.Center)
                    }
                }
            }
        }
        Spacer(Modifier.height(Spacing.md))
        SylphyButton(text = "Reset to flat", onClick = { viewModel.setPreset("flat") }, variant = ButtonVariant.Outline)
    }
}

@Composable
fun SleepTimerScreen(viewModel: AudioSettingsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var customMinutes by remember { mutableLongStateOf(30L) }
    val options = listOf(15L, 30L, 45L, 60L, 90L)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
            .padding(Spacing.lg),
    ) {
        Text("SLEEP TIMER", style = SylphyType.Heading, color = FgPrimary)
        Spacer(Modifier.height(Spacing.lg))
        options.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md), modifier = Modifier.fillMaxWidth()) {
                row.forEach { minutes ->
                    ToggleChip("${minutes} min", false, Modifier.weight(1f)) { viewModel.setTimer(minutes * 60_000L) }
                }
            }
            Spacer(Modifier.height(Spacing.md))
        }
        ToggleChip("End of track", false, Modifier.fillMaxWidth()) { viewModel.setEndOfTrackTimer() }
        Spacer(Modifier.height(Spacing.xl))
        Text("Custom duration", style = SylphyType.Body, color = FgPrimary)
        Spacer(Modifier.height(Spacing.sm))
        TextField(
            value = customMinutes.toString(),
            onValueChange = { customMinutes = it.toLongOrNull()?.coerceIn(1L, 999L) ?: customMinutes },
            singleLine = true,
        )
        Spacer(Modifier.height(Spacing.md))
        SylphyButton(text = "Set timer", variant = ButtonVariant.Solid, onClick = { viewModel.setTimer(customMinutes * 60_000L) })
        Spacer(Modifier.weight(1f))
        if (uiState.timer.active) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Stops in  ${uiState.timer.remainingMs.toMmSs()}", style = SylphyType.Code, color = FgPrimary, modifier = Modifier.weight(1f))
                Text("x", style = SylphyType.Display, color = FgMuted, modifier = Modifier.clickable { viewModel.cancelTimer() }.padding(Spacing.sm))
            }
        }
    }
}

@Composable
private fun ToggleChip(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        style = SylphyType.CodeSmall,
        color = if (selected) ActiveForeground else FgMuted,
        modifier = modifier
            .background(if (selected) ActiveBackground else Color.Transparent, ChipCorner)
            .border(Layout.borderThin, if (selected) ActiveBackground else BorderDefault, ChipCorner)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
        textAlign = TextAlign.Center,
    )
}
