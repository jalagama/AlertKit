package com.jalagama.popup.core

/**
 * Optional list body (e.g. choices). Selection is reported via [id] through [PopupAnalyticsListener].
 */
data class PopupListItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
)
