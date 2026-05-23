package io.sylphy.app.ui.components.player

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.sylphy.app.ui.theme.BgElevated
import io.sylphy.app.ui.theme.Duration
import io.sylphy.app.ui.theme.FgSubtle
import io.sylphy.app.ui.theme.Layout
import io.sylphy.app.ui.theme.SylphyType

@Composable
fun CDDisc(
    artworkPath: String?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    discSize: Dp = Layout.albumArtSize,
) {
    val spinState = rememberPremiumDiscSpinState(isPlaying = isPlaying)

    Box(
        modifier = modifier
            .size(discSize)
            .drawBehind {
                drawCircle(
                    color = Color.Black.copy(alpha = 0.22f),
                    radius = size.minDimension * 0.49f,
                    center = Offset(size.width / 2f, size.height / 2f + 10.dp.toPx()),
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = spinState.rotationDegrees
                },
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width / 2f
                val centerY = size.height / 2f
                val discRadius = size.width / 2f

                // Layer 1: Outer disc base (matte black/deep gray)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1A1A1A),
                            Color(0xFF0F0F0F),
                            Color(0xFF080808),
                        ),
                        center = Offset(centerX, centerY),
                        radius = discRadius,
                    ),
                    radius = discRadius,
                    center = Offset(centerX, centerY),
                )

                // Layer 2: Subtle ring textures (concentric grooves)
                val grooveCount = 40
                val grooveSpacing = discRadius / grooveCount
                for (i in 0 until grooveCount) {
                    val radius = discRadius - (i * grooveSpacing)
                    if (radius > discRadius * 0.35f) { // Only draw grooves outside artwork area
                        val alpha = 0.03f + (0.02f * (i % 3))
                        drawCircle(
                            color = Color(0xFFFFFFFF).copy(alpha = alpha),
                            radius = radius,
                            center = Offset(centerX, centerY),
                            style = Stroke(width = 0.5f),
                        )
                    }
                }

                // Layer 3: subtle disc edge integration
                drawCircle(
                    color = Color(0xFFFFFFFF).copy(alpha = 0.05f),
                    radius = discRadius,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 2f),
                )
            }

            // Album art embedded in disc center
            CDArtwork(artworkPath, size = discSize * 0.65f)

            // Center hub
            CDHub(hubSize = discSize * 0.08f)
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val discRadius = size.width / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // Fixed lighting reads as a physical disc passing under a stable light source.
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF).copy(alpha = 0.08f),
                        Color(0xFFFFFFFF).copy(alpha = 0.02f),
                        Color.Transparent,
                    ),
                    center = Offset(center.x - discRadius * 0.3f, center.y - discRadius * 0.3f),
                    radius = discRadius * 0.8f,
                ),
                radius = discRadius,
                center = center,
            )

            drawArc(
                color = Color(0xFFFFFFFF).copy(alpha = 0.1f),
                startAngle = -45f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(0f, 0f),
                size = Size(size.width, size.height),
                style = Stroke(width = 1.5f),
            )
        }
    }
}

@Composable
private fun CDArtwork(
    artworkPath: String?,
    size: Dp,
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (!artworkPath.isNullOrBlank()) {
            val imageData = when {
                artworkPath.startsWith("content://") -> artworkPath
                artworkPath.startsWith("file://") -> artworkPath
                else -> "file://$artworkPath"
            }
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageData)
                    .crossfade(Duration.Slow)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BgElevated),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "SYLPHY",
                    style = SylphyType.CodeSmall,
                    color = FgSubtle.copy(alpha = 0.5f),
                )
            }
        }

        // Subtle overlay for depth integration
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF000000).copy(alpha = 0.15f),
                        ),
                    ),
                    shape = CircleShape,
                ),
        )
    }
}

@Composable
private fun CDHub(hubSize: Dp) {
    Canvas(modifier = Modifier.size(hubSize)) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val hubRadius = size.width / 2f

        // Hub base
        drawCircle(
            color = Color(0xFF2A2A2A),
            radius = hubRadius,
            center = Offset(centerX, centerY),
        )

        // Center hole
        drawCircle(
            color = Color(0xFF0A0A0A),
            radius = hubRadius * 0.4f,
            center = Offset(centerX, centerY),
        )

        // Subtle ring around hole
        drawCircle(
            color = Color(0xFFFFFFFF).copy(alpha = 0.1f),
            radius = hubRadius * 0.6f,
            center = Offset(centerX, centerY),
            style = Stroke(width = 0.5f),
        )
    }
}
