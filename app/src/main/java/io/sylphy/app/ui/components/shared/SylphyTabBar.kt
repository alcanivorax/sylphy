package io.sylphy.app.ui.components.shared

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import io.sylphy.app.ui.theme.ActiveBackground
import io.sylphy.app.ui.theme.FgMuted
import io.sylphy.app.ui.theme.Layout
import io.sylphy.app.ui.theme.Spacing
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
            .height(Layout.topBarHeight),
    ) {
        val tabWidth = maxWidth / tabs.size
        val tabWidthPx = with(LocalDensity.current) { tabWidth.toPx() }

        Row(modifier = Modifier.fillMaxSize()) {
            tabs.forEachIndexed { index, label ->
                val active = index == selectedIndex

                val textColor by animateColorAsState(
                    targetValue = if (active) ActiveBackground else FgMuted,
                    animationSpec = spring(),
                    label = "tab_color",
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            onTabSelected(index)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label.uppercase(),
                        style = SylphyType.CodeSmall.copy(
                            letterSpacing = 3.sp,
                        ),
                        color = textColor,
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .width(tabWidth * 0.32f)
                .height(Spacing.px1)
                .offset {
                    IntOffset(
                        x = (
                            selectedIndex * tabWidthPx +
                                (tabWidthPx * 0.34f)
                            ).roundToInt(),
                        y = (Layout.topBarHeight - Spacing.px1).roundToPx(),
                    )
                }
                .background(ActiveBackground, RectangleShape),
        )
    }
}
