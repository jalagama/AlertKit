package com.jalagama.popup.core

/** Outcome of [PopupManager.enqueue]. */
enum class EnqueueResult {
    /** Accepted into the queue or shown immediately. */
    Accepted,

    /** Same [PopupRequest.id] already present (queued or handled under current policy). */
    DuplicateIgnored,

    /** Visible popup with same id was replaced; dismiss animation may still be in flight. */
    ReplacedCurrent,
}
