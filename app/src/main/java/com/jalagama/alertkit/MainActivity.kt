package com.jalagama.alertkit

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.jalagama.popup.compose.ComposePopupDisplayBridge
import com.jalagama.popup.core.PopupButton
import com.jalagama.popup.core.PopupDeduplicationMode
import com.jalagama.popup.core.PopupListItem
import com.jalagama.popup.core.PopupPriority
import com.jalagama.popup.core.PopupRequest
import com.jalagama.popup.core.PopupUiType
import com.jalagama.popup.ui.PopupUi

class MainActivity : AppCompatActivity() {

    private val popupManager get() = (application as AlertKitApp).popupManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnLow).setOnClickListener {
            popupManager.enqueue(
                PopupRequest(
                    id = "demo-low-${System.currentTimeMillis()}",
                    priority = PopupPriority.LOW,
                    title = "Low priority",
                    message = "Queued behind higher tiers.",
                    buttons = listOf(PopupButton(id = "ok", text = "OK")),
                ),
            )
        }

        findViewById<Button>(R.id.btnHigh).setOnClickListener {
            popupManager.enqueue(
                PopupRequest(
                    id = "demo-high-${System.currentTimeMillis()}",
                    priority = PopupPriority.HIGH,
                    title = "High priority",
                    message = "Should jump ahead of LOW/MEDIUM when waiting.",
                    buttons = listOf(
                        PopupButton(id = "ok", text = "Continue", style = PopupButton.Style.PRIMARY),
                    ),
                    soundEnabled = true,
                    vibrationEnabled = true,
                ),
            )
        }

        findViewById<Button>(R.id.btnList).setOnClickListener {
            popupManager.enqueue(
                PopupRequest(
                    id = "demo-list",
                    priority = PopupPriority.MEDIUM,
                    title = "Pick an option",
                    message = "List taps dismiss with analytics.",
                    listItems = listOf(
                        PopupListItem(id = "a", title = "Option A", subtitle = "First"),
                        PopupListItem(id = "b", title = "Option B", subtitle = "Second"),
                    ),
                    buttons = listOf(PopupButton(id = "cancel", text = "Cancel")),
                    deduplicationMode = PopupDeduplicationMode.REPLACE,
                ),
            )
        }

        findViewById<Button>(R.id.btnSheet).setOnClickListener {
            popupManager.enqueue(
                PopupRequest(
                    id = "demo-sheet-${System.currentTimeMillis()}",
                    priority = PopupPriority.MEDIUM,
                    title = "Bottom sheet",
                    message = "Material bottom sheet host.",
                    uiType = PopupUiType.BOTTOM_SHEET,
                    buttons = listOf(PopupButton(id = "close", text = "Close")),
                ),
            )
        }

        findViewById<Button>(R.id.btnCritical).setOnClickListener {
            popupManager.enqueue(
                PopupRequest(
                    id = "demo-critical-${System.currentTimeMillis()}",
                    priority = PopupPriority.CRITICAL,
                    title = "Critical",
                    message = "Interrupts non-critical popups.",
                    buttons = listOf(PopupButton(id = "ack", text = "Acknowledge", style = PopupButton.Style.DESTRUCTIVE)),
                    soundEnabled = true,
                    vibrationEnabled = true,
                ),
            )
        }

        findViewById<Button>(R.id.btnUseCompose).setOnClickListener {
            popupManager.setDisplayBridge(ComposePopupDisplayBridge())
        }

        findViewById<Button>(R.id.btnUseMaterial).setOnClickListener {
            popupManager.setDisplayBridge(PopupUi.materialDisplayBridge())
        }
    }
}
