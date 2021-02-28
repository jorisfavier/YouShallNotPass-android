package fr.jorisfavier.youshallnotpass.utils.extensions

import fr.jorisfavier.youshallnotpass.model.exception.YsnpException

inline fun <T> Result<T>.onYsnpFailure(action: (exception: YsnpException) -> Unit): Result<T> {
    exceptionOrNull()?.let {
        if (it is YsnpException) action(it)
    }
    return this
}

inline fun <T> Result<T>.onUnknownFailure(action: (exception: Throwable) -> Unit): Result<T> {
    exceptionOrNull()?.let {
        if (it !is YsnpException) action(it)
    }
    return this
}