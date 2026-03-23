package com.jalagama.popup.core

/**
 * Marshals work to the UI thread. The Android implementation typically wraps [android.os.Handler].
 */
fun interface MainThreadExecutor {
    fun execute(block: () -> Unit)
}
