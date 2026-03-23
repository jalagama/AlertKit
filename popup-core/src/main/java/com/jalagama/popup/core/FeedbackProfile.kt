package com.jalagama.popup.core

/**
 * Default audio/haptic behavior for a priority tier. Implementations interpret [vibrationPattern]
 * using platform APIs (see `popup-ui`).
 */
data class FeedbackProfile(
    val playSound: Boolean,
    val vibrationPattern: LongArray? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as FeedbackProfile
        if (playSound != other.playSound) return false
        if (vibrationPattern != null) {
            if (other.vibrationPattern == null) return false
            if (!vibrationPattern.contentEquals(other.vibrationPattern)) return false
        } else if (other.vibrationPattern != null) return false
        return true
    }

    override fun hashCode(): Int {
        var result = playSound.hashCode()
        result = 31 * result + (vibrationPattern?.contentHashCode() ?: 0)
        return result
    }
}

/**
 * Maps [PopupPriority] to default feedback. Consumers may copy and override entries.
 */
data class FeedbackMapping(
    val byPriority: Map<PopupPriority, FeedbackProfile> = defaultByPriority(),
) {
    fun profileFor(priority: PopupPriority): FeedbackProfile {
        return byPriority[priority] ?: defaultByPriority()[priority]!!
    }

    companion object {
        fun defaultByPriority(): Map<PopupPriority, FeedbackProfile> {
            val gentle = longArrayOf(0, 40)
            val strong = longArrayOf(0, 60, 80, 60)
            return mapOf(
                PopupPriority.LOW to FeedbackProfile(playSound = false, vibrationPattern = null),
                PopupPriority.MEDIUM to FeedbackProfile(playSound = false, vibrationPattern = gentle),
                PopupPriority.HIGH to FeedbackProfile(playSound = true, vibrationPattern = strong),
                PopupPriority.CRITICAL to FeedbackProfile(playSound = true, vibrationPattern = strong),
            )
        }
    }
}
