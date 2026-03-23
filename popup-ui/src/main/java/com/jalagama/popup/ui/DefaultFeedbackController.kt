package com.jalagama.popup.ui

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.jalagama.popup.core.FeedbackController
import com.jalagama.popup.core.FeedbackMapping
import com.jalagama.popup.core.PopupRequest

/**
 * Applies [FeedbackMapping] with per-request overrides. Uses application [Context] to avoid leaks.
 */
class DefaultFeedbackController(
    context: Context,
    private val mapping: FeedbackMapping = FeedbackMapping(),
) : FeedbackController {

    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private val toneLock = Any()
    private var activeTone: ToneGenerator? = null

    private val releaseToneRunnable = Runnable {
        synchronized(toneLock) {
            runCatching { activeTone?.release() }
            activeTone = null
        }
    }

    override fun onPopupShown(request: PopupRequest) {
        val profile = mapping.profileFor(request.priority)
        val sound = request.soundEnabled ?: profile.playSound
        val vibrate = request.vibrationEnabled ?: (profile.vibrationPattern != null)
        if (sound) {
            playShortTone()
        }
        if (vibrate) {
            val pattern = profile.vibrationPattern ?: longArrayOf(0, 40)
            vibrate(pattern)
        }
    }

    private fun playShortTone() {
        synchronized(toneLock) {
            runCatching {
                mainHandler.removeCallbacks(releaseToneRunnable)
                runCatching { activeTone?.release() }
                val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 70)
                activeTone = tone
                tone.startTone(ToneGenerator.TONE_PROP_ACK, 120)
                mainHandler.postDelayed(releaseToneRunnable, 280L)
            }
        }
    }

    private fun vibrate(pattern: LongArray) {
        val vibrator = systemVibrator() ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(pattern, -1)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }

    private fun systemVibrator(): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}
