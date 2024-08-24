package fr.jorisfavier.youshallnotpass.utils.extensions

import fr.jorisfavier.youshallnotpass.model.exception.YsnpException
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException

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

/**
 * Attempts [block], returning a successful [Result] if it succeeds, otherwise a [Result.Failure]
 * taking care not to break structured concurrency
 */
suspend fun <T> suspendRunCatching(
    errorMessage: String = "Failed to evaluate a suspendRunCatchingBlock.",
    block: suspend () -> T,
): Result<T> = try {
    Result.success(block())
} catch (cancellationException: CancellationException) {
    throw cancellationException
} catch (exception: Exception) {
    Timber.w(exception)
    Result.failure(exception)
}