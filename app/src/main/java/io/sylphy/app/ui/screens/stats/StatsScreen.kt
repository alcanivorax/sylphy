package io.sylphy.app.ui.screens.stats

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.sylphy.app.core.util.toMmSs
import io.sylphy.app.ui.components.shared.SylphyDivider
import io.sylphy.app.ui.theme.BgBase
import io.sylphy.app.ui.theme.BgElevated
import io.sylphy.app.ui.theme.Duration
import io.sylphy.app.ui.theme.FgMuted
import io.sylphy.app.ui.theme.FgPrimary
import io.sylphy.app.ui.theme.FgSubtle
import io.sylphy.app.ui.theme.Spacing
import io.sylphy.app.ui.theme.SylphyType

@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(BgBase),
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.xl),
    ) {
        item {
            Text("Listening stats", style = SylphyType.Display, color = FgPrimary)
            Text("Your last 12 weeks", style = SylphyType.Caption, color = FgMuted)
        }
        if (uiState.heatmapData.isEmpty() && uiState.topTracks.isEmpty()) {
            item { Text("No listening history yet", style = SylphyType.Body, color = FgMuted) }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md), modifier = Modifier.fillMaxWidth()) {
                StatCard(uiState.weeklyMinutes.toInt(), "min", "This week", Modifier.weight(1f))
                StatCard(uiState.monthlyTracks, "tracks", "This month", Modifier.weight(1f))
            }
        }
        item {
            Text("Daily listening", style = SylphyType.Heading, color = FgPrimary)
            Spacer(Modifier.height(Spacing.md))
            StatsHeatmap(uiState.heatmapData.associate { it.dayStartMs to it.durationMs })
        }
        item { Text("Top tracks (30 days)", style = SylphyType.Heading, color = FgPrimary) }
        itemsIndexed(uiState.topTracks, key = { _, item -> item.track.id }) { index, item ->
            TopTrackRow(index + 1, item, uiState.topTracks.firstOrNull()?.sessionCount ?: 1)
        }
    }
}

@Composable
private fun StatCard(value: Int, unit: String, label: String, modifier: Modifier) {
    val animated by animateIntAsState(value, tween(Duration.Deliberate), label = "stat_$label")
    Column(modifier.background(BgElevated).padding(Spacing.md)) {
        Text("$animated", style = SylphyType.Stat, color = FgPrimary)
        Text(unit, style = SylphyType.CodeSmall, color = FgMuted)
        Spacer(Modifier.height(Spacing.sm))
        Text(label, style = SylphyType.BodySmall, color = FgMuted)
    }
}

@Composable
private fun StatsHeatmap(data: Map<Long, Long>) {
    val nowDay = (System.currentTimeMillis() / 86400000) * 86400000
    val max = data.values.maxOrNull()?.coerceAtLeast(1L) ?: 1L
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        repeat(7) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                repeat(12) { col ->
                    val day = nowDay - ((11 - col) * 7L + (6 - row)) * 86400000L
                    val alpha = (data[day]?.toFloat()?.div(max) ?: 0f).coerceIn(0f, 1f)
                    Box(
                        Modifier
                            .size(18.dp)
                            .background(if (alpha == 0f) FgSubtle else FgPrimary.copy(alpha = 0.2f + alpha * 0.8f)),
                    )
                }
            }
        }
    }
}

@Composable
private fun TopTrackRow(rank: Int, item: TopTrackItem, maxCount: Int) {
    val weight = item.sessionCount / maxCount.toFloat().coerceAtLeast(1f)
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(rank.toString().padStart(2, '0'), style = SylphyType.CodeSmall.copy(fontWeight = if (rank == 1) FontWeight.Medium else FontWeight.Normal), color = if (rank == 1) FgPrimary else FgMuted)
        Spacer(Modifier.size(Spacing.md))
        Column(Modifier.weight(1f)) {
            Text(item.track.title, style = SylphyType.Code, color = FgPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${item.sessionCount} plays · ${item.listenedMs.toMmSs()}", style = SylphyType.BodySmall, color = FgMuted)
            Box(Modifier.fillMaxWidth(weight).height(Spacing.px1).background(FgPrimary))
        }
    }
    SylphyDivider()
}
