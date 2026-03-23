package com.jalagama.popup.core.internal

import com.jalagama.popup.core.EnqueueResult
import com.jalagama.popup.core.PopupDeduplicationMode
import com.jalagama.popup.core.PopupPriority
import com.jalagama.popup.core.PopupRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PopupQueueStoreTest {

    @Test
    fun `higher priority dequeued before lower when enqueued first-lower then-higher`() {
        val store = PopupQueueStore()
        store.enqueue(req("a", PopupPriority.LOW))
        store.enqueue(req("b", PopupPriority.HIGH))
        assertEquals("b", store.poll()?.id)
        assertEquals("a", store.poll()?.id)
        assertNull(store.poll())
    }

    @Test
    fun `FIFO within same priority`() {
        val store = PopupQueueStore()
        store.enqueue(req("1", PopupPriority.MEDIUM))
        store.enqueue(req("2", PopupPriority.MEDIUM))
        store.enqueue(req("3", PopupPriority.MEDIUM))
        assertEquals("1", store.poll()?.id)
        assertEquals("2", store.poll()?.id)
        assertEquals("3", store.poll()?.id)
    }

    @Test
    fun `duplicate id ignored`() {
        val store = PopupQueueStore()
        val first = req("x", PopupPriority.HIGH, dedup = PopupDeduplicationMode.IGNORE_DUPLICATE)
        val second = first.copy(priority = PopupPriority.CRITICAL)
        assertEquals(EnqueueResult.Accepted, store.enqueue(first))
        assertEquals(EnqueueResult.DuplicateIgnored, store.enqueue(second))
        val polled = store.poll()
        assertEquals("x", polled?.id)
        assertEquals(PopupPriority.HIGH, polled?.priority)
        assertNull(store.poll())
    }

    @Test
    fun `duplicate id replaced`() {
        val store = PopupQueueStore()
        val first = req("x", PopupPriority.LOW, dedup = PopupDeduplicationMode.REPLACE)
        val second = req("x", PopupPriority.CRITICAL, dedup = PopupDeduplicationMode.REPLACE)
        assertEquals(EnqueueResult.Accepted, store.enqueue(first))
        assertEquals(EnqueueResult.Accepted, store.enqueue(second))
        val polled = store.poll()
        assertEquals("x", polled?.id)
        assertEquals(PopupPriority.CRITICAL, polled?.priority)
        assertNull(store.poll())
    }

    @Test
    fun `containsQueuedId tracks membership`() {
        val store = PopupQueueStore()
        store.enqueue(req("a", PopupPriority.MEDIUM))
        assertTrue(store.containsQueuedId("a"))
        store.poll()
        assertTrue(!store.containsQueuedId("a"))
    }

    private fun req(
        id: String,
        priority: PopupPriority,
        dedup: PopupDeduplicationMode = PopupDeduplicationMode.IGNORE_DUPLICATE,
    ): PopupRequest = PopupRequest(id = id, priority = priority, deduplicationMode = dedup)
}
