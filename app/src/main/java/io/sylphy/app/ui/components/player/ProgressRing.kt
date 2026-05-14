package io.sylphy.app.ui.components.player

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.Dp
import io.sylphy.app.ui.theme.Layout
import io.sylphy.app.ui.theme.ProgressEmpty
import io.sylphy.app.ui.theme.ProgressFilled
import io.sylphy.app.ui.theme.ProgressPlayhead
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ProgressRing(
    progress: Float,
    size: Dp,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = Layout.progressRingStroke,
) {
    val strokePx = with(LocalDensity.current) { strokeWidth.toPx() }
    val dotRadius = with(LocalDensity.current) { Layout.seekDotRadius.toPx() }

    Canvas(modifier = modifier.size(size)) {
        val diameter = size.toPx() - strokePx
        val topLeft = Offset(strokePx / 2f, strokePx / 2f)
        val arcSize = Size(diameter, diameter)
        val startAngle = -90f
        val sweep = 360f

        drawArc(
            color = ProgressEmpty,
            startAngle = startAngle,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(strokePx, cap = StrokeCap.Butt),
        )

        if (progress > 0f) {
            drawArc(
                color = ProgressFilled,
                startAngle = startAngle,
                sweepAngle = sweep * progress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(strokePx, cap = StrokeCap.Butt),
            )
        }

        val angleRad = Math.toRadians((startAngle + sweep * progress).toDouble())
        val radius = diameter / 2f
        val cx = center.x + radius * cos(angleRad).toFloat()
        val cy = center.y + radius * sin(angleRad).toFloat()
        drawCircle(color = ProgressPlayhead, radius = dotRadius, center = Offset(cx, cy))
    }
}
