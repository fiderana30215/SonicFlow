package com.sonicflow.ui.player.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sonicflow.ui.theme.VioletPrimary
import com.sonicflow.ui.theme.VioletSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun AnimatedWaveformDisk(
    albumArtUri: String?,
    isPlaying: Boolean,
    waveformData: List<Int> = emptyList(),
    modifier: Modifier = Modifier
) {
    // Rotation du disque
    var rotation by remember { mutableStateOf(0f) }

    // Animation des barres du waveform
    var waveformPhase by remember { mutableStateOf(0f) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isActive) {
                delay(16) // ~60 FPS
                rotation = (rotation + 0.3f) % 360f
                waveformPhase = (waveformPhase + 0.15f) % (2 * PI.toFloat()) // Plus rapide
            }
        }
    }

    // Couleurs variées pour les barres (comme dans l'image)
    val barColors = remember {
        listOf(
            Color(0xFFE91E63), // Rose/Magenta
            Color(0xFF9C27B0), // Violet
            Color(0xFF673AB7), // Violet foncé
            Color(0xFF00BCD4), // Cyan
            Color(0xFF4CAF50), // Vert
            Color(0xFFFFFFFF), // Blanc
        )
    }

    Box(
        modifier = modifier.size(400.dp),
        contentAlignment = Alignment.Center
    ) {
        // Waveform circulaire animé
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasWidth = this.size.width
            val canvasHeight = this.size.height
            val centerX = canvasWidth / 2
            val centerY = canvasHeight / 2
            val radius = minOf(canvasWidth, canvasHeight) / 2 - 20f // Rayon plus grand (réduit la marge de 40f à 20f)

            // Nombre de barres ajusté pour l'espacement
            val numberOfBars = 72 // Réduit pour créer plus d'espace entre les barres
            val angleStep = (2 * PI) / numberOfBars

            repeat(numberOfBars) { i ->
                val angle = i * angleStep - PI / 2

                // Amplitude variable basée sur l'index et la phase d'animation
                val baseAmplitude = if (waveformData.isNotEmpty() && i < waveformData.size) {
                    waveformData[i].toFloat() / 100f
                } else {
                    // Pattern plus varié avec des hauteurs très différentes
                    val pattern = when {
                        i % 7 == 0 -> 0.95f // Très hautes
                        i % 5 == 0 -> 0.75f // Hautes
                        i % 3 == 0 -> 0.5f  // Moyennes
                        i % 2 == 0 -> 0.3f  // Basses
                        else -> 0.15f       // Très basses
                    }
                    pattern
                }

                // Animation sinusoïdale pour créer un effet de vague
                val animatedAmplitude = if (isPlaying) {
                    val wave = sin(waveformPhase + (i * 0.2f)).toFloat()
                    baseAmplitude * (0.8f + wave * 0.4f)
                } else {
                    baseAmplitude * 0.5f
                }

                // Hauteur de la barre avec plus de variation
                val minHeight = 10f
                val maxHeight = 80f
                val barHeight = minHeight + (animatedAmplitude * maxHeight)
                val barWidth = 20f // 2cm ≈ 20dp

                // Couleur basée sur la position pour créer un motif
                val colorIndex = (i / 3) % barColors.size
                val color = if (isPlaying) {
                    barColors[colorIndex].copy(alpha = 0.85f)
                } else {
                    barColors[colorIndex].copy(alpha = 0.3f)
                }

                // Position de départ de la barre (au bord du cercle)
                val startX = centerX + radius * cos(angle).toFloat()
                val startY = centerY + radius * sin(angle).toFloat()

                // Position de fin de la barre (vers l'extérieur)
                val endX = centerX + (radius + barHeight) * cos(angle).toFloat()
                val endY = centerY + (radius + barHeight) * sin(angle).toFloat()

                // Calculer les points perpendiculaires pour la largeur de la barre
                val perpAngle = angle + PI / 2
                val halfWidth = barWidth / 2

                val dx = halfWidth * cos(perpAngle).toFloat()
                val dy = halfWidth * sin(perpAngle).toFloat()

                // Les 4 coins du rectangle
                val p1X = startX - dx
                val p1Y = startY - dy
                val p2X = startX + dx
                val p2Y = startY + dy
                val p3X = endX + dx
                val p3Y = endY + dy
                val p4X = endX - dx
                val p4Y = endY - dy

                // Dessiner la barre comme une ligne épaisse avec des bouts droits (sans arrondi)
                drawLine(
                    color = color,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = barWidth,
                    cap = androidx.compose.ui.graphics.StrokeCap.Butt // Bouts droits sans arrondi
                )
            }
        }

        // Disque principal qui tourne
        Card(
            modifier = Modifier
                .size(360.dp) // Agrandi de 280.dp à 320.dp
                .rotate(rotation),
            shape = CircleShape,
            elevation = CardDefaults.cardElevation(defaultElevation = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(4.dp, VioletPrimary, CircleShape)
            ) {
                AsyncImage(
                    model = albumArtUri,
                    contentDescription = "Album Art",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Centre du disque
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF222222))
                        .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        .align(Alignment.Center)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.6f))
                            .align(Alignment.Center)
                    )
                }
            }
        }
    }
}