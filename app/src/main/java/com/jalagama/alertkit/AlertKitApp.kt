package com.jalagama.alertkit

import android.app.Application
import android.util.Log
import com.jalagama.popup.core.DebugPopupLogger
import com.jalagama.popup.core.PopupAnalyticsListener
import com.jalagama.popup.core.PopupManager
import com.jalagama.popup.core.PopupRequest
import com.jalagama.popup.ui.PopupUi

class AlertKitApp : Application() {

    lateinit var popupManager: PopupManager
        private set

    override fun onCreate() {
        super.onCreate()
        popupManager = PopupUi.install(
            application = this,
            analytics = object : PopupAnalyticsListener {
                override fun onShown(request: PopupRequest) {
                    Log.d(TAG, "onShown ${request.id}")
                }

                override fun onDismissed(request: PopupRequest, reason: PopupAnalyticsListener.DismissReason) {
                    Log.d(TAG, "onDismissed ${request.id} reason=$reason")
                }

                override fun onButtonClicked(request: PopupRequest, buttonId: String) {
                    Log.d(TAG, "onButtonClicked ${request.id} button=$buttonId")
                }
            },
            logger = DebugPopupLogger(),
        )
    }

    companion object {
        private const val TAG = "AlertKitDemo"
    }
}
