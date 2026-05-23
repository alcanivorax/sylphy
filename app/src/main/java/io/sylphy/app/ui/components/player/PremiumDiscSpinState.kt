package io.sylphy.app.ui.components.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import kotlin.math.exp

private const val TargetDegreesPerSecond = 360f / 32f
private const val AccelerationTauSeconds = 2.4f
private const val DecelerationTauSeconds = 4.2f
private const val MinVisibleVelocity = 0.015f
private const val MaxFrameDeltaSeconds = 1f / 30f

class PremiumDiscSpinState(initialRotation: Float = 0f) {
    var rotationDegrees by mutableFloatStateOf(initialRotation.wrapDegrees())
        private set

    private var velocityDegreesPerSecond = 0f
    private var lastFrameNanos = 0L

    fun resetFrameClock() {
        lastFrameNanos = 0L
    }

    fun step(frameNanos: Long, isPlaying: Boolean) {
        val previousFrame = lastFrameNanos
        lastFrameNanos = frameNanos
        if (previousFrame == 0L) return

        val deltaSeconds = ((frameNanos - previousFrame) / 1_000_000_000f)
            .coerceIn(0f, MaxFrameDeltaSeconds)
        if (deltaSeconds <= 0f) return

        val targetVelocity = if (isPlaying) TargetDegreesPerSecond else 0f
        val tau = if (isPlaying) AccelerationTauSeconds else DecelerationTauSeconds
        val blend = 1f - exp(-deltaSeconds / tau)

        velocityDegreesPerSecond += (targetVelocity - velocityDegreesPerSecond) * blend
        if (!isPlaying && velocityDegreesPerSecond < MinVisibleVelocity) {
            velocityDegreesPerSecond = 0f
        }

        rotationDegrees = (rotationDegrees + velocityDegreesPerSecond * deltaSeconds).wrapDegrees()
    }
}

@Composable
fun rememberPremiumDiscSpinState(isPlaying: Boolean): PremiumDiscSpinState {
    var savedRotation by rememberSaveable { mutableFloatStateOf(0f) }
    val state = remember { PremiumDiscSpinState(savedRotation) }
    val playingState by rememberUpdatedState(isPlaying)

    LaunchedEffect(state) {
        state.resetFrameClock()
        while (true) {
            withFrameNanos { frameNanos ->
                state.step(frameNanos = frameNanos, isPlaying = playingState)
            }
        }
    }

    DisposableEffect(state) {
        onDispose {
            savedRotation = state.rotationDegrees
        }
    }

    return state
}

private fun Float.wrapDegrees(): Float {
    val wrapped = this % 360f
    return if (wrapped < 0f) wrapped + 360f else wrapped
}
