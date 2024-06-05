package at.hannibal2.skyhanni.utils

import kotlin.time.Duration

class TimeLimitedSet<T : Any>(
    expireAfterWrite: Duration,
    private val removalListener: (T) -> Unit = {},
) {

    private val cache = TimeLimitedCache<T, Unit>(expireAfterWrite) { key, _ -> key?.let { removalListener(it) } }

    fun add(element: T) {
        cache[element] = Unit
    }

    fun addIfAbsent(element: T) {
        if (!contains(element)) add(element)
    }

    fun remove(element: T) = cache.remove(element)

    operator fun contains(element: T): Boolean = cache.containsKey(element)

    fun clear() = cache.clear()

    fun toSet(): Set<T> = cache.keys().toSet()
}
