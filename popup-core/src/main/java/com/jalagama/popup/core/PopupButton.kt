package com.jalagama.popup.core

/**
 * @param id Stable identifier for analytics and for correlating user actions.
 */
data class PopupButton(
    val id: String,
    val text: String,
    val style: Style = Style.DEFAULT,
) {
    enum class Style {
        DEFAULT,
        PRIMARY,
        DESTRUCTIVE,
    }
}
