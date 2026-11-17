package org.devcloud.waypoints.util

sealed class Outcome<out T, out E> {
    data class Ok<T>(val value: T) : Outcome<T, Nothing>()

    data class Err<E>(val error: E) : Outcome<Nothing, E>()
}
