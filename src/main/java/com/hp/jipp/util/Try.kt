package com.hp.jipp.util

/** An attempt to get a value. Contains either [Success] or [Error] */
sealed class Try<out T> {
    /** Return the value of this try or throw its error */
    abstract fun get(): T

    companion object {
        /** A replacement for `try` which returns the [Try] result of the block */
        operator fun <T> invoke(block: () -> T): Try<T> = try {
            Success(block())
        } catch (thrown: Throwable) {
            Error<T>(thrown)
        }
    }
}

/** A failed instance of [Try] */
data class Error<out T>(val thrown: Throwable): Try<T>() {
    override fun get() = throw thrown
}

/** A successful instance of [Try] */
data class Success<out T>(val value: T): Try<T>() {
    override fun get() = value
}
