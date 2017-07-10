package com.hp.jipp.util

import java.util.*
import java.util.concurrent.*

typealias TryCallback<T> = (Try<T>) -> Unit

/**
 * A holder for a value that may complete successfully or end in a thrown error.
 * Similar to Scala's [Future](https://www.scala-lang.org/api/2.12.2/scala/concurrent/Future.html).
 */
class Async<Result> private constructor(val executor: ExecutorService = DEFAULT_EXECUTOR) {

    constructor(executor: ExecutorService = DEFAULT_EXECUTOR, block: () -> Result) : this(executor) {
        future = executor.submit(Callable {
            result = Try { block() }
        })
    }

    @JvmOverloads constructor(executor: ExecutorService = DEFAULT_EXECUTOR, toRun: Callable<Result>) :
            this(executor, toRun::call)

    /** The result at this moment, if any */
    @Volatile var result: Try<Result>? = null
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
    private var listeners: List<TryCallback<Result>> = listOf()

    /** Future doing work on a service, if necessary */
    private var future: Future<Unit>? = null

    /** Thing to do if it's possible to cancel before a future gets started */
    private var onCancel: (() -> Unit)? = null

    /** The success value, or null if incomplete or unsuccessful */
    val value: Result?
        get() {
            val result = this.result
            return when (result) {
                is Success<Result> -> result.value
                else -> null
            }
        }

    /** The error, or null if incomplete or successful */
    val error: Throwable?
        get() {
            val result = this.result
            return when (result) {
                is Error<Result> -> result.thrown
                else -> null
            }
        }

    /** Return true if completed successfully with a value */
    fun isValue(): Boolean = synchronized(this) {
        return isComplete() && result is Success<Result>
    }

    /** Return true if completed with error */
    fun isError(): Boolean = synchronized(this) {
        return isComplete() && result is Error<Result>
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

    /** Cancels this async if it has not completed in the specified period */
    fun timeout(millis: Long) {
        val timer = Timer()
        timer.schedule(object: TimerTask() {
            override fun run() {
                this@Async.cancel()
            }
        }, millis)

        listen { _ -> timer.cancel() }
    }

    /** Wait up to timeout for a value to arrive and return it, or null */
    fun await(timeout: Long): Result? = try { get(timeout) } catch (ignored: Throwable) { null }

    /** Like [await] but throws if the attempt to get the value threw */
    fun get(timeout: Long): Result? {
        if (!isComplete()) {
            val latch = CountDownLatch(1)
            listen { _ -> latch.countDown() }
            latch.await(timeout, TimeUnit.MILLISECONDS)
        }

        return this.result?.get()
    }

    /** Tell all callbacks about results */
    private fun tell() {
        val result = this.result ?: return
        val toTell = synchronized(this) {
            val toTell = listeners
            // Only one notification is given
            listeners = listOf()
            toTell
        }
        // Inform outside of synchronized block
        toTell.forEach { executor.execute { it(result) }}
    }

    /** Listen for completion, returning this */
    private fun listen(listener: TryCallback<Result>): Async<Result> {
        synchronized(this) {
            listeners += listener
        }
        tell()
        return this
    }

    /** Stop listening for completion */
    private fun unlisten(listener: TryCallback<Result>): Async<Result> {
        synchronized(this) {
            listeners -= listener
        }
        return this
    }

    /** Adds a callback to be invoked if success is reached and returns the original async. */
    @JvmOverloads fun onSuccess(executor: ExecutorService = this.executor, callback: SuccessListener<Result>): Async<Result> =
            onSuccess(executor, callback::onSuccess)

    /** Adds a callback to be invoked if success is reached and returns the original async. */
    fun onSuccess(executor: ExecutorService = this.executor, callback: (Result) -> Unit) =
            listen { if (it is Success<Result>) executor.submit { callback(it.value) } }

    @JvmOverloads fun onError(executor: ExecutorService = this.executor, callback: ErrorListener): Async<Result> =
            onError(executor, callback::onError)

    /** Adds a callback to be invoked if an error is reached and returns the original async. */
    @JvmOverloads fun onError(executor: ExecutorService = this.executor, callback: (Throwable) -> Unit) =
            listen { if (it is Error<Result>) executor.submit { callback(it.thrown) } }

    /** Adds a callback to be invoked upon completion, and returns the original async */
    @JvmOverloads fun onDone(executor: ExecutorService = this.executor, callback: Listener<Result>) =
            onDone(executor) {
                when (it) {
                    is Error<Result> -> callback.onError(it.thrown)
                    is Success<Result> -> callback.onSuccess(it.value)
                }
            }

    /** Adds a callback to be invoked upon completion, and returns the original async */
    fun onDone(executor: ExecutorService = this.executor, callback: TryCallback<Result>) =
        listen { executor.submit { callback(it) } }

    @JvmOverloads fun <U> map(executor: ExecutorService = this.executor, mapper: Mapper<Result, U>) =
            flatMap(executor, { thrown -> Async(executor) { (mapper::map)(thrown) } })

    /** Return a new async to hold the converted result */
    fun <U> map(executor: ExecutorService = this.executor, mapper: (Result) -> U): Async<U> =
            flatMap(executor, { thrown -> Async(executor) { mapper(thrown) } })

    @JvmOverloads fun <U> flatMap(executor: ExecutorService = this.executor, mapper: Mapper<Result, Async<U>>) =
            flatMap(executor, mapper::map)

    /** Return a new async to hold the converted asynchronous result */
    fun <U> flatMap(executor: ExecutorService = this.executor, mapper: (Result) -> Async<U>): Async<U> {
        val async = Async<U>(executor)

        val listener: TryCallback<Result> = {
            when(it) {
                is Error<Result> -> async.safeSetResult(Error<U>(it.thrown))
                is Success<Result> -> {
                    val next = mapper(it.value)
                    next.listen { async.safeSetResult(it) }
                    async.onCancel = { -> next.cancel() }
                }
            }
        }

        // If this is cancelled then really just stop listening
        async.onCancel = { -> unlisten(listener) }
        listen(listener)
        return async
    }

    /** Safely set a result when it's not clear whether cancel might have occurred */
    private fun safeSetResult(result: Try<Result>) = synchronized(this) {
        if (!isComplete()) this.result = result
    }


    /** Return a new async that applies the mapper if this async results in an error */
    @JvmOverloads fun recover(executor: ExecutorService = this.executor, mapper: Mapper<Throwable, Result>) =
            recover(executor, mapper::map)

    /** Return a new async that applies the mapper if this async results in an error */
    fun recover(executor: ExecutorService = this.executor, mapper: (Throwable) -> Result) =
            flatRecover(executor, { thrown -> Async(executor) { mapper(thrown) } })

    /** Return a new async that applies the mapper if this async results in an error */
    @JvmOverloads fun flatRecover(executor: ExecutorService = this.executor, mapper: Mapper<Throwable, Async<Result>>) =
            flatRecover(executor, mapper::map)

    /** Return a new async that applies the mapper if this async results in an error */
    fun flatRecover(executor: ExecutorService = this.executor, mapper: (Throwable) -> Async<Result>): Async<Result> {
        val async = Async<Result>(executor)
        val listener: TryCallback<Result> = {
            when(it) {
                is Error<Result> -> {
                    val next = mapper(it.thrown)
                    next.listen { async.safeSetResult(it) }
                    async.onCancel = { -> next.cancel() }
                }
                is Success<Result> -> async.result = it
            }
        }
        async.onCancel = { -> unlisten(listener) }
        listen(listener)
        return async
    }

    /** Return a new Async which provides this async's success value after a delay */
    @JvmOverloads fun delay(executor: ExecutorService = this.executor, millis: Long): Async<Result> {
        val async = Async<Result>(executor)
        val timer = Timer()

        val listener: TryCallback<Result> = {
            when (it) {
                is Error<Result> -> async.safeSetResult(it)
                is Success<Result> -> {
                    async.onCancel = { timer.cancel() }
                    timer.schedule(object : TimerTask() {
                        override fun run() {
                            async.safeSetResult(it)
                        }
                    }, millis)
                }
            }
        }
        async.onCancel = { unlisten(listener) }
        listen(listener)
        return async
    }

    // Well-named callbacks for Java use
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
