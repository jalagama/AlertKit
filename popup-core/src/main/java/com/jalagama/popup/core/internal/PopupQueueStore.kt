package com.jalagama.popup.core.internal

import com.jalagama.popup.core.EnqueueResult
import com.jalagama.popup.core.PopupDeduplicationMode
import com.jalagama.popup.core.PopupRequest
import java.util.PriorityQueue
import java.util.concurrent.atomic.AtomicLong

/**
 * Thread-safe priority FIFO-per-tier queue with optional replace-by-id.
 * All methods must be called under [com.jalagama.popup.core.PopupManager]'s coordinator lock.
 */
internal class PopupQueueStore {
    private val sequence = AtomicLong()
    private val queue = PriorityQueue(QueuedEntry.COMPARATOR)
    private val byId = HashMap<String, QueuedEntry>()

    fun enqueue(request: PopupRequest): EnqueueResult {
        val existing = byId[request.id]
        if (existing != null) {
            when (request.deduplicationMode) {
                PopupDeduplicationMode.IGNORE_DUPLICATE -> return EnqueueResult.DuplicateIgnored
                PopupDeduplicationMode.REPLACE -> {
                    queue.remove(existing)
                    byId.remove(request.id)
                }
            }
        }
        val entry = QueuedEntry(sequence.incrementAndGet(), request)
        byId[request.id] = entry
        queue.add(entry)
        return EnqueueResult.Accepted
    }

    fun poll(): PopupRequest? {
        val entry = queue.poll() ?: return null
        byId.remove(entry.request.id)
        return entry.request
    }

    fun peek(): PopupRequest? = queue.peek()?.request

    fun containsQueuedId(id: String): Boolean = byId.containsKey(id)

    fun clear() {
        queue.clear()
        byId.clear()
        sequence.set(0L)
    }

    private data class QueuedEntry(
        val sequence: Long,
        val request: PopupRequest,
    ) {
        companion object {
            val COMPARATOR = Comparator<QueuedEntry> { a, b ->
                val pw = b.request.priority.weight.compareTo(a.request.priority.weight)
                if (pw != 0) pw else a.sequence.compareTo(b.sequence)
            }
        }
    }
}
