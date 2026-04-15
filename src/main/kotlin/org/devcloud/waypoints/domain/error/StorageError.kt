package org.devcloud.waypoints.domain.error

sealed class StorageError(open val cause: Throwable? = null) {
    class IO(override val cause: Throwable) : StorageError(cause)

    class Serialization(val message: String, override val cause: Throwable? = null) : StorageError(cause)

    class Concurrency(val message: String) : StorageError()

    class Unavailable(val message: String) : StorageError()
}
