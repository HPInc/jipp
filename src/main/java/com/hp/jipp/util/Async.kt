package com.hp.jipp.util

import java.util.*
import java.util.concurrent.*

/**
 * A holder for a value that may complete successfully or end in a thrown error.
 * Similar to Scala's [Future](https://www.scala-lang.org/api/2.12.2/scala/concurrent/Future.html).
 */
class Async<T> private constructor(val executor: ExecutorService = DEFAULT_EXECUTOR) {

    constructor(executor: ExecutorService = DEFAULT_EXECUTOR, block: () -> T) : this(executor) {
        future = executor.submit(Callable {
            result = Try { block() }
        })
    }

    @JvmOverloads constructor(executor: ExecutorService = DEFAULT_EXECUTOR, toRun: Callable<T>) :
            this(executor, toRun::call)

    /** The result at this moment, if any */
    @Volatile var result: Try<T>? = null
        private set(value) {
            synchronized(this) {
                value ?: throw IllegalArgumentException("Cannot set null")
                if (field != null) throw IllegalArgumentException("Already completed")
                field = value
                // No need to bother with any of this now
                onCancel = null
                future = null
            }
            tell()
        }

    /** Listeners waiting for results */
    private var listeners: List<Listener<T>> = listOf()

    /** Future doing work on a service, if necessary */
    private var future: Future<Unit>? = null

    /** Thing to do if it's possible to cancel before a future gets started */
    private var onCancel: (() -> Unit)? = null

    /** The success value, or null if incomplete or unsuccessful */
    val value: T?
        get() {
            val result = this.result
            return when (result) {
                is Success<T> -> result.value
                else -> null
            }
        }

    /** The error, or null if incomplete or successful */
    val error: Throwable?
        get() {
            val result = this.result
            return when (result) {
                is Error<T> -> result.thrown
                else -> null
            }
        }

    /** Return true if completed successfully with a value */
    fun isValue(): Boolean = synchronized(this) {
        return isComplete() && result is Success<T>
    }

    /** Return true if completed with error */
    fun isError(): Boolean = synchronized(this) {
        return isComplete() && result is Error<T>
    }

    /** Return true if complete with either success or error. */
    fun isComplete() = result != null

    /** Stop the attempt to get a value, returning true if the attempt to cancel was successful */
    fun cancel(): Boolean = synchronized(this) {
        // If it's complete then can't do anything
        if (isComplete()) return false

        // If onCancel is present use it to cancel
        if (onCancel != null) {
            onCancel!!()
            onCancel = null
            result = Error(CancellationException())
            return true
        }

        // If future is present try to use it to cancel
        if (future == null || !future!!.cancel(true)) return false
        safeSetResult(Error(CancellationException()))
        true
    }

    /** Wait up to timeout for a value to arrive and return it, or null */
    fun await(timeout: Long): T? = try { get(timeout) } catch (ignored: Throwable) { null }

    /** Like [await] but throws if the attempt to get the value threw */
    fun get(timeout: Long): T? {
        if (!isComplete()) {
            val latch = CountDownLatch(1)
            listen(object : Listener<T> {
                override fun onError(thrown: Throwable) {
                    latch.countDown()
                }

                override fun onSuccess(value: T) {
                    latch.countDown()
                }
            })
            latch.await(timeout, TimeUnit.MILLISECONDS)
        }

        return this.result?.get()
    }

    /** Tell all current listeners about results */
    private fun tell() {
        if (result == null) return
        val toTell = synchronized(this) {
            val toTell = listeners
            // Only one notification is given
            listeners = listOf()
            toTell
        }
        val result = this.result
        when (result) {
            is Success<T> -> toTell.forEach { executor.execute { it.onSuccess(result.value) } }
            is Error<T> -> toTell.forEach { executor.execute { it.onError(result.thrown) } }
        }
    }

    /** Adds a callback to be invoked if success is reached and returns the original async. */
    @JvmOverloads fun onSuccess(executor: ExecutorService = this.executor, callback: SuccessListener<T>): Async<T> =
            onSuccess(executor, callback::onSuccess)

    /** Adds a callback to be invoked if success is reached and returns the original async. */
    fun onSuccess(executor: ExecutorService = this.executor, callback: (T) -> Unit): Async<T> {
        listen(object : Async.Listener<T> {
            override fun onSuccess(value: T) {
                executor.submit {
                    callback(value)
                }
            }

            override fun onError(thrown: Throwable) {
                // Ignore
            }
        })
        return this
    }

    @JvmOverloads fun onError(executor: ExecutorService = this.executor, callback: ErrorListener): Async<T> =
            onError(executor, callback::onError)

    /** Adds a callback to be invoked if an error is reached and returns the original async. */
    @JvmOverloads fun onError(executor: ExecutorService = this.executor, callback: (Throwable) -> Unit): Async<T> {
        listen(object : Async.Listener<T> {
            override fun onSuccess(value: T) {
                // Ignore
            }

            override fun onError(thrown: Throwable) {
                executor.submit {
                    callback(thrown)
                }
            }
        })
        return this
    }

    /** Adds a callback to be invoked upon completion, and returns the original async */
    @JvmOverloads fun onDone(executor: ExecutorService = this.executor, callback: Listener<T>): Async<T> {
        listen(object : Async.Listener<T> {
            override fun onSuccess(value: T) {
                executor.submit {
                    callback.onSuccess(value)
                }
            }

            override fun onError(thrown: Throwable) {
                executor.submit {
                    callback.onError(thrown)
                }
            }
        })
        return this
    }

    @JvmOverloads fun <U> map(executor: ExecutorService = this.executor, mapper: Mapper<T, U>) =
            flatMap(executor, { thrown -> Async(executor) { (mapper::map)(thrown) } })

    /** Return a new async to hold the converted result */
    fun <U> map(executor: ExecutorService = this.executor, mapper: (T) -> U): Async<U> =
            flatMap(executor, { thrown -> Async(executor) { mapper(thrown) } })

    @JvmOverloads fun <U> flatMap(executor: ExecutorService = this.executor, mapper: FlatMapper<T, U>) =
            flatMap(executor, mapper::map)

    /** Return a new async to hold the converted asynchronous result */
    fun <U> flatMap(executor: ExecutorService = this.executor, mapper: (T) -> Async<U>): Async<U> {
        val async = Async<U>(executor)

        val listener = object : Listener<T> {
            override fun onSuccess(value: T) {
                val next = mapper(value)
                next.listen(object : Listener<U> {
                    override fun onSuccess(value: U) {
                        async.safeSetResult(Success(value))
                    }

                    override fun onError(thrown: Throwable) {
                        async.safeSetResult(Error(thrown))
                    }
                })
                async.onCancel = { -> next.cancel() }
            }

            override fun onError(thrown: Throwable) {
                async.safeSetResult(Error(thrown))
            }
        }

        // If this is cancelled then really just stop listening
        async.onCancel = { -> unlisten(listener) }
        listen(listener)
        return async
    }

    /** Safely set a result when it's not clear whether cancel might have occurred */
    private fun safeSetResult(result: Try<T>) = synchronized(this) {
        if (!isComplete()) this.result = result
    }


    /** Return a new async that applies the mapper if this async results in an error */
    @JvmOverloads fun recover(executor: ExecutorService = this.executor, mapper: Mapper<Throwable, T>) =
            recover(executor, mapper::map)

    /** Return a new async that applies the mapper if this async results in an error */
    fun recover(executor: ExecutorService = this.executor, mapper: (Throwable) -> T) =
            flatRecover(executor, { thrown -> Async(executor) { mapper(thrown) } })

    /** Return a new async that applies the mapper if this async results in an error */
    @JvmOverloads fun flatRecover(executor: ExecutorService = this.executor, mapper: FlatMapper<Throwable, T>) =
            flatRecover(executor, mapper::map)

    /** Return a new async that applies the mapper if this async results in an error */
    fun flatRecover(executor: ExecutorService = this.executor, mapper: (Throwable) -> Async<T>): Async<T> {
        val async = Async<T>(executor)
        val listener = object : Listener<T> {
            override fun onSuccess(value: T) {
                async.result = Success(value)
            }

            override fun onError(thrown: Throwable) {
                val next = mapper(thrown)
                next.listen(object : Listener<T> {
                    override fun onError(thrown: Throwable) {
                        async.safeSetResult(Error(thrown))
                    }

                    override fun onSuccess(value: T) {
                        async.safeSetResult(Success(value))
                    }
                })
                async.onCancel = { -> next.cancel() }
            }
        }
        async.onCancel = { -> unlisten(listener) }
        listen(listener)
        return async
    }

    /** Listen for completion */
    private fun listen(listener: Listener<T>): Async<T> {
        synchronized(this) {
            listeners += listener
        }
        tell()
        return this
    }

    /** Stop listening for completion */
    private fun unlisten(listener: Listener<T>): Async<T> {
        synchronized(this) {
            listeners -= listener
        }
        return this
    }

    /** Return a new Async which provides this async's success value after a delay */
    @JvmOverloads fun delay(executor: ExecutorService = this.executor, millis: Long): Async<T> {
        val async = Async<T>(executor)
        val timer = Timer()
        val listener = object : Listener<T> {
            override fun onSuccess(value: T) {
                async.onCancel = { timer.cancel() }
                timer.schedule(object: TimerTask() {
                    override fun run() {
                        async.safeSetResult(Success(value))
                    }
                }, millis)
            }

            override fun onError(thrown: Throwable) {
                async.safeSetResult(Error(thrown))
            }
        }
        async.onCancel = { unlisten(listener) }
        listen(listener)
        return async
    }

    interface Listener<in T> : ErrorListener, SuccessListener<T>

    interface SuccessListener<in T> {
        fun onSuccess(value: T)
    }

    interface ErrorListener {
        fun onError(thrown: Throwable)
    }

    interface Mapper<in T, out U> {
        @Throws(Throwable::class) fun map(from: T): U
    }

    interface FlatMapper<in T, U> {
        @Throws(Throwable::class) fun map(from: T): Async<U>
    }

    companion object {
        private var PER_PROCESSOR = 4
        private val DEFAULT_EXECUTOR = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() *
                PER_PROCESSOR)

        /** Return a completed async containing an error */
        @JvmStatic fun <T> error(thrown: Throwable): Async<T> {
            val async = Async<T>()
            async.result = Error(thrown)
            return async
        }

        /** Return a completed async containing success */
        @JvmStatic fun <T> success(value: T): Async<T> {
            val async = Async<T>()
            async.result = Success(value)
            return async
        }

        /** Return an async which waits before running something */
        @JvmStatic @JvmOverloads fun delay(executor: ExecutorService = DEFAULT_EXECUTOR, delay: Long,
                                             toRun: Runnable): Async<Unit> =
                delay(delay, executor, { -> toRun.run() })

        /** Return an async which waits before running something */
        @JvmStatic @JvmOverloads fun <T> delay(executor: ExecutorService = DEFAULT_EXECUTOR, delay: Long,
                                                 toRun: Callable<T>) =
                delay(delay, executor, toRun::call)

        /** Return an async which waits before running something */
        fun <T> delay(delay: Long, executor: ExecutorService = DEFAULT_EXECUTOR,
                                                 toRun: () -> T): Async<T> =
                Async.success(Unit).delay(millis = delay).map(executor) { _ -> toRun() }

        /** Return an async which completes when something has finished executing */
        @JvmStatic @JvmOverloads fun run(executor: ExecutorService = DEFAULT_EXECUTOR, toRun: Runnable): Async<Unit> =
                Async(executor, { -> toRun.run() })

        // TODO: Join? Zip?
    }
}
