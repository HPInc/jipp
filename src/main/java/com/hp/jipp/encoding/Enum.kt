package com.hp.jipp.encoding

import com.hp.jipp.util.Reflect

/**
 * A machine-readable integer code paired with a human-readable name.
 */
abstract class Enum {

    abstract val name: String

    abstract val code: Int

    /** A factory for objects of a Enum subclass  */
    interface Factory<out T : Enum> {
        fun of(name: String, code: Int): T
    }

    override fun toString() = name

    companion object {
        /** Convert a List of T into a Map of integer codes to T, where T is a Enum subclass. */
        fun <T : Enum> toCodeMap(nameCodes: Iterable<T>): Map<Int, T> =
                nameCodes.map { it.code to it }.toMap()

        /** Using Java reflection, look up all statically-declared instances of T */
        fun <T : Enum> allFrom(cls: Class<*>): Collection<T> {
            return Reflect.getStaticObjects(cls).filter { cls.isAssignableFrom(it.javaClass) }.map {
                @Suppress("UNCHECKED_CAST")
                it as T
            }
        }
    }
}
