package io.sylphy.app.ui.components.shared

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.sylphy.app.ui.theme.BgBase
import io.sylphy.app.ui.theme.FgGhost
import io.sylphy.app.ui.theme.FgMuted
import io.sylphy.app.ui.theme.FgPrimary
import io.sylphy.app.ui.theme.Layout
import io.sylphy.app.ui.theme.SharpCorner
import io.sylphy.app.ui.theme.Spacing
import io.sylphy.app.ui.theme.SylphyType

@Composable
fun SylphyDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        color = FgGhost,
        thickness = Layout.borderThin,
    )
}

@Composable
fun SylphyText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = SylphyType.Body,
    color: Color = FgPrimary,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Ellipsis,
) {
    Text(
        text = text,
        modifier = modifier,
        style = style,
        color = color,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Composable
fun SylphyScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        containerColor = BgBase,
        topBar = topBar,
        bottomBar = bottomBar,
        content = content,
    )
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(Layout.sectionHeaderHeight)
            .background(BgBase),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            style = SylphyType.CodeSmall,
            color = FgMuted,
            modifier = Modifier.padding(horizontal = Spacing.md),
        )
        SylphyDivider()
    }
}

@Composable
fun EmptyState(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    action: Pair<String, () -> Unit>? = null,
) {
    Column(
        modifier = modifier.padding(Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = title, style = SylphyType.Heading, color = FgPrimary)

        if (description != null) {
            Spacer(Modifier.height(Spacing.sm))
            Text(
                text = description,
                style = SylphyType.Caption,
                color = FgMuted,
            )
        }

        if (action != null) {
            Spacer(Modifier.height(Spacing.lg))
            SylphyButton(text = action.first, onClick = action.second)
        }
    }
}

@Composable
fun LoadingDots(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "loading_dots")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) { i ->
            val alpha by transition.animateFloat(
                initialValue = 0.1f,
                targetValue = 0.9f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 600, delayMillis = i * 200),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "dot_$i",
            )
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .background(FgMuted.copy(alpha = alpha), SharpCorner),
            )
        }
    }
}
