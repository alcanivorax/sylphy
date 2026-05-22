package io.sylphy.app.ui.components.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
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
import io.sylphy.app.ui.theme.Duration
import io.sylphy.app.ui.theme.FgGhost
import io.sylphy.app.ui.theme.FgMuted
import io.sylphy.app.ui.theme.FgPrimary
import io.sylphy.app.ui.theme.Layout
import io.sylphy.app.ui.theme.NothingRed
import io.sylphy.app.ui.theme.SylphyEasing
import kotlinx.coroutines.launch

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
    isNothingOS: Boolean = false,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TransportIconButton(
            icon = R.drawable.ic_shuffle,
            active = shuffleEnabled,
            onClick = onToggleShuffle,
            contentDescription = "Shuffle",
            isNothingOS = isNothingOS,
        )
        TransportIconButton(
            icon = R.drawable.ic_skip_prev,
            onClick = onPrevious,
            contentDescription = "Previous",
            isNothingOS = isNothingOS,
        )
        MorphingPlayButton(isPlaying = isPlaying, onPress = onPlayPause, isNothingOS = isNothingOS)
        TransportIconButton(
            icon = R.drawable.ic_skip_next,
            onClick = onNext,
            contentDescription = "Next",
            isNothingOS = isNothingOS,
        )
        TransportIconButton(
            icon = if (repeatMode == RepeatMode.ONE) R.drawable.ic_repeat_one else R.drawable.ic_repeat,
            active = repeatMode != RepeatMode.OFF,
            onClick = onCycleRepeat,
            contentDescription = "Repeat",
            isNothingOS = isNothingOS,
        )
    }
}

@Composable
private fun MorphingPlayButton(
    isPlaying: Boolean,
    onPress: () -> Unit,
    isNothingOS: Boolean = false,
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scope = rememberCoroutineScope()

    val morphProgress = remember { Animatable(if (isPlaying) 1f else 0f) }
    LaunchedEffect(isPlaying) {
        morphProgress.animateTo(
            targetValue = if (isPlaying) 1f else 0f,
            animationSpec = tween(Duration.Normal, easing = SylphyEasing.Standard),
        )
    }

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "play_button_scale",
    )

    val bgColor by animateColorAsState(
        targetValue = when {
            isNothingOS && isPlaying -> NothingRed
            isNothingOS -> Color(0xFF1A1A1A)
            isPlaying -> BgBase
            else -> ActiveBackground
        },
        animationSpec = tween(Duration.Fast),
        label = "play_bg",
    )

    val fgColor by animateColorAsState(
        targetValue = when {
            isNothingOS && isPlaying -> Color.White
            isNothingOS -> NothingRed
            isPlaying -> FgPrimary
            else -> ActiveForeground
        },
        animationSpec = tween(Duration.Fast),
        label = "play_fg",
    )

    val borderColor by animateColorAsState(
        targetValue = if (isNothingOS && isPlaying) NothingRed else BorderStrong,
        animationSpec = tween(Duration.Fast),
        label = "play_border",
    )

    Box(
        modifier = Modifier
            .size(Layout.playButtonSize)
            .scale(scale)
            .background(bgColor, ContainerCorner)
            .then(
                if (isPlaying) {
                    Modifier.border(Layout.borderThick, borderColor, ContainerCorner)
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
        Canvas(modifier = Modifier.size(32.dp)) {
            val w = size.width
            val h = size.height
            val corner = w * 0.15f

            val playPath = Path().apply {
                val left = w * 0.25f
                val right = w * 0.8f
                val top = h * 0.15f
                val bottom = h * 0.85f
                val midY = (top + bottom) / 2f

                moveTo(left + corner, top)
                lineTo(right - corner * 0.5f, midY)
                lineTo(left + corner, bottom)
                quadraticTo(left, bottom, left, bottom - corner)
                lineTo(left, top + corner)
                quadraticTo(left, top, left + corner, top)
                close()
            }

            val pausePath = Path().apply {
                val barWidth = w * 0.18f
                val gap = w * 0.15f
                val left1 = w * 0.2f
                val left2 = left1 + barWidth + gap
                val top = h * 0.15f
                val bottom = h * 0.85f
                val r = barWidth * 0.3f

                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        left = left1,
                        top = top,
                        right = left1 + barWidth,
                        bottom = bottom,
                        topLeftCornerRadius = CornerRadius(r, r),
                        topRightCornerRadius = CornerRadius(r, r),
                        bottomLeftCornerRadius = CornerRadius(r, r),
                        bottomRightCornerRadius = CornerRadius(r, r),
                    ),
                )
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        left = left2,
                        top = top,
                        right = left2 + barWidth,
                        bottom = bottom,
                        topLeftCornerRadius = CornerRadius(r, r),
                        topRightCornerRadius = CornerRadius(r, r),
                        bottomLeftCornerRadius = CornerRadius(r, r),
                        bottomRightCornerRadius = CornerRadius(r, r),
                    ),
                )
            }

            val morphedPath = Path()
            val t = morphProgress.value

            if (t < 0.5f) {
                morphedPath.addPath(playPath)
            } else {
                morphedPath.addPath(pausePath)
            }

            drawPath(morphedPath, fgColor, style = Fill)
        }
    }
}

@Composable
private fun TransportIconButton(
    icon: Int,
    active: Boolean = false,
    onClick: () -> Unit,
    contentDescription: String,
    isNothingOS: Boolean = false,
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "transport_icon_scale",
    )

    val tintColor by animateColorAsState(
        targetValue = when {
            active && isNothingOS -> NothingRed
            active -> FgPrimary
            else -> FgMuted
        },
        animationSpec = tween(Duration.Fast),
        label = "transport_tint",
    )

    val indicatorColor by animateColorAsState(
        targetValue = if (active) {
            if (isNothingOS) NothingRed else FgPrimary
        } else {
            Color.Transparent
        },
        animationSpec = tween(Duration.Fast),
        label = "indicator_color",
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
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            tint = tintColor,
            modifier = Modifier.size(Layout.transportIconSize),
        )
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier
                .size(width = 16.dp, height = 2.dp)
                .background(indicatorColor, ContainerCorner),
        )
    }
}
