package io.sylphy.app.ui.components.shared

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import io.sylphy.app.ui.theme.ActiveBackground
import io.sylphy.app.ui.theme.ActiveForeground
import io.sylphy.app.ui.theme.Layout
import io.sylphy.app.ui.theme.BorderDefault
import io.sylphy.app.ui.theme.ContainerCorner
import io.sylphy.app.ui.theme.Duration
import io.sylphy.app.ui.theme.FgPrimary
import io.sylphy.app.ui.theme.Spacing
import io.sylphy.app.ui.theme.SylphyEasing
import io.sylphy.app.ui.theme.SylphyType
import kotlinx.coroutines.launch

enum class ButtonVariant { Solid, Outline }

@Composable
fun SylphyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Outline,
    enabled: Boolean = true,
) {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    val bgColor = when (variant) {
        ButtonVariant.Solid -> ActiveBackground
        ButtonVariant.Outline -> androidx.compose.ui.graphics.Color.Transparent
    }
    val textColor = when (variant) {
        ButtonVariant.Solid -> ActiveForeground
        ButtonVariant.Outline -> FgPrimary
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(40.dp)
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .background(bgColor, ContainerCorner)
            .then(
                if (variant == ButtonVariant.Outline)
                    Modifier.border(Layout.borderThin, BorderDefault, ContainerCorner)
                else Modifier
            )
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                scope.launch {
                    scale.animateTo(0.97f, tween(Duration.Instant, easing = SylphyEasing.Exit))
                    scale.animateTo(1f, spring())
                }
                onClick()
            }
            .padding(horizontal = Spacing.lg)
            .graphicsLayer { alpha = if (enabled) 1f else 0.4f },
    ) {
        Text(text = text, style = SylphyType.Heading, color = textColor)
    }
}
