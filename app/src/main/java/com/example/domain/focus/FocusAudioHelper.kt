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
    var isSoundEnabled: Boolean = true
    var isHapticsEnabled: Boolean = true
    var currentVolume: Float = 0.7f

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
        if (!isSoundEnabled && !isHapticsEnabled) return
        try {
            if (isSoundEnabled) toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 200)
            if (isHapticsEnabled) vibrate(50)
        } catch (e: Exception) {}
    }

    fun playSessionPause() {
        if (!isSoundEnabled && !isHapticsEnabled) return
        try {
            if (isSoundEnabled) toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
            if (isHapticsEnabled) vibrate(30)
        } catch (e: Exception) {}
    }

    fun playSessionComplete() {
        if (!isSoundEnabled && !isHapticsEnabled) return
        try {
            if (isSoundEnabled) toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 400)
            if (isHapticsEnabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 100, 200), -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(300)
                }
            }
        } catch (e: Exception) {}
    }

    fun setVolume(volume: Float) {
        currentVolume = volume.coerceIn(0.0f, 1.0f)
        try {
            ambientTrack?.setVolume(currentVolume)
        } catch (e: Exception) {}
    }

    fun startAmbient(soundType: String, scope: CoroutineScope) {
        stopAmbient()
        if (!isSoundEnabled || soundType == "SILENT") return

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

                track.setVolume(currentVolume)
                ambientTrack = track
                track.play()

                val buffer = ShortArray(1024)
                var lastVal = 0.0
                var brownVal = 0.0
                var phase = 0.0
                var chirpTimer = 0

                while (isActive) {
                    when (soundType) {
                        "WHITE_NOISE" -> {
                            val gain = (currentVolume * 4000).toInt()
                            for (i in buffer.indices) {
                                buffer[i] = ((Random.nextDouble() * 2 - 1) * gain).toInt().toShort()
                            }
                        }
                        "BROWN_NOISE" -> {
                            // True integrated Brownian / red noise for deep low focus
                            val gain = (currentVolume * 8000).toInt()
                            for (i in buffer.indices) {
                                val white = Random.nextDouble() * 2 - 1
                                brownVal = (brownVal + (0.02 * white)) / 1.02
                                buffer[i] = (brownVal * gain).toInt().coerceIn(-32767, 32767).toShort()
                            }
                        }
                        "RAIN" -> {
                            // Pink / brown filtered noise simulation for soft rain
                            val gain = (currentVolume * 7000).toInt()
                            for (i in buffer.indices) {
                                val white = Random.nextDouble() * 2 - 1
                                lastVal = (lastVal + (0.06 * white)) / 1.06
                                buffer[i] = (lastVal * gain).toInt().coerceIn(-32767, 32767).toShort()
                            }
                        }
                        "FOREST" -> {
                            // Gentle wind rustle with subtle organic high chirps
                            val gain = (currentVolume * 4000).toInt()
                            chirpTimer++
                            val isChirp = chirpTimer in 4000..4200
                            if (chirpTimer > 8000) chirpTimer = 0
                            for (i in buffer.indices) {
                                val white = Random.nextDouble() * 2 - 1
                                lastVal = (lastVal + (0.04 * white)) / 1.04
                                var sample = lastVal * gain
                                if (isChirp) {
                                    sample += sin(phase * 4) * (gain * 0.4)
                                }
                                buffer[i] = sample.toInt().coerceIn(-32767, 32767).toShort()
                                phase += 0.05
                            }
                        }
                        "SOFT_AMBIENCE" -> {
                            // Soothing warm harmonic drone (432Hz + 528Hz sub-octave)
                            val gain = (currentVolume * 3500).toInt()
                            val delta1 = 2.0 * Math.PI * 216.0 / sampleRate
                            val delta2 = 2.0 * Math.PI * 432.0 / sampleRate
                            for (i in buffer.indices) {
                                val drone = (sin(phase) * 0.7 + sin(phase * 2) * 0.3) * gain
                                buffer[i] = drone.toInt().toShort()
                                phase += delta1
                                if (phase > 2.0 * Math.PI) phase -= 2.0 * Math.PI
                            }
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
        if (!isHapticsEnabled) return
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
