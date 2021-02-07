package fr.jorisfavier.youshallnotpass.utils

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