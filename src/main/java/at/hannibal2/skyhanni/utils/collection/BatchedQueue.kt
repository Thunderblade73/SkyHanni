package at.hannibal2.skyhanni.utils.collection

import java.util.LinkedList
import java.util.Queue

class BatchedQueue<T>() : LinkedList<T>(), Queue<T> {

    private val batchSizes = ArrayList<Int>().apply { add(0) }
    private val insertBatch get() = batchSizes.size - 1

    constructor(emptyBatches: Int) : this() {
        for (i in 1 until emptyBatches) {
            batchSizes.add(0)
        }
    }

    fun endBatch() {
        batchSizes.add(0)
    }

    fun drainBatch(): Sequence<T> = sequence<T> {
        while(batchSizes[0] != 0) {
            batchSizes[0]--
            this.yield(poll())
        }
        batchSizes.removeAt(0)
    }

    fun clearBatch(){
        clear()
        batchSizes.clear()
    }

    override fun add(element: T): Boolean {
        batchSizes[insertBatch]++
        return super.add(element)
    }

    override fun addAll(elements: Collection<T>): Boolean {
        batchSizes[insertBatch] += elements.size
        return super.addAll(elements)
    }

    override fun clear() {
        super.clear()
        batchSizes.replaceAll { 0 }
    }
}
