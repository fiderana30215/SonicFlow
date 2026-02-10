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
import androidx.compose.ui.graphics.Brush
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
        
        // Calculate current bar index
        val currentBarIndex = (progress * amplitudes.size).toInt().coerceIn(0, amplitudes.size - 1)
        
        amplitudes.forEachIndexed { index, amplitude ->
            val barHeight = (amplitude.toFloat() / maxAmplitude) * canvasHeight * 0.8f
            val x = index * barSpacing
            val y = (canvasHeight - barHeight) / 2
            
            val barProgress = index.toFloat() / amplitudes.size
            val isCurrentBar = index == currentBarIndex
            
            // Determine rendering style for this bar
            when {
                isCurrentBar && progress > 0 -> {
                    // Scale effect for current bar
                    val scale = 1.3f
                    val scaledHeight = barHeight * scale
                    val scaledY = (canvasHeight - scaledHeight) / 2
                    
                    // Glowing effect for current bar using gradient
                    val gradientBrush = Brush.verticalGradient(
                        colors = listOf(
                            VioletPrimary.copy(alpha = 1f),
                            VioletPrimary.copy(alpha = 0.8f),
                            VioletPrimary.copy(alpha = 1f)
                        )
                    )
                    drawRoundRect(
                        brush = gradientBrush,
                        topLeft = Offset(x, scaledY),
                        size = Size(barWidth, scaledHeight),
                        cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                    )
                }
                barProgress <= progress -> {
                    // Played bars - solid color, no scaling
                    drawRoundRect(
                        color = VioletPrimary.copy(alpha = 0.7f),
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                    )
                }
                else -> {
                    // Unplayed bars - solid color, no scaling
                    drawRoundRect(
                        color = Color.Gray.copy(alpha = 0.3f),
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                    )
                }
            }
        }
    }
}
