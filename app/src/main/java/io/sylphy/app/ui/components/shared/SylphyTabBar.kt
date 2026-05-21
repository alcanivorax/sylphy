package io.sylphy.app.ui.components.shared

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import io.sylphy.app.ui.theme.ActiveBackground
import io.sylphy.app.ui.theme.ActiveForeground
import io.sylphy.app.ui.theme.BgBase
import io.sylphy.app.ui.theme.BorderDefault
import io.sylphy.app.ui.theme.ChipCorner
import io.sylphy.app.ui.theme.Duration
import io.sylphy.app.ui.theme.FgMuted
import io.sylphy.app.ui.theme.Layout
import io.sylphy.app.ui.theme.NothingRed
import io.sylphy.app.ui.theme.OLEDBlack
import io.sylphy.app.ui.theme.Spacing
import io.sylphy.app.ui.theme.SylphyEasing
import io.sylphy.app.ui.theme.SylphyType
import kotlin.math.roundToInt

@Composable
fun SylphyTabBar(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isNothingOS: Boolean = false,
) {
    val haptic = LocalHapticFeedback.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(Layout.topBarHeight)
            .background(if (isNothingOS) OLEDBlack else BgBase),
    ) {
        val tabWidth = maxWidth / tabs.size
        val tabWidthPx = with(LocalDensity.current) { tabWidth.toPx() }
        val indicatorOffset = remember { Animatable(0f) }

        LaunchedEffect(selectedIndex, tabWidthPx) {
            indicatorOffset.animateTo(
                targetValue = selectedIndex * tabWidthPx,
                animationSpec = spring(dampingRatio = 0.85f, stiffness = 400f),
            )
        }

        val indicatorColor = if (isNothingOS) NothingRed else ActiveBackground

        Box(
            modifier = Modifier
                .width(tabWidth * 0.6f)
                .height(Spacing.px1)
                .offset {
                    IntOffset(
                        x = indicatorOffset.value.roundToInt(),
                        y = (Layout.topBarHeight - Spacing.px1).roundToPx(),
                    )
                }
                .background(indicatorColor, RectangleShape),
        )

        Row(modifier = Modifier.fillMaxSize()) {
            tabs.forEachIndexed { i, label ->
                val active = i == selectedIndex
                val interactionSource = remember { MutableInteractionSource() }
                val pressed by interactionSource.collectIsPressedAsState()
                val scale by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = if (pressed) 0.96f else 1f,
                    animationSpec = spring(dampingRatio = 1f, stiffness = 800f),
                    label = "tab_scale",
                )
                val textColor by animateColorAsState(
                    targetValue = when {
                        active && isNothingOS -> NothingRed
                        active -> ActiveForeground
                        else -> FgMuted
                    },
                    animationSpec = tween(Duration.Fast),
                    label = "tab_text_color",
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .scale(scale)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onTabSelected(i)
                        },
                ) {
                    Text(
                        text = label.uppercase(),
                        style = if (isNothingOS) SylphyType.CodeSmall else SylphyType.Heading,
                        color = textColor,
                    )
                }
            }
        }
    }
}
