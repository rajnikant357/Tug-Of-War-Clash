package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class SoundManager(context: Context) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(Dispatchers.Default)

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    var isSoundEnabled: Boolean = true
    var isHapticsEnabled: Boolean = true

    private fun playPcm(sampleRate: Int, samples: ShortArray) {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(samples.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(samples, 0, samples.size)
                track.play()
                // Wait for playback and release
                val durationMs = (samples.size * 1000L) / sampleRate
                kotlinx.coroutines.delay(durationMs + 50)
                track.stop()
                track.release()
            } catch (_: Exception) {
                // Ignore audio write errors on background or low memory
            }
        }
    }

    fun playTap(isPlayer1: Boolean) {
        if (isHapticsEnabled) {
            triggerHaptic(30, 80)
        }
        if (!isSoundEnabled) return

        val sampleRate = 22050
        val durationMs = 45
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)

        val baseFreq = if (isPlayer1) 220.0 else 280.0
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val decay = (1.0 - (i.toDouble() / numSamples))
            val envelope = decay * decay
            // Wood/drum thud sound with punch
            val wave = sin(2.0 * PI * (baseFreq * (1.0 - t * 8.0).coerceAtLeast(0.5)) * t)
            val sub = 0.5 * sin(2.0 * PI * (baseFreq * 0.5) * t)
            buffer[i] = ((wave + sub) * envelope * 0.7 * Short.MAX_VALUE).toInt().toShort()
        }
        playPcm(sampleRate, buffer)
    }

    fun playStrain() {
        if (!isSoundEnabled) return
        val sampleRate = 22050
        val durationMs = 120
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val decay = sin((i.toDouble() / numSamples) * PI)
            val freq = 450.0 + sin(2.0 * PI * 18.0 * t) * 120.0
            val wave = sin(2.0 * PI * freq * t)
            buffer[i] = (wave * decay * 0.4 * Short.MAX_VALUE).toInt().toShort()
        }
        playPcm(sampleRate, buffer)
    }

    fun playSurge() {
        if (isHapticsEnabled) {
            triggerHaptic(70, 200)
        }
        if (!isSoundEnabled) return

        val sampleRate = 22050
        val durationMs = 260
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val freq = 200.0 + progress * 600.0 // rising whoosh
            val t = i.toDouble() / sampleRate
            val envelope = sin(progress * PI)
            val wave = sin(2.0 * PI * freq * t) + 0.3 * sin(2.0 * PI * (freq * 2.0) * t)
            buffer[i] = (wave * envelope * 0.6 * Short.MAX_VALUE).toInt().toShort()
        }
        playPcm(sampleRate, buffer)
    }

    fun playWhistle() {
        if (isHapticsEnabled) {
            triggerHaptic(120, 255)
        }
        if (!isSoundEnabled) return

        val sampleRate = 22050
        val durationMs = 450
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples
            val envelope = when {
                progress < 0.15 -> progress / 0.15
                progress > 0.85 -> (1.0 - progress) / 0.15
                else -> 1.0
            }
            // Referee whistle trill with beating dual tones
            val trill = 1.0 + 0.15 * sin(2.0 * PI * 35.0 * t)
            val wave1 = sin(2.0 * PI * 2600.0 * trill * t)
            val wave2 = sin(2.0 * PI * 2900.0 * trill * t)
            buffer[i] = ((wave1 + wave2) * 0.4 * envelope * Short.MAX_VALUE).toInt().toShort()
        }
        playPcm(sampleRate, buffer)
    }

    fun playCountdownBeep(isFinal: Boolean) {
        if (isHapticsEnabled) {
            triggerHaptic(if (isFinal) 100 else 40, if (isFinal) 220 else 100)
        }
        if (!isSoundEnabled) return

        val sampleRate = 22050
        val durationMs = if (isFinal) 300 else 120
        val freq = if (isFinal) 880.0 else 440.0
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples
            val envelope = (1.0 - progress)
            val wave = sin(2.0 * PI * freq * t)
            buffer[i] = (wave * envelope * 0.5 * Short.MAX_VALUE).toInt().toShort()
        }
        playPcm(sampleRate, buffer)
    }

    fun playVictoryFanfare() {
        if (isHapticsEnabled) {
            triggerHapticPattern()
        }
        if (!isSoundEnabled) return

        val sampleRate = 22050
        val notes = listOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
        val noteDurationMs = 150
        val totalSamples = (sampleRate * noteDurationMs * notes.size) / 1000
        val buffer = ShortArray(totalSamples)

        for (noteIdx in notes.indices) {
            val freq = notes[noteIdx]
            val offset = (sampleRate * noteDurationMs * noteIdx) / 1000
            val noteSamples = (sampleRate * noteDurationMs) / 1000

            for (i in 0 until noteSamples) {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / noteSamples
                val envelope = (1.0 - progress * 0.7)
                val wave = sin(2.0 * PI * freq * t) + 0.25 * sin(2.0 * PI * (freq * 2.0) * t)
                val idx = offset + i
                if (idx < totalSamples) {
                    buffer[idx] = (wave * envelope * 0.5 * Short.MAX_VALUE).toInt().toShort()
                }
            }
        }
        playPcm(sampleRate, buffer)
    }

    private fun triggerHaptic(durationMs: Long, amplitude: Int = 120) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val clampedAmp = amplitude.coerceIn(1, 255)
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, clampedAmp))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {
            // Ignore security/device vibration issues
        }
    }

    private fun triggerHapticPattern() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 80, 50, 80, 50, 200)
                val amplitudes = intArrayOf(0, 150, 0, 180, 0, 255)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 80, 50, 80, 50, 200), -1)
            }
        } catch (_: Exception) {}
    }
}
