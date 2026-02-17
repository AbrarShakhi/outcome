package com.github.abrarshakhi.outcome

import kotlin.coroutines.cancellation.CancellationException

/**
 * Represents the result of a computation that can either succeed with a value of type [T]
 * or fail with an error of type [E].
 *
 * This is similar in concept to Kotlin's `Result`, but allows a custom error type.
 *
 * @param T the success value type
 * @param E the error type
 */
sealed interface Outcome<out T, out E> {

    /**
     * Represents a successful outcome holding a [value].
     *
     * @param T the type of the success value
     * @property value the success value
     */
    data class Ok<T>(val value: T) : Outcome<T, Nothing>

    /**
     * Represents a failed outcome holding an [error].
     *
     * @param E the type of the error
     * @property error the failure value
     */
    data class Err<E>(val error: E) : Outcome<Nothing, E>

    companion object {

        /**
         * Creates a successful [Outcome] containing the given [data].
         *
         * @param T the type of the success value
         * @param data the value to wrap
         * @return an [Outcome.Ok] containing [data]
         */
        fun <T> ofOk(data: T): Outcome<T, Nothing> = Ok(data)

        /**
         * Creates a failed [Outcome] containing the given [error].
         *
         * @param E the type of the error
         * @param error the error to wrap
         * @return an [Outcome.Err] containing [error]
         */
        fun <E> ofErr(error: E): Outcome<Nothing, E> = Err(error)

        /**
         * Executes the given [block] and captures its result as an [Outcome].
         *
         * If the [block] completes successfully, its result is wrapped in [Outcome.Ok].
         * If it throws an exception, the exception is wrapped in [Outcome.Err].
         *
         * Note: [CancellationException] is rethrown to preserve coroutine cancellation semantics.
         *
         * @param T the result type of the computation
         * @param block the computation to execute
         * @return an [Outcome] representing success or failure
         * @throws CancellationException if the computation is cancelled
         */
        inline fun <T> maybeThrows(block: () -> T): Outcome<T, Throwable> = try {
            ofOk(block())
        } catch (t: Throwable) {
            if (t is CancellationException) throw t
            ofErr(t)
        }
    }
}

/**
 * Returns `true` if this [Outcome] represents a success.
 */
fun Outcome<*, *>.isOk(): Boolean = this is Outcome.Ok<*>

/**
 * Returns `true` if this [Outcome] represents a failure.
 */
fun Outcome<*, *>.isErr(): Boolean = this is Outcome.Err<*>

/**
 * Returns the success value if this is [Outcome.Ok], or `null` if it is [Outcome.Err].
 *
 * @return the success value or `null`
 */
fun <T> Outcome<T, *>.getOrNull(): T? =
    (this as? Outcome.Ok)?.value

/**
 * Returns the error value if this is [Outcome.Err], or `null` if it is [Outcome.Ok].
 *
 * @return the error value or `null`
 */
fun <E> Outcome<*, E>.errorOrNull(): E? =
    (this as? Outcome.Err)?.error

/**
 * Returns the success value if this is [Outcome.Ok].
 *
 * If this is [Outcome.Err], throws the contained error.
 *
 * @param T the success type
 * @param E the error type, which must be a subtype of [Throwable]
 * @return the success value
 * @throws E if this is [Outcome.Err]
 */
fun <T, E : Throwable> Outcome<T, E>.getOrThrow(): T = when (this) {
    is Outcome.Ok -> value
    is Outcome.Err -> throw error
}

/**
 * Returns the error value if this is [Outcome.Err].
 *
 * If this is [Outcome.Ok], throws an [IllegalStateException].
 *
 * @param E the error type
 * @return the error value
 * @throws IllegalStateException if this is [Outcome.Ok]
 */
fun <E> Outcome<*, E>.errorOrThrow(): E = when (this) {
    is Outcome.Ok -> throw IllegalStateException("Cannot get error from Outcome.Ok")
    is Outcome.Err -> error
}

/**
 * Returns the success value if this is [Outcome.Ok],
 * or computes a fallback value using [onError] if this is [Outcome.Err].
 *
 * @param onError function invoked with the error to produce a fallback value
 * @return the success value or the result of [onError]
 */
inline fun <T, E> Outcome<T, E>.getOrElse(onError: (E) -> T): T = when (this) {
    is Outcome.Ok -> value
    is Outcome.Err -> onError(error)
}
