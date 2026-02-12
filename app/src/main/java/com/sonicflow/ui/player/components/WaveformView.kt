package com.sonicflow.ui.player.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.sonicflow.ui.theme.VioletPrimary
import com.sonicflow.ui.theme.VioletSecondary
import kotlin.math.max
import kotlin.math.sin

@Composable
fun AnimatedWaveformView(
    amplitudes: List<Int>,
    progress: Float,
    isPlaying: Boolean,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    if (amplitudes.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(Color.Gray.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = VioletPrimary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(48.dp)
            )
        }
        return
    }

    // Animations
    val infiniteTransition = rememberInfiniteTransition()

    // Animation de pulsation pour les barres jouées
    val pulseAnimation by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Animation de flow pour les barres non jouées
    val flowAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Animation de glow pour la barre courante
    val glowAnimation by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val seekProgress = (offset.x / size.width).coerceIn(0f, 1f)
                    onSeek(seekProgress)
                }
            }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Optimisation du nombre de barres
        val maxBars = 80
        val processedAmplitudes = if (amplitudes.size > maxBars) {
            val step = amplitudes.size / maxBars.toFloat()
            List(maxBars) { index ->
                val startIdx = (index * step).toInt()
                val endIdx = ((index + 1) * step).toInt().coerceAtMost(amplitudes.size)
                val slice = amplitudes.subList(startIdx, endIdx)
                slice.maxOrNull() ?: 0
            }
        } else {
            amplitudes
        }

        val maxAmplitude = max(processedAmplitudes.maxOrNull() ?: 1, 1)
        val barSpacing = canvasWidth / processedAmplitudes.size
        val barWidth = (barSpacing * 0.55f)

        val progressIndex = (progress * processedAmplitudes.size).coerceIn(0f, processedAmplitudes.size - 1f)

        processedAmplitudes.forEachIndexed { index, amplitude ->
            // Hauteur avec effet de vague
            val waveEffect = if (isPlaying) {
                (sin(index * 0.3 + flowAnimation * 2 * Math.PI) * 0.1 + 1).toFloat()
            } else {
                1f
            }

            val normalizedAmplitude = amplitude.toFloat() / maxAmplitude
            val barHeight = max(3f, normalizedAmplitude * canvasHeight * 0.7f * waveEffect)
            val x = index * barSpacing + (barSpacing - barWidth) / 2
            val y = (canvasHeight - barHeight) / 2

            val isPlayed = index < progressIndex
            val isCurrentBar = index == progressIndex.toInt()

            when {
                isCurrentBar && isPlaying -> {
                    // Barre courante avec effet de glow animé
                    val scale = 1.2f + (glowAnimation * 0.1f)
                    val scaledHeight = barHeight * scale
                    val scaledY = (canvasHeight - scaledHeight) / 2

                    // Dégradé animé
                    val gradientBrush = Brush.verticalGradient(
                        colors = listOf(
                            VioletPrimary.copy(alpha = glowAnimation),
                            VioletSecondary.copy(alpha = glowAnimation * 0.8f),
                            VioletPrimary.copy(alpha = glowAnimation)
                        )
                    )

                    // Effet de glow
                    drawRoundRect(
                        color = VioletPrimary.copy(alpha = 0.3f * glowAnimation),
                        topLeft = Offset(x - 3.dp.toPx(), scaledY - 3.dp.toPx()),
                        size = Size(barWidth + 6.dp.toPx(), scaledHeight + 6.dp.toPx()),
                        cornerRadius = CornerRadius(barWidth, barWidth)
                    )

                    // Barre principale
                    drawRoundRect(
                        brush = gradientBrush,
                        topLeft = Offset(x, scaledY),
                        size = Size(barWidth, scaledHeight),
                        cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                    )
                }
                isPlayed -> {
                    // Barres jouées avec effet de pulsation
                    val pulseFactor = if (isPlaying) {
                        1f + (pulseAnimation - 1f) * (1f - (progressIndex - index) / progressIndex)
                    } else {
                        1f
                    }

                    val pulsedHeight = barHeight * pulseFactor
                    val pulsedY = (canvasHeight - pulsedHeight) / 2

                    // Dégradé pour barres jouées
                    val playedGradient = Brush.verticalGradient(
                        colors = listOf(
                            VioletPrimary.copy(alpha = 0.9f),
                            VioletSecondary.copy(alpha = 0.7f)
                        )
                    )

                    drawRoundRect(
                        brush = playedGradient,
                        topLeft = Offset(x, pulsedY),
                        size = Size(barWidth, pulsedHeight),
                        cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                    )
                }
                else -> {
                    // Barres non jouées avec effet de flow
                    val alpha = if (isPlaying) {
                        0.2f + (sin(index * 0.2 + flowAnimation * 2 * Math.PI) * 0.1).toFloat()
                    } else {
                        0.2f
                    }

                    drawRoundRect(
                        color = Color.Gray.copy(alpha = alpha),
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                    )
                }
            }
        }
    }
}