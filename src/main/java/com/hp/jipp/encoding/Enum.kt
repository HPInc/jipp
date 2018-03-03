package com.hp.jipp.encoding

import com.hp.jipp.util.getStaticObjects

/**
 * A machine-readable integer code paired with a human-readable name.
 */
abstract class Enum {

    abstract val code: Int

    abstract val name: String

    /** A factory for objects of a Enum subclass  */
    interface Factory<out T : Enum> {
        /** Return a new [Enum] from a name/code pair */
        fun of(code: Int, name: String): T
    }

    override fun toString() = name

    companion object {
        /** Convert a List of T into a Map of integer codes to T, where T is a Enum subclass. */
        fun <T : Enum> toCodeMap(nameCodes: Iterable<T>): Map<Int, T> =
                nameCodes.map { it.code to it }.toMap()

        /** Using Java reflection, look up all statically-declared instances of T */
        fun <T : Enum> allFrom(cls: Class<*>): Collection<T> {
            return cls.getStaticObjects().filter { cls.isAssignableFrom(it.javaClass) }.map {
                @Suppress("UNCHECKED_CAST")
                it as T
            }
        }
    }
}
