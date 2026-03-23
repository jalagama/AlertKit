package com.jalagama.popup.core

import android.app.Activity

/**
 * Supplies the current foreground [Activity] used to host dialogs.
 * Implementations should hold [Activity] via [java.lang.ref.WeakReference] to avoid leaks.
 */
fun interface ActivityProvider {
    fun currentActivity(): Activity?
}
