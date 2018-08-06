// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/** A map of keys to values where keys and values are both strings. */
class KeyValues(
    /** Pairs of key/values. */
    val pairs: Map<String, String> = linkedMapOf(),
    /**
     * The original string representation, if known. If present this exact string will be written instead of
     * the contents [pairs]. This may differ from the calculated representation from [pairs] due to
     * implementations that differ in appending the final ";".
     *
     * Note: this field is ignored by equals/hashcode methods.
     */
    val _encoded: String? = null
) : Map<String, String> by pairs {

    constructor(vararg pairs: Pair<String, String>) : this(pairs.toMap())

    /** Each key/value pair is supplied as a pair of arguments, e.g. `("key1", "value1", "key2", "value2")`. */
    constructor(vararg keyValues: String) : this(fromPairs(keyValues))

    override fun equals(other: Any?) =
        if (this === other) true else when (other) {
            is KeyValues -> pairs == other.pairs
            is Map<*, *> -> other == this // Fall back to other's equals implementation
            else -> false
        }

    override fun hashCode() = pairs.hashCode()

    companion object {
        private const val ELEMENT_SEPARATOR = ";"
        private const val PART_SEPARATOR = "="
        val codec = AttributeGroup.codec(Tag.octetString, {
            parse(readString())
        }, {
            // Write the original string, or fall back to pairs if _encoded is not present
            writeString(it._encoded ?: it.combine())
        })

        /** Convert an IPP string to an ordered KeyValues map. */
        fun parse(combined: String) =
            KeyValues(combined.split(ELEMENT_SEPARATOR)
                .map { it.split(PART_SEPARATOR) }
                .filter { it.size == 2 && it[0].isNotEmpty() && it[1].isNotEmpty() }
                .map { it[0] to it[1] }
                .toMap(), combined)

        /** Convert an array of items (key1, value1, ...) into pairs of items. */
        internal fun fromPairs(keyValues: Array<out String>) =
            keyValues.toList().windowed(2, 2).map { it[0] to it[1] }.toMap()
    }

    /** Return the contents of this map combined into an IPP string representation. */
    fun combine() =
        pairs.entries.joinToString(ELEMENT_SEPARATOR) { "${it.key}$PART_SEPARATOR${it.value}" } + ELEMENT_SEPARATOR

    override fun toString() = "{${combine()}}"
}
