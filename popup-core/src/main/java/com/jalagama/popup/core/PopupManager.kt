package com.jalagama.popup.core

import com.jalagama.popup.core.internal.PopupQueueStore
import java.util.ArrayDeque

/**
 * Coordinates priority queueing, preemption, deduplication, foreground gating, and a pluggable
 * [PopupDisplayBridge]. All public methods are thread-safe.
 *
 * **Integration:** construct via [Builder], attach a [PopupDisplayBridge] on the main thread, and
 * register [ActivityProvider] + [ForegroundStateProvider] from your `Application` (see `popup-ui`).
 */
class PopupManager private constructor(
    private val activityProvider: ActivityProvider,
    private val foregroundStateProvider: ForegroundStateProvider,
    private val feedbackController: FeedbackController,
    private val mainThreadExecutor: MainThreadExecutor,
    private val analyticsListener: PopupAnalyticsListener?,
    private val logger: PopupLogger,
    @Volatile private var displayBridge: PopupDisplayBridge?,
) {
    private val lock = Any()
    private val queueStore = PopupQueueStore()
    private val preemptedStack = ArrayDeque<PopupRequest>()

    @Volatile
    private var currentRequest: PopupRequest? = null

    /** Next request to show without dequeuing (critical preemption or replace-current). */
    private var pendingShow: PopupRequest? = null

    private var programmaticDismissReason: PopupAnalyticsListener.DismissReason =
        PopupAnalyticsListener.DismissReason.PROGRAMMATIC

    private val displayListener = object : PopupDisplayListener {
        override fun onButtonClicked(buttonId: String) {
            val req = synchronized(lock) { currentRequest }
            if (req != null) {
                analyticsListener?.onButtonClicked(req, buttonId)
            }
        }

        override fun onListItemClicked(itemId: String) {
            val req = synchronized(lock) { currentRequest }
            if (req != null) {
                analyticsListener?.onListItemClicked(req, itemId)
            }
        }

        override fun onDismissed(userInitiated: Boolean) {
            mainThreadExecutor.execute { handleDismiss(userInitiated) }
        }
    }

    /**
     * Hot-swaps the UI implementation (e.g. View vs Compose) or attaches it after process start.
     */
    fun setDisplayBridge(bridge: PopupDisplayBridge?) {
        mainThreadExecutor.execute {
            synchronized(lock) {
                displayBridge = bridge
            }
            tryShowNext()
        }
    }

    fun enqueue(request: PopupRequest): EnqueueResult {
        val after = AfterEnqueue()
        val result = synchronized(lock) {
            enqueueUnderLock(request, after)
        }
        mainThreadExecutor.execute {
            if (after.dismissCurrent) {
                displayBridge?.dismissProgrammatically()
            }
            tryShowNext()
        }
        return result
    }

    /** Clears waiting items; does not dismiss the visible popup. Intended for tests / sign-out flows. */
    fun clearQueue() {
        synchronized(lock) {
            queueStore.clear()
            preemptedStack.clear()
        }
    }

    private data class AfterEnqueue(
        var dismissCurrent: Boolean = false,
    )

    private fun enqueueUnderLock(request: PopupRequest, after: AfterEnqueue): EnqueueResult {
        val cur = currentRequest
        if (cur != null && cur.id == request.id) {
            when (request.deduplicationMode) {
                PopupDeduplicationMode.IGNORE_DUPLICATE -> return EnqueueResult.DuplicateIgnored
                PopupDeduplicationMode.REPLACE -> {
                    pendingShow = request
                    programmaticDismissReason = PopupAnalyticsListener.DismissReason.REPLACED
                    after.dismissCurrent = true
                    return EnqueueResult.ReplacedCurrent
                }
            }
        }
        if (cur != null && request.preemptsOver(cur)) {
            pendingShow = request
            preemptedStack.addLast(cur)
            programmaticDismissReason = PopupAnalyticsListener.DismissReason.INTERRUPTED
            after.dismissCurrent = true
            return EnqueueResult.Accepted
        }
        return queueStore.enqueue(request)
    }

    private fun handleDismiss(userInitiated: Boolean) {
        val (dismissed, reason) = synchronized(lock) {
            val req = currentRequest
            currentRequest = null
            val reason = when {
                userInitiated -> PopupAnalyticsListener.DismissReason.USER_ACTION
                else -> programmaticDismissReason
            }
            programmaticDismissReason = PopupAnalyticsListener.DismissReason.PROGRAMMATIC
            Pair(req, reason)
        }
        if (dismissed != null) {
            analyticsListener?.onDismissed(dismissed, reason)
        }
        mainThreadExecutor.execute { tryShowNext() }
    }

    private fun tryShowNext() {
        val snapshot = synchronized(lock) {
            if (currentRequest != null) {
                return@synchronized null
            }
            if (!foregroundStateProvider.isAppInForeground()) {
                return@synchronized null
            }
            val activity = activityProvider.currentActivity()
            val bridge = displayBridge
            if (activity == null || bridge == null) {
                return@synchronized null
            }
            val next = pendingShow
                ?: preemptedStack.pollLast()
                ?: queueStore.poll()
                ?: return@synchronized null
            pendingShow = null
            Triple(activity, bridge, next)
        } ?: return

        val (activity, bridge, next) = snapshot
        try {
            val shown = bridge.show(activity, next, displayListener)
            if (shown) {
                synchronized(lock) {
                    currentRequest = next
                }
                feedbackController.onPopupShown(next)
                analyticsListener?.onShown(next)
            } else {
                synchronized(lock) {
                    preemptedStack.addLast(next)
                }
                logger.w(TAG, "PopupDisplayBridge.show returned false; re-queued id=${next.id}")
                mainThreadExecutor.execute { tryShowNext() }
            }
        } catch (e: Exception) {
            synchronized(lock) {
                preemptedStack.addLast(next)
            }
            logger.w(TAG, "PopupDisplayBridge.show threw; re-queued id=${next.id} — ${e.message}")
            mainThreadExecutor.execute { tryShowNext() }
        }
    }

    class Builder(
        private val activityProvider: ActivityProvider,
        private val foregroundStateProvider: ForegroundStateProvider,
    ) {
        private var feedback: FeedbackController = NoOpFeedbackController
        private var mainThread: MainThreadExecutor? = null
        private var analytics: PopupAnalyticsListener? = null
        private var logger: PopupLogger = NoOpPopupLogger
        private var bridge: PopupDisplayBridge? = null

        fun feedbackController(controller: FeedbackController) = apply { feedback = controller }

        fun mainThreadExecutor(executor: MainThreadExecutor) = apply { mainThread = executor }

        fun analyticsListener(listener: PopupAnalyticsListener?) = apply { analytics = listener }

        fun logger(logger: PopupLogger) = apply { this.logger = logger }

        fun displayBridge(bridge: PopupDisplayBridge?) = apply { this.bridge = bridge }

        fun build(): PopupManager {
            val exec = mainThread ?: error("Call mainThreadExecutor(...) before build()")
            return PopupManager(
                activityProvider = activityProvider,
                foregroundStateProvider = foregroundStateProvider,
                feedbackController = feedback,
                mainThreadExecutor = exec,
                analyticsListener = analytics,
                logger = logger,
                displayBridge = bridge,
            )
        }
    }

    companion object {
        private const val TAG = "PopupManager"
    }
}
