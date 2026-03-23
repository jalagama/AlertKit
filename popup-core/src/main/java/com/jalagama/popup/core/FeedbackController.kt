package com.jalagama.popup.core

/**
 * Plays optional sound / vibration when a popup becomes visible.
 * No-op implementations are valid for tests or headless pipelines.
 */
fun interface FeedbackController {
    fun onPopupShown(request: PopupRequest)
}

object NoOpFeedbackController : FeedbackController {
    override fun onPopupShown(request: PopupRequest) {}
}
