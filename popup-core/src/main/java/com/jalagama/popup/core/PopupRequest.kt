package com.jalagama.popup.core

/**
 * Immutable description of work for the queue + UI layer.
 *
 * @param soundEnabled When null, [FeedbackController] uses priority-based defaults.
 * @param vibrationEnabled When null, [FeedbackController] uses priority-based defaults.
 * @param allowsCriticalPreemption When false, an incoming [PopupPriority.CRITICAL] request will wait
 * instead of interrupting this popup while it is visible.
 */
data class PopupRequest(
    val id: String,
    val priority: PopupPriority = PopupPriority.MEDIUM,
    val title: String? = null,
    val message: String? = null,
    val buttons: List<PopupButton> = emptyList(),
    val listItems: List<PopupListItem> = emptyList(),
    val uiType: PopupUiType = PopupUiType.DIALOG,
    val soundEnabled: Boolean? = null,
    val vibrationEnabled: Boolean? = null,
    val deduplicationMode: PopupDeduplicationMode = PopupDeduplicationMode.IGNORE_DUPLICATE,
    val allowsCriticalPreemption: Boolean = true,
) {
    internal fun preemptsOver(visible: PopupRequest): Boolean {
        return priority == PopupPriority.CRITICAL &&
            visible.priority != PopupPriority.CRITICAL &&
            visible.allowsCriticalPreemption
    }
}
