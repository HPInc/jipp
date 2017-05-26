package com.hp.jipp.encoding

import com.hp.jipp.util.Util

/**
 * A machine-readable integer code paired with a human-readable name.
 */
abstract class NameCode {

    abstract val name: String

    abstract val code: Int

    /** A factory for objects of a NameCode subclass  */
    interface Factory<out T : NameCode> {
        fun of(name: String, code: Int): T
    }

    override fun toString() = name

    companion object {
        /** Convert a List of T into a Map of integer codes to T, where T is a NameCode subclass. */
        fun <T : NameCode> toCodeMap(nameCodes: Iterable<T>): Map<Int, T> =
                nameCodes.map { it.code to it }.toMap()

        /** Using Java reflection, look up all statically-declared instances of T */
        fun <T : NameCode> allFrom(cls: Class<*>): Collection<T> {
            return Util.getStaticObjects(cls).filter { cls.isAssignableFrom(it.javaClass) }.map {
                @Suppress("UNCHECKED_CAST")
                it as T
            }
        }
    }
}
