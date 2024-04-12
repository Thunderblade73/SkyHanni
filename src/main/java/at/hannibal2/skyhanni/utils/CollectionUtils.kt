package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.utils.renderables.Renderable
import java.util.Collections
import java.util.WeakHashMap
import java.util.concurrent.ConcurrentLinkedQueue

object CollectionUtils {

    fun <E> ConcurrentLinkedQueue<E>.drainTo(list: MutableCollection<E>) {
        while (true)
            list.add(this.poll() ?: break)
    }

    // Let garbage collector handle the removal of entries in this list
    fun <T> weakReferenceList(): MutableSet<T> = Collections.newSetFromMap(WeakHashMap<T, Boolean>())

    fun <T> MutableCollection<T>.filterToMutable(predicate: (T) -> Boolean) = filterTo(mutableListOf(), predicate)

    fun <T> List<T>.indexOfFirst(vararg args: T) = args.map { indexOf(it) }.firstOrNull { it != -1 }

    infix fun <K, V> MutableMap<K, V>.put(pairs: Pair<K, V>) {
        this[pairs.first] = pairs.second
    }

    // Taken and modified from Skytils
    @JvmStatic
    fun <T> T.equalsOneOf(vararg other: T): Boolean {
        for (obj in other) {
            if (this == obj) return true
        }
        return false
    }

    fun <E> List<E>.getOrNull(index: Int): E? {
        return if (index in indices) {
            get(index)
        } else null
    }

    fun <T : Any> T?.toSingletonListOrEmpty(): List<T> {
        if (this == null) return emptyList()
        return listOf(this)
    }

    fun <K> MutableMap<K, Int>.addOrPut(key: K, number: Int): Int =
        this.merge(key, number, Int::plus)!! // Never returns null since "plus" can't return null

    fun <K> MutableMap<K, Long>.addOrPut(key: K, number: Long): Long =
        this.merge(key, number, Long::plus)!! // Never returns null since "plus" can't return null

    fun <K> MutableMap<K, Double>.addOrPut(key: K, number: Double): Double =
        this.merge(key, number, Double::plus)!! // Never returns null since "plus" can't return null

    fun <K> MutableMap<K, Float>.addOrPut(key: K, number: Float): Float =
        this.merge(key, number, Float::plus)!! // Never returns null since "plus" can't return null

    fun <K, N : Number> Map<K, N>.sumAllValues(): Double {
        if (values.isEmpty()) return 0.0

        return when (values.first()) {
            is Double -> values.sumOf { it.toDouble() }
            is Float -> values.sumOf { it.toDouble() }
            is Long -> values.sumOf { it.toLong() }.toDouble()
            else -> values.sumOf { it.toInt() }.toDouble()
        }
    }

    fun List<String>.nextAfter(after: String, skip: Int = 1) = nextAfter({ it == after }, skip)

    fun List<String>.nextAfter(after: (String) -> Boolean, skip: Int = 1): String? {
        var missing = -1
        for (line in this) {
            if (after(line)) {
                missing = skip - 1
                continue
            }
            if (missing == 0) {
                return line
            }
            if (missing != -1) {
                missing--
            }
        }
        return null
    }

    fun List<String>.removeNextAfter(after: String, skip: Int = 1) = removeNextAfter({ it == after }, skip)

    fun List<String>.removeNextAfter(after: (String) -> Boolean, skip: Int = 1): List<String> {
        val newList = mutableListOf<String>()
        var missing = -1
        for (line in this) {
            if (after(line)) {
                missing = skip - 1
                continue
            }
            if (missing == 0) {
                missing--
                continue
            }
            if (missing != -1) {
                missing--
            }
            newList.add(line)
        }
        return newList
    }

    fun List<String>.addIfNotNull(element: String?) = element?.let { plus(it) } ?: this

    fun <K, V> Map<K, V>.editCopy(function: MutableMap<K, V>.() -> Unit) =
        toMutableMap().also { function(it) }.toMap()

    fun <T> List<T>.editCopy(function: MutableList<T>.() -> Unit) =
        toMutableList().also { function(it) }.toList()

    fun <K, V> Map<K, V>.moveEntryToTop(matcher: (Map.Entry<K, V>) -> Boolean): Map<K, V> {
        val entry = entries.find(matcher)
        if (entry != null) {
            val newMap = linkedMapOf(entry.key to entry.value)
            newMap.putAll(this)
            return newMap
        }
        return this
    }

    fun <E> MutableList<List<E>>.addAsSingletonList(text: E) {
        add(Collections.singletonList(text))
    }

    fun <K, V : Comparable<V>> List<Pair<K, V>>.sorted(): List<Pair<K, V>> {
        return sortedBy { (_, value) -> value }
    }

    fun <K, V : Comparable<V>> Map<K, V>.sorted(): Map<K, V> {
        return toList().sorted().toMap()
    }

    fun <K, V : Comparable<V>> Map<K, V>.sortedDesc(): Map<K, V> {
        return toList().sorted().reversed().toMap()
    }

    inline fun <reified T> ConcurrentLinkedQueue<T>.drainForEach(action: (T) -> Unit) {
        while (true) {
            val value = this.poll() ?: break
            action(value)
        }
    }

    fun <T> Sequence<T>.takeWhileInclusive(predicate: (T) -> Boolean) = sequence {
        with(iterator()) {
            while (hasNext()) {
                val next = next()
                yield(next)
                if (!predicate(next)) break
            }
        }
    }

    /** Updates a value if it is present in the set (equals), useful if the newValue is not reference equal with the value in the set */
    inline fun <reified T> MutableSet<T>.refreshReference(newValue: T) = if (this.contains(newValue)) {
        this.remove(newValue)
        this.add(newValue)
        true
    } else false

    @Suppress("UNCHECKED_CAST")
    fun <T> Iterable<T?>.takeIfAllNotNull(): Iterable<T>? =
        takeIf { null !in this } as? Iterable<T>

    @Suppress("UNCHECKED_CAST")
    fun <T> List<T?>.takeIfAllNotNull(): List<T>? =
        takeIf { null !in this } as? List<T>

    fun Collection<Collection<Renderable>>.tableStretchXPadding(xSpace: Int): Int {
        if (this.isEmpty()) return xSpace
        val xWidth = this.maxOf { it.sumOf { it.width } }
        val xLength = this.maxOf { it.size }
        val emptySpace = xSpace - xWidth
        if (emptySpace < 0) {
            //    throw IllegalArgumentException("Not enough space for content")
        }
        return emptySpace / (xLength - 1)
    }

    fun Collection<Collection<Renderable>>.tableStretchYPadding(ySpace: Int): Int {
        if (this.isEmpty()) return ySpace
        val yWidth = this.sumOf { it.maxOfOrNull { it.height } ?: 0 }
        val yLength = this.size
        val emptySpace = ySpace - yWidth
        if (emptySpace < 0) {
            //    throw IllegalArgumentException("Not enough space for content")
        }
        return emptySpace / (yLength - 1)
    }
}
