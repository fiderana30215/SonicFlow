package com.sonicflow.ui.player.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.sonicflow.ui.theme.VioletPrimary

@Composable
fun WaveformView(
    amplitudes: List<Int>,
    progress: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val seekProgress = offset.x / size.width
                    onSeek(seekProgress.coerceIn(0f, 1f))
                }
            }
    ) {
        if (amplitudes.isEmpty()) return@Canvas
        
        val canvasWidth = size.width
        val canvasHeight = size.height
        val barWidth = (canvasWidth / amplitudes.size) * 0.8f
        val barSpacing = canvasWidth / amplitudes.size
        val maxAmplitude = amplitudes.maxOrNull() ?: 1
        
        amplitudes.forEachIndexed { index, amplitude ->
            val barHeight = (amplitude.toFloat() / maxAmplitude) * canvasHeight * 0.8f
            val x = index * barSpacing
            val y = (canvasHeight - barHeight) / 2
            
            val barProgress = index.toFloat() / amplitudes.size
            val color = if (barProgress <= progress) {
                VioletPrimary
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
