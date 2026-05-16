package io.sylphy.app.ui.screens.ambient

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.sylphy.app.ui.theme.Duration
import io.sylphy.app.ui.theme.FgMuted
import io.sylphy.app.ui.theme.FgPrimary
import io.sylphy.app.ui.theme.FgSecondary
import io.sylphy.app.ui.theme.FgSubtle
import io.sylphy.app.ui.theme.Spacing
import io.sylphy.app.ui.theme.SylphyType

@Composable
fun AmbientScreen(
    navController: NavController,
    viewModel: AmbientViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val view = LocalView.current

    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { navController.popBackStack() },
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center).padding(Spacing.lg),
            horizontalAlignment = Alignment.Start,
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(uiState.time, style = SylphyType.Clock, color = FgPrimary)
                AnimatedVisibility(
                    visible = uiState.cursorVisible,
                    enter = fadeIn(tween(0)),
                    exit = fadeOut(tween(0)),
                ) {
                    Text("_", style = SylphyType.Clock, color = FgSecondary, modifier = Modifier.padding(start = 2.dp))
                }
            }
            Spacer(Modifier.height(Spacing.lg))
            uiState.activeTrack?.let { track ->
                Text(track.title, style = SylphyType.Heading, color = FgSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(Spacing.xs))
                Text(track.artist, style = SylphyType.Body, color = FgMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        uiState.progress?.let { progress ->
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(progress)
                    .height(1.dp)
                    .background(FgSubtle),
            )
        }
    }
}
