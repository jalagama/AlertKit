package com.jalagama.popup.core

/**
 * Reflects process foreground state (typically backed by [androidx.lifecycle.ProcessLifecycleOwner]).
 */
fun interface ForegroundStateProvider {
    fun isAppInForeground(): Boolean
}
