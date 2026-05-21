package io.sylphy.app.ui.components.player

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.sylphy.app.R
import io.sylphy.app.data.model.RepeatMode
import io.sylphy.app.ui.theme.ActiveBackground
import io.sylphy.app.ui.theme.ActiveForeground
import io.sylphy.app.ui.theme.BgBase
import io.sylphy.app.ui.theme.BorderStrong
import io.sylphy.app.ui.theme.ContainerCorner
import io.sylphy.app.ui.theme.FgGhost
import io.sylphy.app.ui.theme.FgMuted
import io.sylphy.app.ui.theme.FgPrimary
import io.sylphy.app.ui.theme.Layout

@Composable
fun TransportControls(
    isPlaying: Boolean,
    shuffleEnabled: Boolean,
    repeatMode: RepeatMode,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TransportIconButton(
            icon = R.drawable.ic_shuffle,
            active = shuffleEnabled,
            onClick = onToggleShuffle,
            contentDescription = "Shuffle",
        )
        TransportIconButton(
            icon = R.drawable.ic_skip_prev,
            onClick = onPrevious,
            contentDescription = "Previous",
        )
        PlayButton(isPlaying = isPlaying, onPress = onPlayPause)
        TransportIconButton(
            icon = R.drawable.ic_skip_next,
            onClick = onNext,
            contentDescription = "Next",
        )
        TransportIconButton(
            icon = if (repeatMode == RepeatMode.ONE) R.drawable.ic_repeat_one else R.drawable.ic_repeat,
            active = repeatMode != RepeatMode.OFF,
            onClick = onCycleRepeat,
            contentDescription = "Repeat",
        )
    }
}

@Composable
private fun PlayButton(
    isPlaying: Boolean,
    onPress: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.90f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 600f),
        label = "play_button_scale",
    )

    Box(
        modifier = Modifier
            .size(Layout.playButtonSize)
            .scale(scale)
            .background(if (isPlaying) BgBase else ActiveBackground, ContainerCorner)
            .then(
                if (isPlaying) {
                    Modifier.border(Layout.borderThick, BorderStrong, ContainerCorner)
                } else {
                    Modifier
                },
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                onPress()
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
            contentDescription = if (isPlaying) "Pause" else "Play",
            tint = if (isPlaying) FgPrimary else ActiveForeground,
            modifier = Modifier.size(32.dp),
        )
    }
}

@Composable
private fun TransportIconButton(
    @DrawableRes icon: Int,
    active: Boolean = false,
    onClick: () -> Unit,
    contentDescription: String,
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.85f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 600f),
        label = "transport_icon_scale",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .size(Layout.transportTapTarget)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                onClick()
            },
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            tint = if (active) FgPrimary else FgMuted,
            modifier = Modifier.size(Layout.transportIconSize),
        )
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier
                .size(width = 12.dp, height = 2.dp)
                .background(if (active) FgPrimary else Color.Transparent, CircleShape),
        )
    }
}
