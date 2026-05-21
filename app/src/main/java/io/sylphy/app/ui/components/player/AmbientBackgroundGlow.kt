package io.sylphy.app.ui.components.player

import android.graphics.Bitmap
import androidx.core.graphics.scale
import androidx.core.graphics.get
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import io.sylphy.app.ui.theme.Duration
import io.sylphy.app.ui.theme.SylphyEasing
import kotlin.math.pow

@Composable
fun AmbientBackgroundGlow(
    artworkPath: String?,
    modifier: Modifier = Modifier,
    intensity: Float = 0.15f,
) {
    val context = LocalContext.current
    var dominantColor by remember { mutableStateOf<Color?>(null) }
    var secondaryColor by remember { mutableStateOf<Color?>(null) }
    
    val animatedDominant = remember { Animatable(0f) }
    val animatedSecondary = remember { Animatable(0f) }
    
    LaunchedEffect(artworkPath) {
        if (!artworkPath.isNullOrBlank()) {
            val imageData = when {
                artworkPath.startsWith("content://") -> artworkPath
                artworkPath.startsWith("file://") -> artworkPath
                else -> "file://$artworkPath"
            }
            
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageData)
                .allowHardware(false)
                .build()
            
            val result = (loader.execute(request) as? SuccessResult)?.drawable
            result?.let { drawable ->
                val bitmap = drawable.toBitmap()
                val colors = extractDominantColors(bitmap)
                if (colors.isNotEmpty()) {
                    dominantColor = colors[0]
                    secondaryColor = colors.getOrNull(1)
                }
            }
        } else {
            dominantColor = null
            secondaryColor = null
        }
    }
    
    LaunchedEffect(dominantColor, secondaryColor) {
        animatedDominant.animateTo(
            targetValue = if (dominantColor != null) 1f else 0f,
            animationSpec = tween(Duration.Slow, easing = SylphyEasing.Standard),
        )
        animatedSecondary.animateTo(
            targetValue = if (secondaryColor != null) 1f else 0f,
            animationSpec = tween(Duration.Slow, easing = SylphyEasing.Standard),
        )
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (dominantColor != null) {
                drawAmbientGlow(
                    dominantColor = dominantColor!!,
                    secondaryColor = secondaryColor,
                    intensity = intensity,
                    dominantAlpha = animatedDominant.value,
                    secondaryAlpha = animatedSecondary.value,
                )
            }
        }
    }
}

private fun DrawScope.drawAmbientGlow(
    dominantColor: Color,
    secondaryColor: Color?,
    intensity: Float,
    dominantAlpha: Float,
    secondaryAlpha: Float,
) {
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val maxRadius = size.width.coerceAtLeast(size.height) * 0.8f
    
    // Primary glow from dominant color
    val dominantGlowColor = dominantColor.copy(alpha = intensity * dominantAlpha)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                dominantGlowColor,
                dominantGlowColor.copy(alpha = dominantGlowColor.alpha * 0.5f),
                Color.Transparent,
            ),
            center = Offset(centerX, centerY * 0.8f),
            radius = maxRadius,
        ),
        radius = maxRadius,
        center = Offset(centerX, centerY),
    )
    
    // Secondary accent glow
    if (secondaryColor != null) {
        val secondaryGlowColor = secondaryColor.copy(alpha = intensity * 0.6f * secondaryAlpha)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    secondaryGlowColor,
                    secondaryGlowColor.copy(alpha = secondaryGlowColor.alpha * 0.3f),
                    Color.Transparent,
                ),
                center = Offset(centerX * 1.2f, centerY * 1.1f),
                radius = maxRadius * 0.7f,
            ),
            radius = maxRadius * 0.7f,
            center = Offset(centerX, centerY),
        )
    }
}

private fun extractDominantColors(bitmap: Bitmap, colorCount: Int = 2): List<Color> {
    val width = bitmap.width.coerceAtMost(64)
    val height = bitmap.height.coerceAtMost(64)
    val scaledBitmap = bitmap.scale(width, height, false)
    
    val colorMap = mutableMapOf<Color, Int>()
    
    for (x in 0 until width step 2) {
        for (y in 0 until height step 2) {
            val pixel = scaledBitmap[x, y]
            val androidColor = android.graphics.Color.red(pixel)
            val green = android.graphics.Color.green(pixel)
            val blue = android.graphics.Color.blue(pixel)
            val alpha = android.graphics.Color.alpha(pixel)
            
            if (alpha < 128) continue
            
            val color = Color(
                red = androidColor / 255f,
                green = green / 255f,
                blue = blue / 255f,
                alpha = 1f,
            )
            
            colorMap[color] = (colorMap[color] ?: 0) + 1
        }
    }
    
    scaledBitmap.recycle()
    
    return colorMap.entries
        .sortedByDescending { it.value }
        .take(colorCount)
        .map { it.key }
}
