package com.example.domain.audio

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Unified Sound and Haptic Feedback System for YAWMEK.
 * Provides subtle, elegant, optional audio tones and tactile feedback.
 */
class SoundHapticManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("yawmek_sound_haptics", Context.MODE_PRIVATE)

    private var toneGenerator: ToneGenerator? = null
    private var vibrator: Vibrator? = null
    private val audioScope = CoroutineScope(Dispatchers.Default)

    companion object {
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_HAPTICS_ENABLED = "haptics_enabled"

        @Volatile
        private var instance: SoundHapticManager? = null

        fun getInstance(context: Context): SoundHapticManager {
            return instance ?: synchronized(this) {
                instance ?: SoundHapticManager(context.applicationContext).also { instance = it }
            }
        }
    }

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
        } catch (e: Exception) {
            Log.w("SoundHapticManager", "ToneGenerator init failed", e)
        }

        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            Log.w("SoundHapticManager", "Vibrator init failed", e)
        }
    }

    var isSoundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()

    var isHapticsEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTICS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTICS_ENABLED, value).apply()

    fun playTaskCompleted() {
        if (isSoundEnabled) {
            audioScope.launch {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 80)
                    delay(90)
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 100)
                } catch (_: Exception) {}
            }
        }
        if (isHapticsEnabled) {
            vibratePattern(longArrayOf(0, 20, 30, 35))
        }
    }

    fun playHabitCompleted() {
        if (isSoundEnabled) {
            audioScope.launch {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_DTMF_D, 70)
                    delay(75)
                    toneGenerator?.startTone(ToneGenerator.TONE_DTMF_0, 90)
                } catch (_: Exception) {}
            }
        }
        if (isHapticsEnabled) {
            vibrate(28)
        }
    }

    fun playGoalMilestone() {
        if (isSoundEnabled) {
            audioScope.launch {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 70)
                    delay(80)
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 110)
                } catch (_: Exception) {}
            }
        }
        if (isHapticsEnabled) {
            vibratePattern(longArrayOf(0, 30, 40, 45))
        }
    }

    fun playFocusSessionCompleted() {
        if (isSoundEnabled) {
            audioScope.launch {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 220)
                } catch (_: Exception) {}
            }
        }
        if (isHapticsEnabled) {
            vibratePattern(longArrayOf(0, 50, 60, 80))
        }
    }

    fun playPlanApplied() {
        if (isSoundEnabled) {
            audioScope.launch {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 100)
                } catch (_: Exception) {}
            }
        }
        if (isHapticsEnabled) {
            vibrate(30)
        }
    }

    fun playTap() {
        if (isHapticsEnabled) {
            vibrate(15)
        }
    }

    fun playSplashTone() {
        if (isSoundEnabled) {
            audioScope.launch {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
                } catch (_: Exception) {}
            }
        }
    }

    fun playRpgLevelUp() {
        if (isSoundEnabled) {
            audioScope.launch {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
                    delay(90)
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 100)
                    delay(110)
                    toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 250)
                } catch (_: Exception) {}
            }
        }
        if (isHapticsEnabled) {
            vibratePattern(longArrayOf(0, 40, 50, 60, 50, 100))
        }
    }

    fun playRpgXpGain() {
        if (isSoundEnabled) {
            audioScope.launch {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 40)
                } catch (_: Exception) {}
            }
        }
        if (isHapticsEnabled) {
            vibrate(18)
        }
    }

    fun playRpgEquipmentEquipped() {
        if (isSoundEnabled) {
            audioScope.launch {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 70)
                } catch (_: Exception) {}
            }
        }
        if (isHapticsEnabled) {
            vibratePattern(longArrayOf(0, 30, 30, 40))
        }
    }

    fun playRpgBossHit() {
        if (isSoundEnabled) {
            audioScope.launch {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_SUP_ERROR, 120)
                } catch (_: Exception) {}
            }
        }
        if (isHapticsEnabled) {
            vibratePattern(longArrayOf(0, 50, 40, 60))
        }
    }

    private fun vibrate(durationMillis: Long) {
        try {
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(durationMillis)
                }
            }
        } catch (_: Exception) {}
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
        } catch (_: Exception) {}
    }

    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (_: Exception) {}
    }
}
