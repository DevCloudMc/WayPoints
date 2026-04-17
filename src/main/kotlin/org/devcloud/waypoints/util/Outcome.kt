package org.devcloud.waypoints.util

sealed class Outcome<out T, out E> {
    data class Ok<T>(val value: T) : Outcome<T, Nothing>()

    data class Err<E>(val error: E) : Outcome<Nothing, E>()

    inline fun <R> map(transform: (T) -> R): Outcome<R, E> =
        when (this) {
            is Ok -> Ok(transform(value))
            is Err -> this
        }

    inline fun <R> mapErr(transform: (E) -> R): Outcome<T, R> =
        when (this) {
            is Ok -> this
            is Err -> Err(transform(error))
        }

    inline fun onOk(block: (T) -> Unit): Outcome<T, E> {
        if (this is Ok) {
            block(value)
        }
        return this
    }

    inline fun onErr(block: (E) -> Unit): Outcome<T, E> {
        if (this is Err) {
            block(error)
        }
        return this
    }
}
