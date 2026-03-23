package com.jalagama.popup.ui

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.jalagama.popup.core.ForegroundStateProvider
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Uses [ProcessLifecycleOwner] so queue display pauses while the app is backgrounded.
 */
class ProcessForegroundStateProvider : ForegroundStateProvider, DefaultLifecycleObserver {
    private val foreground = AtomicBoolean(false)

    init {
        val owner = ProcessLifecycleOwner.get()
        owner.lifecycle.addObserver(this)
        foreground.set(owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
    }

    override fun isAppInForeground(): Boolean = foreground.get()

    override fun onStart(owner: LifecycleOwner) {
        foreground.set(true)
    }

    override fun onStop(owner: LifecycleOwner) {
        foreground.set(false)
    }
}
