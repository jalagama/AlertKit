package com.jalagama.popup.ui

import android.os.Handler
import android.os.Looper
import com.jalagama.popup.core.MainThreadExecutor

/** Posts work to the main looper; runs inline when already on the main thread. */
class AndroidMainThreadExecutor(
    private val handler: Handler = Handler(Looper.getMainLooper()),
) : MainThreadExecutor {
    override fun execute(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block()
        } else {
            handler.post(block)
        }
    }
}
