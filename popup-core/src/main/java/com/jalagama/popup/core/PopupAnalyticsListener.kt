package com.jalagama.popup.core

/**
 * Optional analytics / telemetry hooks. All callbacks are invoked on the main thread by [PopupManager].
 */
interface PopupAnalyticsListener {
    fun onShown(request: PopupRequest) {}

    fun onDismissed(request: PopupRequest, reason: DismissReason) {}

    fun onButtonClicked(request: PopupRequest, buttonId: String) {}

    fun onListItemClicked(request: PopupRequest, itemId: String) {}

    enum class DismissReason {
        USER_ACTION,
        PROGRAMMATIC,
        REPLACED,
        INTERRUPTED,
    }
}
