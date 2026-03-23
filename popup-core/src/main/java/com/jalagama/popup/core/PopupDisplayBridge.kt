package com.jalagama.popup.core

import android.content.Context

/**
 * View-system UI contract implemented by `popup-ui` / `popup-compose`.
 * Must be called on the main thread by [PopupManager].
 */
interface PopupDisplayBridge {
    /**
     * Presents [request]. Return false if the UI could not be created (caller may retry later).
     */
    fun show(context: Context, request: PopupRequest, listener: PopupDisplayListener): Boolean

    /** Dismisses the currently hosted UI without invoking [PopupDisplayListener.onDismissed] as user action. */
    fun dismissProgrammatically()

    fun isShowing(): Boolean
}

interface PopupDisplayListener {
    fun onButtonClicked(buttonId: String)

    fun onListItemClicked(itemId: String)

    /**
     * @param userInitiated true when the user dismissed via outside touch / back where applicable.
     */
    fun onDismissed(userInitiated: Boolean)
}
