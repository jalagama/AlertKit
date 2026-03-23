package com.jalagama.popup.compose

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import com.jalagama.popup.core.PopupButton
import com.jalagama.popup.core.PopupDisplayBridge
import com.jalagama.popup.core.PopupDisplayListener
import com.jalagama.popup.core.PopupRequest
import com.jalagama.popup.core.PopupUiType

/**
 * Renders popups with Jetpack Compose inside a lightweight [android.app.Dialog] window.
 */
class ComposePopupDisplayBridge : PopupDisplayBridge {

    private val lock = Any()
    private var host: Dialog? = null

    override fun show(context: Context, request: PopupRequest, listener: PopupDisplayListener): Boolean {
        val activity = context as? Activity ?: return false
        if (activity.isFinishing || activity.isDestroyed) return false

        dismissProgrammatically()

        val dialog = Dialog(activity)
        var userCancelled = false

        dialog.window?.apply {
            setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            if (request.uiType == PopupUiType.BOTTOM_SHEET) {
                setGravity(Gravity.BOTTOM)
            }
            setBackgroundDrawableResource(android.R.color.transparent)
        }

        val composeView = ComposeView(activity).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                val dark = (activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                    Configuration.UI_MODE_NIGHT_YES
                MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
                    when (request.uiType) {
                        PopupUiType.DIALOG -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.45f))
                                    .clickable {
                                        userCancelled = true
                                        dialog.dismiss()
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .padding(24.dp)
                                        .clickable(enabled = false) {},
                                    shape = MaterialTheme.shapes.large,
                                    tonalElevation = 6.dp,
                                ) {
                                    PopupBodyColumn(
                                        request = request,
                                        listener = listener,
                                        onFinish = { dialog.dismiss() },
                                        modifier = Modifier.padding(20.dp),
                                    )
                                }
                            }
                        }
                        PopupUiType.BOTTOM_SHEET -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.45f))
                                    .clickable {
                                        userCancelled = true
                                        dialog.dismiss()
                                    },
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .clickable(enabled = false) {},
                                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                                    tonalElevation = 8.dp,
                                ) {
                                    PopupBodyColumn(
                                        request = request,
                                        listener = listener,
                                        onFinish = { dialog.dismiss() },
                                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        dialog.setContentView(composeView)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setOnCancelListener { userCancelled = true }
        dialog.setOnDismissListener {
            synchronized(lock) {
                if (host === dialog) {
                    host = null
                }
            }
            listener.onDismissed(userCancelled)
        }

        synchronized(lock) {
            host = dialog
        }
        dialog.show()
        return true
    }

    override fun dismissProgrammatically() {
        val d = synchronized(lock) {
            val h = host
            host = null
            h
        }
        d?.dismiss()
    }

    override fun isShowing(): Boolean = synchronized(lock) { host?.isShowing == true }

    @Composable
    private fun PopupBodyColumn(
        request: PopupRequest,
        listener: PopupDisplayListener,
        onFinish: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        val buttons = remember(request.id, request.buttons) {
            if (request.buttons.isEmpty()) {
                listOf(PopupButton(id = "ok", text = "OK"))
            } else {
                request.buttons
            }
        }
        Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            request.title?.let { Text(it, style = MaterialTheme.typography.headlineSmall) }
            request.message?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            if (request.listItems.isNotEmpty()) {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    request.listItems.forEach { item ->
                        Text(
                            text = item.title + (item.subtitle?.let { "\n$it" } ?: ""),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    listener.onListItemClicked(item.id)
                                    onFinish()
                                }
                                .padding(vertical = 8.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            buttons.forEach { btn ->
                when (btn.style) {
                    PopupButton.Style.DEFAULT -> TextButton(
                        onClick = {
                            listener.onButtonClicked(btn.id)
                            onFinish()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(btn.text) }
                    PopupButton.Style.PRIMARY -> Button(
                        onClick = {
                            listener.onButtonClicked(btn.id)
                            onFinish()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(btn.text) }
                    PopupButton.Style.DESTRUCTIVE -> Button(
                        onClick = {
                            listener.onButtonClicked(btn.id)
                            onFinish()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        ),
                    ) { Text(btn.text) }
                }
            }
        }
    }
}
