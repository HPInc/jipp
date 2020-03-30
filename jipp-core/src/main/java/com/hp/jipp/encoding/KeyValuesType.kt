// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/** An [AttributeType] for a value containing [KeyValues] pairs. */
open class KeyValuesType(name: String) : AttributeTypeImpl<KeyValues>(name, KeyValues::class.java) {
    /** An [AttributeType] for multiple values of [KeyValues] pairs. */
    class Set(name: String) : KeyValuesType(name), AttributeSetType<KeyValues> {
        override fun toString() = "KeyValuesType.Set($name)"
    }

    /**
     * Construct an [Attribute] containing a value of supplied key/value pairs.
     * Each pair is supplied as a pair of arguments, e.g. `[of]("key1", "value1", "key2", "value2")`.
     */
    fun of(vararg keyValues: String) =
        @Suppress("SpreadOperator")
        of(KeyValues(*keyValues))

    override fun coerce(value: Any): KeyValues? =
        when (value) {
            is String -> KeyValues.parse(value)
            is ByteArray -> coerce(String(value))
            is KeyValues -> value
            else -> null
        }

    override fun toString() = "KeyValuesType($name)"
}
