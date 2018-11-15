// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/** Represents an attribute type encoded in key-value pairs. */
class KeyValuesType(override val name: String) : AttributeType<KeyValues> {
    override fun coerce(value: Any): KeyValues? =
        when (value) {
            is String -> KeyValues.parse(value)
            is ByteArray -> coerce(String(value))
            is KeyValues -> value
            else -> null
        }

    /**
     * Construct an attribute of this type containing the supplied key/value pairs.
     * Each pair is supplied as a pair of arguments, e.g. `[of]("key1", "value1", "key2", "value2")`.
     */
    fun of(vararg keyValues: String) =
        of(KeyValues(KeyValues.fromPairs(keyValues)))
}
