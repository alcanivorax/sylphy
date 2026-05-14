package io.sylphy.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.sylphy.app.ui.theme.BgBase
import io.sylphy.app.ui.theme.FgMuted
import io.sylphy.app.ui.theme.SylphyType

@Composable
fun EqScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(BgBase),
        contentAlignment = Alignment.Center,
    ) {
        Text("Equalizer", style = SylphyType.Heading, color = FgMuted)
    }
}

@Composable
fun SleepTimerScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(BgBase),
        contentAlignment = Alignment.Center,
    ) {
        Text("Sleep Timer", style = SylphyType.Heading, color = FgMuted)
    }
}
