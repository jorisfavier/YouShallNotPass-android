package fr.jorisfavier.youshallnotpass.utils.extensions

import fr.jorisfavier.youshallnotpass.model.exception.YsnpException
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException

inline fun <T> Result<T>.onYsnpFailure(action: (exception: YsnpException) -> Unit): Result<T> {
    val exception = exceptionOrNull() as? YsnpException
    if (exception != null) action(exception)
    return this
}

inline fun <T> Result<T>.onUnknownFailure(action: (exception: Throwable) -> Unit): Result<T> {
    val exception = exceptionOrNull()
    if (exception != null && exception !is YsnpException) action(exception)
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
    Timber.e(exception, errorMessage)
    Result.failure(exception)
}
