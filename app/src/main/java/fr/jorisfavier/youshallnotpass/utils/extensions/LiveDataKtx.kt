package fr.jorisfavier.youshallnotpass.utils.extensions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

fun <T> LiveData<T>.default(
    defaultValue: T
): LiveData<T> {
    val result = MediatorLiveData<T>()
    result.value = defaultValue!!
    result.addSource(this) { result.value = it }
    return result
}

/**
 * Combine two data streams by emitting a new [Pair] whenever one of the sources emits
 * a new event. Note that the first [Pair] is emitted after both streams have emitted at least one non-null value.
 */
fun <A, B> LiveData<A>.combine(other: LiveData<B>): LiveData<Pair<A, B>> {
    return MediatorLiveData<Pair<A, B>>().apply {
        fun update() {
            val valueA = this@combine.value
            val valueB = other.value
            if (valueA != null && valueB != null) {
                value = valueA to valueB
            }
        }

        addSource(this@combine) {
            update()
        }

        addSource(other) {
            update()
        }

        // trigger initial update in case both LiveDatas already have a value
        update()
    }
}