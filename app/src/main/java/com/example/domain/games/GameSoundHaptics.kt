package com.example.domain.games

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

class GameSoundHaptics(private val context: Context) {

    private var toneGenerator: ToneGenerator? = null
    private var vibrator: Vibrator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 75)
        } catch (e: Exception) {
            Log.w("GameSoundHaptics", "ToneGenerator init failed", e)
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
            Log.w("GameSoundHaptics", "Vibrator init failed", e)
        }
    }

    fun playCorrect(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (soundEnabled) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 120)
            } catch (e: Exception) {
                // Ignore audio failure
            }
        }
        if (hapticsEnabled) {
            vibrate(35)
        }
    }

    fun playWrong(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (soundEnabled) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 180)
            } catch (e: Exception) {
                // Ignore audio failure
            }
        }
        if (hapticsEnabled) {
            vibrate(70)
        }
    }

    fun playRecord(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (soundEnabled) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 300)
            } catch (e: Exception) {
                // Ignore audio failure
            }
        }
        if (hapticsEnabled) {
            vibratePattern(longArrayOf(0, 50, 60, 100))
        }
    }

    fun playTap(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (soundEnabled) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 40)
            } catch (e: Exception) {
                // Ignore audio failure
            }
        }
        if (hapticsEnabled) {
            vibrate(20)
        }
    }

    private fun vibrate(durationMillis: Long) {
        try {
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(durationMillis)
                }
            }
        } catch (e: Exception) {
            // Ignore vibration error
        }
    }

    private fun vibratePattern(pattern: LongArray) {
        try {
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(pattern, -1)
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (e: Exception) {
            // Ignore
        }
    }
}
