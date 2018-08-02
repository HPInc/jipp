// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/** A key-value map where keys and values are both strings. */
data class KeyValues(
    /** Pairs of key/values. */
    val pairs: List<Pair<String, String>> = listOf(),
    /**
     * The original string representation, if known. If present this exact string will be written instead of
     * the contents [pairs].
     */
    val _encoded: String? = null
) : List<Pair<String, String>> by pairs {
    constructor(map: Map<String, String>) : this(map.toList())
    constructor(vararg keyValues: String) : this(keyValues.toList().zipWithNext())

    val map: Map<String, String> by lazy {
        pairs.toMap()
    }

    companion object {
        const val ELEMENT_SEPARATOR = ";"
        const val PART_SEPARATOR = "="
        val codec = AttributeGroup.codec(Tag.octetString, {
            parse(readString())
        }, {
            // Write the original string, or fall back to pairs if _encoded is not present
            writeString(it._encoded ?: it.combine())
        })

        fun parse(combined: String) =
            KeyValues(combined.split(ELEMENT_SEPARATOR)
                .map { it.split(PART_SEPARATOR) }
                .filter { it.size == 2 && it[0].isNotEmpty() && it[1].isNotEmpty() }
                .map { it[0] to it[1] }, combined)
    }

    fun combine() =
        pairs.joinToString(ELEMENT_SEPARATOR) { "${it.first}$PART_SEPARATOR${it.second}" }

    override fun toString() = "{${combine()}}"
}

/** Represents an attribute type encoded in key-value pairs. */
class KeyValueType(override val name: String) : AttributeType<KeyValues> {
    override fun coerce(value: Any): KeyValues? =
        when (value) {
            is ByteArray -> KeyValues.parse(String(value))
            else -> null
        }

    fun of(vararg keyValues: String) =
        of(KeyValues(keyValues.toList().zipWithNext()))

}
