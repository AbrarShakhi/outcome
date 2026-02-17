package com.github.abrarshakhi.outcome

import com.github.abrarshakhi.outcome.Outcome.Companion.maybeThrows

/**
 * Transforms the success value using [transform] and captures any thrown exception
 * as an [Outcome.Err].
 *
 * If this is [Outcome.Ok], the [transform] function is applied to the value.
 * If the transformation throws, the exception is wrapped in [Outcome.Err].
 *
 * If this is [Outcome.Err], it is returned unchanged.
 *
 * This function is only available when the error type is [Throwable].
 *
 * @param transform the function to apply to the success value
 * @return a new [Outcome] containing the transformed value or captured exception
 */
inline fun <T, R> Outcome<T, Throwable>.mapCatching(
    transform: (T) -> R
): Outcome<R, Throwable> = when (this) {
    is Outcome.Ok -> maybeThrows { transform(value) }
    is Outcome.Err -> this
}

/**
 * Performs the given [action] if this is [Outcome.Ok].
 *
 * The original [Outcome] is returned unchanged.
 *
 * @param action the side-effect to perform on the success value
 * @return this [Outcome]
 */
inline fun <T, E> Outcome<T, E>.onOk(action: (T) -> Unit): Outcome<T, E> = apply {
    if (this is Outcome.Ok) action(value)
}

/**
 * Performs the given [action] if this is [Outcome.Err].
 *
 * The original [Outcome] is returned unchanged.
 *
 * @param action the side-effect to perform on the error value
 * @return this [Outcome]
 */
inline fun <T, E> Outcome<T, E>.onErr(action: (E) -> Unit): Outcome<T, E> = apply {
    if (this is Outcome.Err) action(error)
}

/**
 * Returns the success value if this is [Outcome.Ok].
 *
 * If this is [Outcome.Err], throws the [Throwable] produced by [throwable].
 *
 * @param throwable supplies the exception to throw on failure
 * @return the success value
 * @throws Throwable if this is [Outcome.Err]
 */
inline fun <T> Outcome<T, *>.getOrThrowWith(throwable: () -> Throwable): T = when (this) {
    is Outcome.Ok -> value
    is Outcome.Err -> throw throwable()
}

/**
 * Returns the error value if this is [Outcome.Err].
 *
 * If this is [Outcome.Ok], throws the [Throwable] produced by [throwable].
 *
 * @param throwable supplies the exception to throw on success
 * @return the error value
 * @throws Throwable if this is [Outcome.Ok]
 */
inline fun <E> Outcome<*, E>.errorOrThrowWith(throwable: () -> Throwable): E = when (this) {
    is Outcome.Ok -> throw throwable()
    is Outcome.Err -> error
}

/**
 * Applies [onOk] if this is [Outcome.Ok], or [onErr] if this is [Outcome.Err],
 * and returns the result.
 *
 * This function allows handling both cases in a single expression.
 *
 * @param onOk function invoked with the success value
 * @param onErr function invoked with the error value
 * @return the result of either [onOk] or [onErr]
 */
inline fun <T, E, R> Outcome<T, E>.fold(
    onOk: (T) -> R,
    onErr: (E) -> R
): R = when (this) {
    is Outcome.Ok -> onOk(value)
    is Outcome.Err -> onErr(error)
}

/**
 * Transforms the success value using [transform].
 *
 * If this is [Outcome.Err], it is returned unchanged.
 *
 * @param transform the function to apply to the success value
 * @return a new [Outcome] containing the transformed value
 */
inline fun <T, E, R> Outcome<T, E>.map(
    transform: (T) -> R
): Outcome<R, E> = when (this) {
    is Outcome.Ok -> Outcome.Ok(transform(value))
    is Outcome.Err -> this
}

/**
 * Transforms the error value using [transform].
 *
 * If this is [Outcome.Ok], it is returned unchanged.
 *
 * @param transform the function to apply to the error value
 * @return a new [Outcome] containing the transformed error
 */
inline fun <T, E, F> Outcome<T, E>.mapError(
    transform: (E) -> F
): Outcome<T, F> = when (this) {
    is Outcome.Ok -> this
    is Outcome.Err -> Outcome.Err(transform(error))
}

/**
 * Chains another computation that returns an [Outcome].
 *
 * If this is [Outcome.Ok], [transform] is invoked with the success value.
 * If this is [Outcome.Err], it is returned unchanged.
 *
 * This is useful for composing multiple dependent operations.
 *
 * @param transform the function producing the next [Outcome]
 * @return the resulting [Outcome]
 */
inline fun <T, E, R> Outcome<T, E>.flatMap(
    transform: (T) -> Outcome<R, E>
): Outcome<R, E> = when (this) {
    is Outcome.Ok -> transform(value)
    is Outcome.Err -> this
}

/**
 * Recovers from a failure by transforming the error into a new [Outcome].
 *
 * If this is [Outcome.Ok], it is returned unchanged.
 *
 * @param transform the function producing a replacement [Outcome] for the error
 * @return the recovered [Outcome]
 */
inline fun <T, E> Outcome<T, E>.recoverWith(
    transform: (E) -> Outcome<T, E>
): Outcome<T, E> = when (this) {
    is Outcome.Ok -> this
    is Outcome.Err -> transform(error)
}

/**
 * Flattens a nested [Outcome].
 *
 * Converts an `Outcome<Outcome<T, E>, E>` into `Outcome<T, E>`.
 *
 * If this is [Outcome.Ok], its inner value is returned.
 * If this is [Outcome.Err], it is returned unchanged.
 *
 * @return the flattened [Outcome]
 */
fun <T, E> Outcome<Outcome<T, E>, E>.flatten(): Outcome<T, E> = when (this) {
    is Outcome.Ok -> value
    is Outcome.Err -> this
}

/**
 * Converts this [Outcome] into a [Pair] of (value, error).
 *
 * - If this is [Outcome.Ok], returns Pair(value, null)
 * - If this is [Outcome.Err], returns Pair(null, error)
 */
fun <T, E> Outcome<T, E>.toPair(): Pair<T?, E?> =
    getOrNull() to errorOrNull()
