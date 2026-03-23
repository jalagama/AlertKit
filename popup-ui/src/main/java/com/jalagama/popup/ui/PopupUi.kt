package com.jalagama.popup.ui

import android.app.Application
import androidx.annotation.StyleRes
import com.jalagama.popup.core.FeedbackMapping
import com.jalagama.popup.core.PopupAnalyticsListener
import com.jalagama.popup.core.PopupLogger
import com.jalagama.popup.core.PopupManager

/**
 * One-call wiring for the View-system stack: activity tracking, foreground detection, feedback,
 * and [MaterialPopupDisplayBridge].
 */
object PopupUi {
    /** Default Material 3 bridge using library theme overlays. */
    fun materialDisplayBridge(): MaterialPopupDisplayBridge = MaterialPopupDisplayBridge(
        dialogThemeOverlay = R.style.ThemeOverlay_PopupUi_MaterialAlert,
        bottomSheetThemeOverlay = R.style.ThemeOverlay_PopupUi_BottomSheet,
    )

    fun install(
        application: Application,
        @StyleRes materialThemeOverlay: Int = R.style.ThemeOverlay_PopupUi_MaterialAlert,
        @StyleRes bottomSheetThemeOverlay: Int = R.style.ThemeOverlay_PopupUi_BottomSheet,
        mapping: FeedbackMapping = FeedbackMapping(),
        analytics: PopupAnalyticsListener? = null,
        logger: PopupLogger = com.jalagama.popup.core.NoOpPopupLogger,
    ): PopupManager {
        val activityProvider = DefaultActivityProvider().also { it.register(application) }
        val foreground = ProcessForegroundStateProvider()
        val bridge = MaterialPopupDisplayBridge(
            dialogThemeOverlay = materialThemeOverlay,
            bottomSheetThemeOverlay = bottomSheetThemeOverlay,
        )
        return PopupManager.Builder(activityProvider, foreground)
            .mainThreadExecutor(AndroidMainThreadExecutor())
            .feedbackController(DefaultFeedbackController(application, mapping))
            .analyticsListener(analytics)
            .logger(logger)
            .displayBridge(bridge)
            .build()
    }
}
