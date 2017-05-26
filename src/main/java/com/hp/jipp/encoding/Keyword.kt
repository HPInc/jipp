package com.hp.jipp.encoding

/**
 * A known sequence of characters.

 * @see [RFC2911 Section 4.1.3](https://tools.ietf.org/html/rfc2911.section-4.1.3)
 */
abstract class Keyword {
    abstract val name: String

    /** A factory for Keyword objects */
    interface Factory<out T : Keyword> {
        fun of(name: String): T
    }

    override fun toString() = name
}
