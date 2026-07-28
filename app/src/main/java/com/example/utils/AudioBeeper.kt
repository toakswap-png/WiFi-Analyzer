package com.example.utils

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log

class AudioBeeper {
    private var toneGenerator: ToneGenerator? = null
    private var isMuted = true

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        } catch (e: Exception) {
            Log.e("AudioBeeper", "Failed to initialize ToneGenerator: ${e.message}")
        }
    }

    fun setMuted(muted: Boolean) {
        isMuted = muted
    }

    fun isMuted(): Boolean = isMuted

    fun playBeepForSignal(rssi: Int) {
        if (isMuted || toneGenerator == null) return

        try {
            // Stronger signal -> higher tone & shorter duration for crisp rapid beeping
            val (toneType, durationMs) = when {
                rssi > -55 -> Pair(ToneGenerator.TONE_PROP_BEEP2, 50)
                rssi >= -65 -> Pair(ToneGenerator.TONE_PROP_BEEP, 60)
                rssi >= -74 -> Pair(ToneGenerator.TONE_PROP_PROMPT, 75)
                rssi >= -83 -> Pair(ToneGenerator.TONE_CDMA_PIP, 90)
                else -> Pair(ToneGenerator.TONE_PROP_ACK, 100)
            }
            toneGenerator?.startTone(toneType, durationMs)
        } catch (e: Exception) {
            Log.e("AudioBeeper", "Error playing beep tone: ${e.message}")
        }
    }

    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (e: Exception) {
            Log.e("AudioBeeper", "Error releasing ToneGenerator: ${e.message}")
        }
    }
}
