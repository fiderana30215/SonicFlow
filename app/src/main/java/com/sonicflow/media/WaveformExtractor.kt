package com.sonicflow.media

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Singleton
class WaveformExtractor @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun extractWaveform(
        filePath: String,
        samplesCount: Int = 100
    ): List<Int> = withContext(Dispatchers.IO) {
        try {
            val extractor = MediaExtractor()
            extractor.setDataSource(filePath)

            // Find audio track
            var audioTrackIndex = -1
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("audio/")) {
                    audioTrackIndex = i
                    break
                }
            }

            if (audioTrackIndex == -1) {
                extractor.release()
                return@withContext generateFallbackWaveform(samplesCount)
            }

            extractor.selectTrack(audioTrackIndex)

            val format = extractor.getTrackFormat(audioTrackIndex)

            // Get audio format properties
            val duration = format.getLong(MediaFormat.KEY_DURATION)

            // Calculate sample interval based on file size and duration
            val bucketCount = samplesCount * 2 // Collect more samples for better averaging
            val bucketSizes = Array(bucketCount) { 0 }
            val bucketAmplitudes = Array(bucketCount) { 0L }

            val buffer = java.nio.ByteBuffer.allocate(4096)
            var bucketIndex = 0
            var samplesRead = 0L
            val maxAmplitudeValue = 32767 // 16-bit PCM max value

            while (true) {
                val sampleSize = extractor.readSampleData(buffer, 0)
                if (sampleSize < 0) break

                // Calculate amplitude from buffer
                buffer.rewind()
                val amplitude = calculateAmplitude(buffer, sampleSize)

                if (amplitude > 0) {
                    bucketAmplitudes[bucketIndex % bucketCount] += amplitude
                    bucketSizes[bucketIndex % bucketCount]++
                    bucketIndex++
                }

                extractor.advance()
                samplesRead++
                buffer.clear()
            }

            extractor.release()

            // Calculate average amplitudes for each bucket
            val averagedAmplitudes = mutableListOf<Int>()
            for (i in 0 until bucketCount) {
                if (bucketSizes[i] > 0) {
                    val avgAmplitude = (bucketAmplitudes[i] / bucketSizes[i]).toInt()
                    averagedAmplitudes.add(avgAmplitude)
                }
            }

            // Resample to desired number of samples
            val finalAmplitudes = if (averagedAmplitudes.isNotEmpty()) {
                resampleWaveform(averagedAmplitudes, samplesCount)
            } else {
                generateFallbackWaveform(samplesCount)
            }

            // Normalize to 10-100 range for visibility
            normalizeWaveform(finalAmplitudes, minValue = 10, maxValue = 100)

        } catch (e: Exception) {
            e.printStackTrace()
            generateFallbackWaveform(samplesCount)
        }
    }

    private fun calculateAmplitude(buffer: java.nio.ByteBuffer, size: Int): Int {
        var maxAmplitude = 0

        // Process 16-bit PCM audio
        for (i in 0 until size step 2) {
            if (i + 1 < size) {
                val sample = (buffer[i].toInt() and 0xFF) or
                        ((buffer[i + 1].toInt() and 0xFF) shl 8)
                val absoluteValue = abs(sample.toShort().toInt())
                maxAmplitude = max(maxAmplitude, absoluteValue)
            }
        }

        return maxAmplitude
    }

    private fun resampleWaveform(source: List<Int>, targetSize: Int): List<Int> {
        if (source.isEmpty()) return emptyList()
        if (source.size == targetSize) return source

        val result = mutableListOf<Int>()
        val scale = source.size.toDouble() / targetSize.toDouble()

        for (i in 0 until targetSize) {
            val startPos = (i * scale).toInt()
            val endPos = ((i + 1) * scale).toInt().coerceAtMost(source.size)

            if (endPos > startPos) {
                val slice = source.subList(startPos, endPos)
                val maxInSlice = slice.maxOrNull() ?: 0
                result.add(maxInSlice)
            } else {
                result.add(source[startPos.coerceAtMost(source.size - 1)])
            }
        }

        return result
    }

    private fun normalizeWaveform(amplitudes: List<Int>, minValue: Int, maxValue: Int): List<Int> {
        if (amplitudes.isEmpty()) return amplitudes

        val currentMax = amplitudes.maxOrNull() ?: 1
        val currentMin = amplitudes.minOrNull() ?: 0

        return if (currentMax > currentMin) {
            amplitudes.map { amplitude ->
                val normalized = (amplitude - currentMin).toFloat() / (currentMax - currentMin)
                (normalized * (maxValue - minValue) + minValue).toInt()
            }
        } else {
            amplitudes.map { minValue + (maxValue - minValue) / 2 }
        }
    }

    private fun generateFallbackWaveform(samplesCount: Int): List<Int> {
        val result = mutableListOf<Int>()
        val random = java.util.Random()

        for (i in 0 until samplesCount) {
            // Créer un pattern réaliste avec variations
            val phase = i.toFloat() / samplesCount
            val pattern = when {
                phase < 0.1 -> 30  // Intro
                phase < 0.3 -> 50  // Montée
                phase < 0.7 -> 80  // Refrain
                phase < 0.9 -> 60  // Transition
                else -> 40         // Outro
            }

            // Ajouter des variations aléatoires
            val variation = random.nextInt(20) - 10
            val amplitude = (pattern + variation).coerceIn(15, 95)
            result.add(amplitude)
        }

        return result
    }
}