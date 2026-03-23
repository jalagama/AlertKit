package com.jalagama.popup.core

/**
 * How to treat a second [PopupRequest] with the same [PopupRequest.id] as an already queued
 * (or, for [REPLACE], currently visible) popup.
 */
enum class PopupDeduplicationMode {
    /** Drop the new request. */
    IGNORE_DUPLICATE,

    /** Remove the queued entry (or dismiss the visible popup) and apply the new payload. */
    REPLACE,
}
