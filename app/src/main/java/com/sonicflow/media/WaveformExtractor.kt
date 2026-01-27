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

/**
 * Extracts waveform amplitude data from audio files using MediaExtractor
 */
@Singleton
class WaveformExtractor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    /**
     * Extract waveform amplitudes from an audio file
     * 
     * @param filePath Path to the audio file
     * @param samplesCount Number of samples to extract (default 100)
     * @return List of amplitude values normalized to 0-100 range
     */
    suspend fun extractWaveform(
        filePath: String,
        samplesCount: Int = 100
    ): List<Int> = withContext(Dispatchers.IO) {
        val amplitudes = mutableListOf<Int>()
        
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
                return@withContext emptyList()
            }
            
            extractor.selectTrack(audioTrackIndex)
            
            val format = extractor.getTrackFormat(audioTrackIndex)
            val sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            val duration = format.getLong(MediaFormat.KEY_DURATION)
            
            // Calculate sample interval
            val totalSamples = (duration / 1_000_000.0 * sampleRate).toLong()
            val sampleInterval = (totalSamples / samplesCount).coerceAtLeast(1)
            
            val buffer = java.nio.ByteBuffer.allocate(4096)
            var sampleIndex = 0L
            var maxAmplitude = 0
            var currentAmplitude = 0
            var samplesInBucket = 0
            
            while (true) {
                val sampleSize = extractor.readSampleData(buffer, 0)
                if (sampleSize < 0) break
                
                // Calculate amplitude from buffer
                buffer.rewind()
                val amplitude = calculateAmplitude(buffer, sampleSize)
                
                currentAmplitude += amplitude
                samplesInBucket++
                
                if (sampleIndex % sampleInterval == 0L && samplesInBucket > 0) {
                    val avgAmplitude = currentAmplitude / samplesInBucket
                    amplitudes.add(avgAmplitude)
                    maxAmplitude = maxAmplitude.coerceAtLeast(avgAmplitude)
                    currentAmplitude = 0
                    samplesInBucket = 0
                    
                    if (amplitudes.size >= samplesCount) break
                }
                
                extractor.advance()
                sampleIndex++
                buffer.clear()
            }
            
            extractor.release()
            
            // Normalize amplitudes to 0-100 range
            if (maxAmplitude > 0) {
                amplitudes.map { (it * 100) / maxAmplitude }
            } else {
                amplitudes
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * Calculate amplitude from audio buffer
     */
    private fun calculateAmplitude(buffer: java.nio.ByteBuffer, size: Int): Int {
        var sum = 0L
        var count = 0
        
        // Assume 16-bit PCM audio
        for (i in 0 until size step 2) {
            if (i + 1 < size) {
                val sample = (buffer[i].toInt() and 0xFF) or
                        ((buffer[i + 1].toInt() and 0xFF) shl 8)
                sum += abs(sample.toShort().toInt())
                count++
            }
        }
        
        return if (count > 0) (sum / count).toInt() else 0
    }
}
