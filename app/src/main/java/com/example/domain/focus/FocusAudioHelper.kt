package com.example.domain.focus

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.*
import kotlin.math.sin
import kotlin.random.Random

class FocusAudioHelper(private val context: Context) {

    private var toneGenerator: ToneGenerator? = null
    private var vibrator: Vibrator? = null
    private var ambientTrack: AudioTrack? = null
    private var ambientJob: Job? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 65)
        } catch (e: Exception) {
            // Audio unavailable
        }

        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            // Vibrator unavailable
        }
    }

    fun playSessionStart() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 200)
            vibrate(50)
        } catch (e: Exception) {}
    }

    fun playSessionPause() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
            vibrate(30)
        } catch (e: Exception) {}
    }

    fun playSessionComplete() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 400)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 100, 200), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(300)
            }
        } catch (e: Exception) {}
    }

    fun startAmbient(soundType: String, scope: CoroutineScope) {
        stopAmbient()
        if (soundType == "SILENT") return

        ambientJob = scope.launch(Dispatchers.Default) {
            try {
                val sampleRate = 22050
                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufferSize = (minBufferSize * 2).coerceAtLeast(4096)

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                ambientTrack = track
                track.play()

                val buffer = ShortArray(1024)
                var lastVal = 0.0
                var phase = 0.0

                while (isActive) {
                    when (soundType) {
                        "WHITE_NOISE" -> {
                            for (i in buffer.indices) {
                                buffer[i] = ((Random.nextDouble() * 2 - 1) * 3000).toInt().toShort()
                            }
                        }
                        "RAIN" -> {
                            // Pink / brown filtered noise simulation for soft rain
                            for (i in buffer.indices) {
                                val white = Random.nextDouble() * 2 - 1
                                lastVal = (lastVal + (0.05 * white)) / 1.05
                                buffer[i] = (lastVal * 7000).toInt().coerceIn(-32767, 32767).toShort()
                            }
                        }
                        "BINAURAL" -> {
                            // 432Hz sine wave alpha focus tone
                            val freq = 432.0
                            val delta = 2.0 * Math.PI * freq / sampleRate
                            for (i in buffer.indices) {
                                buffer[i] = (sin(phase) * 4500).toInt().toShort()
                                phase += delta
                                if (phase > 2.0 * Math.PI) phase -= 2.0 * Math.PI
                            }
                        }
                        "CHIME" -> {
                            // Periodic gentle pulse
                            val freq = 528.0
                            val delta = 2.0 * Math.PI * freq / sampleRate
                            for (i in buffer.indices) {
                                buffer[i] = (sin(phase) * 2000).toInt().toShort()
                                phase += delta
                            }
                            delay(1500)
                        }
                        else -> {
                            for (i in buffer.indices) buffer[i] = 0
                        }
                    }
                    track.write(buffer, 0, buffer.size)
                }
            } catch (e: Exception) {
                // Audio stream ended or cancelled safely
            }
        }
    }

    fun stopAmbient() {
        ambientJob?.cancel()
        ambientJob = null
        try {
            ambientTrack?.stop()
            ambientTrack?.release()
        } catch (e: Exception) {}
        ambientTrack = null
    }

    private fun vibrate(durationMillis: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMillis)
            }
        } catch (e: Exception) {}
    }

    fun release() {
        stopAmbient()
        try {
            toneGenerator?.release()
        } catch (e: Exception) {}
        toneGenerator = null
    }
}
