package com.jalagama.popup.core

/**
 * Popup urgency. Higher [weight] is shown first in the queue.
 * [CRITICAL] may preempt a currently visible non-critical popup when enabled on the visible request.
 */
enum class PopupPriority(internal val weight: Int) {
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    CRITICAL(4),
}
