package io.sylphy.app.ui.components.shared

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import io.sylphy.app.ui.theme.ActiveBackground
import io.sylphy.app.ui.theme.ActiveForeground
import io.sylphy.app.ui.theme.BgBase
import io.sylphy.app.ui.theme.BorderDefault
import io.sylphy.app.ui.theme.ChipCorner
import io.sylphy.app.ui.theme.Duration
import io.sylphy.app.ui.theme.FgMuted
import io.sylphy.app.ui.theme.Layout
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
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(Layout.topBarHeight)
            .background(BgBase)
            .border(width = Layout.borderThin, color = BorderDefault, shape = RectangleShape),
    ) {
        val tabWidth = maxWidth / tabs.size
        val tabWidthPx = with(LocalDensity.current) { tabWidth.toPx() }
        val pillOffset = remember { Animatable(selectedIndex * tabWidthPx) }

        LaunchedEffect(selectedIndex) {
            pillOffset.animateTo(
                targetValue = selectedIndex * tabWidthPx,
                animationSpec = tween(Duration.Normal, easing = SylphyEasing.Standard),
            )
        }

        // Sliding inverted pill
        Box(
            modifier = Modifier
                .width(tabWidth - Spacing.sm)
                .fillMaxHeight()
                .padding(vertical = Spacing.sm, horizontal = Spacing.xs)
                .offset {
                    IntOffset(
                        x = pillOffset.value.roundToInt() + Spacing.xs.roundToPx(),
                        y = 0,
                    )
                }
                .background(ActiveBackground, ChipCorner),
        )

        // Tab labels layered on top of the pill
        Row(modifier = Modifier.fillMaxSize()) {
            tabs.forEachIndexed { i, label ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { onTabSelected(i) },
                ) {
                    Text(
                        text = label,
                        style = SylphyType.Heading,
                        color = if (i == selectedIndex) ActiveForeground else FgMuted,
                    )
                }
            }
        }
    }
}
