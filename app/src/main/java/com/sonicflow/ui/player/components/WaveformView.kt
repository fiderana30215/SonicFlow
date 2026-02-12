package com.sonicflow.ui.player.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
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
import kotlin.math.max

@Composable
fun WaveformView(
    amplitudes: List<Int>,
    progress: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // Afficher un indicateur de chargement si pas de données
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

        // Limiter le nombre de barres pour les performances
        val maxBars = 100
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
        val barWidth = (barSpacing * 0.6f)

        val progressIndex = (progress * processedAmplitudes.size).coerceIn(0f, processedAmplitudes.size - 1f)

        processedAmplitudes.forEachIndexed { index, amplitude ->
            val normalizedAmplitude = amplitude.toFloat() / maxAmplitude
            val barHeight = max(2f, normalizedAmplitude * canvasHeight * 0.7f)
            val x = index * barSpacing + (barSpacing - barWidth) / 2
            val y = (canvasHeight - barHeight) / 2

            val isPlayed = index < progressIndex
            val isCurrentBar = index == progressIndex.toInt()

            if (isCurrentBar) {
                // Barre courante avec effet de glow
                val glowBrush = Brush.verticalGradient(
                    colors = listOf(
                        VioletPrimary.copy(alpha = 1f),
                        VioletPrimary.copy(alpha = 0.9f),
                        VioletPrimary.copy(alpha = 1f)
                    )
                )

                drawRoundRect(
                    brush = glowBrush,
                    topLeft = Offset(x, y - 2.dp.toPx()),
                    size = Size(barWidth, barHeight + 4.dp.toPx()),
                    cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                )

                // Effet de lueur
                drawRoundRect(
                    color = VioletPrimary.copy(alpha = 0.2f),
                    topLeft = Offset(x - 2.dp.toPx(), y - 4.dp.toPx()),
                    size = Size(barWidth + 4.dp.toPx(), barHeight + 8.dp.toPx()),
                    cornerRadius = CornerRadius(barWidth, barWidth)
                )
            } else {
                val color = if (isPlayed) {
                    VioletPrimary.copy(alpha = 0.8f)
                } else {
                    Color.Gray.copy(alpha = 0.3f)
                }

                drawRoundRect(
                    color = color,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                )
            }
        }
    }
}