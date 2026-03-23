package com.jalagama.popup.core

/** Debug logging for the queue and UI bridge; disabled by default in [NoOpPopupLogger]. */
interface PopupLogger {
    fun isEnabled(): Boolean

    fun d(tag: String, message: String)

    fun w(tag: String, message: String)
}

object NoOpPopupLogger : PopupLogger {
    override fun isEnabled(): Boolean = false

    override fun d(tag: String, message: String) {}

    override fun w(tag: String, message: String) {}
}

class DebugPopupLogger : PopupLogger {
    override fun isEnabled(): Boolean = true

    override fun d(tag: String, message: String) {
        android.util.Log.d(tag, message)
    }

    override fun w(tag: String, message: String) {
        android.util.Log.w(tag, message)
    }
}
