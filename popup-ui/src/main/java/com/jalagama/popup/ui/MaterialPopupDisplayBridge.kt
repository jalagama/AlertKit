package com.jalagama.popup.ui

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.StyleRes
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jalagama.popup.core.PopupButton
import com.jalagama.popup.core.PopupDisplayBridge
import com.jalagama.popup.core.PopupDisplayListener
import com.jalagama.popup.core.PopupListItem
import com.jalagama.popup.core.PopupRequest
import com.jalagama.popup.core.PopupUiType
import kotlin.math.roundToInt

/**
 * [MaterialAlertDialog] and [BottomSheetDialog] backed host that keeps a single [Dialog] reference.
 */
class MaterialPopupDisplayBridge(
    @StyleRes private val dialogThemeOverlay: Int,
    @StyleRes private val bottomSheetThemeOverlay: Int,
) : PopupDisplayBridge {

    private val lock = Any()
    private var host: Dialog? = null

    override fun show(context: Context, request: PopupRequest, listener: PopupDisplayListener): Boolean {
        val activity = context as? Activity ?: return false
        if (activity.isFinishing || activity.isDestroyed) return false

        dismissProgrammatically()

        val themedCtx = ContextThemeWrapper(
            activity,
            if (request.uiType == PopupUiType.BOTTOM_SHEET) bottomSheetThemeOverlay else dialogThemeOverlay,
        )
        val inflater = LayoutInflater.from(themedCtx)
        val body = inflater.inflate(R.layout.popup_material_body, null, false) as LinearLayout

        lateinit var target: Dialog
        bindBody(body, request, listener) { target.dismiss() }

        var userCancelled = false
        target = when (request.uiType) {
            PopupUiType.DIALOG -> {
                MaterialAlertDialogBuilder(themedCtx)
                    .setView(body)
                    .create()
                    .also { d ->
                        d.setCanceledOnTouchOutside(true)
                        d.setOnCancelListener { userCancelled = true }
                    }
            }
            PopupUiType.BOTTOM_SHEET -> {
                BottomSheetDialog(themedCtx).apply {
                    setContentView(body)
                    behavior.peekHeight = (activity.resources.displayMetrics.heightPixels * 0.55f).roundToInt()
                    setCanceledOnTouchOutside(true)
                    setOnCancelListener { userCancelled = true }
                }
            }
        }

        target.setOnDismissListener {
            synchronized(lock) {
                if (host === target) {
                    host = null
                }
            }
            listener.onDismissed(userCancelled)
        }

        synchronized(lock) {
            host = target
        }
        target.show()
        return true
    }

    override fun dismissProgrammatically() {
        val d = synchronized(lock) {
            val h = host
            host = null
            h
        }
        // Do not clear OnDismissListener: PopupManager must receive onDismissed for preemption/replace.
        d?.dismiss()
    }

    override fun isShowing(): Boolean {
        synchronized(lock) {
            return host?.isShowing == true
        }
    }

    private fun bindBody(
        root: LinearLayout,
        request: PopupRequest,
        listener: PopupDisplayListener,
        dismiss: () -> Unit,
    ) {
        val title = root.findViewById<TextView>(R.id.popupTitle)
        val message = root.findViewById<TextView>(R.id.popupMessage)
        val listScroll = root.findViewById<ScrollView>(R.id.popupListScroll)
        val listContainer = root.findViewById<LinearLayout>(R.id.popupListContainer)
        val buttons = root.findViewById<LinearLayout>(R.id.popupButtons)

        if (!request.title.isNullOrBlank()) {
            title.visibility = View.VISIBLE
            title.text = request.title
        }
        if (!request.message.isNullOrBlank()) {
            message.visibility = View.VISIBLE
            message.text = request.message
        }

        if (request.listItems.isNotEmpty()) {
            listScroll.visibility = View.VISIBLE
            request.listItems.forEach { item ->
                listContainer.addView(createListRow(root.context, item, listener, dismiss))
            }
        }

        val effectiveButtons = if (request.buttons.isEmpty()) {
            listOf(PopupButton(id = "ok", text = root.context.getString(android.R.string.ok)))
        } else {
            request.buttons
        }

        effectiveButtons.forEachIndexed { index, btn ->
            val mb = MaterialButton(root.context, null, com.google.android.material.R.attr.materialButtonOutlinedStyle)
            mb.text = btn.text
            mb.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                topMargin = if (index == 0) 0 else (6 * root.resources.displayMetrics.density).toInt()
            }
            applyButtonStyle(mb, btn.style)
            mb.setOnClickListener {
                listener.onButtonClicked(btn.id)
                dismiss()
            }
            buttons.addView(mb)
        }
    }

    private fun createListRow(
        context: Context,
        item: PopupListItem,
        listener: PopupDisplayListener,
        dismiss: () -> Unit,
    ): View {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (12 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad, pad, pad)
            isClickable = true
            isFocusable = true
            val ta = context.obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground))
            val bg = ta.getResourceId(0, 0)
            ta.recycle()
            setBackgroundResource(bg)
        }
        val title = TextView(context).apply {
            text = item.title
            textSize = 16f
        }
        container.addView(title)
        if (!item.subtitle.isNullOrBlank()) {
            val sub = TextView(context).apply {
                text = item.subtitle
                textSize = 14f
                setTextColor(0xFF666666.toInt())
            }
            container.addView(sub)
        }
        container.setOnClickListener {
            listener.onListItemClicked(item.id)
            dismiss()
        }
        return container
    }

    private fun applyButtonStyle(button: MaterialButton, style: PopupButton.Style) {
        when (style) {
            PopupButton.Style.DEFAULT -> Unit
            PopupButton.Style.PRIMARY -> {
                val bg = com.google.android.material.color.MaterialColors.getColor(
                    button,
                    com.google.android.material.R.attr.colorOnSecondary,
                )
                val fg = com.google.android.material.color.MaterialColors.getColor(
                    button,
                    com.google.android.material.R.attr.colorOnPrimary,
                )
                button.backgroundTintList = ColorStateList.valueOf(bg)
                button.setTextColor(fg)
            }
            PopupButton.Style.DESTRUCTIVE -> {
                val bg = com.google.android.material.color.MaterialColors.getColor(
                    button,
                    com.google.android.material.R.attr.colorErrorContainer,
                )
                val fg = com.google.android.material.color.MaterialColors.getColor(
                    button,
                    com.google.android.material.R.attr.colorOnErrorContainer,
                )
                button.backgroundTintList = ColorStateList.valueOf(bg)
                button.setTextColor(fg)
            }
        }
    }
}
