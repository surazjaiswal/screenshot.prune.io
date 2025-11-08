package io.prune.screenshot.data.cache

import io.prune.screenshot.domain.model.Screenshot
import java.util.concurrent.atomic.AtomicReference

class InMemoryScreenshotCache {

    private val cache = AtomicReference<List<Screenshot>>(emptyList())

    fun get(): List<Screenshot>? = cache.get().takeIf { it.isNotEmpty() }

    fun set(items: List<Screenshot>) {
        cache.set(items)
    }

    fun clear() {
        cache.set(emptyList())
    }
}

