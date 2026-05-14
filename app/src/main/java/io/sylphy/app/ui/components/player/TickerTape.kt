package io.sylphy.app.ui.components.player

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.sylphy.app.data.model.Track
import io.sylphy.app.ui.theme.FgMuted
import io.sylphy.app.ui.theme.SylphyType
import kotlin.math.roundToInt

@Composable
fun TickerTape(
    track: Track?,
    modifier: Modifier = Modifier,
) {
    val content = remember(track) {
        if (track == null) {
            "Sylphy  -  No track playing  -  "
        } else {
            buildList {
                add(track.title)
                add(track.artist)
                if (track.album.isNotBlank()) add(track.album)
                track.year?.let { add(it.toString()) }
                track.mimeType?.substringAfterLast('/')?.uppercase()?.let { add(it) }
                track.bitRate?.let { add("${it / 1000} kbps") }
            }.joinToString("  -  ") + "  -  "
        }
    }

    val textMeasurer = rememberTextMeasurer()
    val textStyle = SylphyType.CodeSmall.copy(color = FgMuted)
    val singleWidth = remember(content) {
        textMeasurer.measure(content, textStyle).size.width.toFloat().coerceAtLeast(1f)
    }
    val offsetX = remember { Animatable(0f) }

    LaunchedEffect(content, singleWidth) {
        offsetX.snapTo(0f)
        offsetX.animateTo(
            targetValue = -singleWidth,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = (singleWidth / 32f * 1000f).roundToInt().coerceAtLeast(1000),
                    easing = LinearEasing,
                ),
                repeatMode = RepeatMode.Restart,
            ),
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp)
            .clipToBounds(),
    ) {
        Text(
            text = content.repeat(3),
            style = textStyle,
            maxLines = 1,
            modifier = Modifier
                .wrapContentWidth(Alignment.Start, unbounded = true)
                .offset { IntOffset(offsetX.value.roundToInt(), 0) },
        )
    }
}
