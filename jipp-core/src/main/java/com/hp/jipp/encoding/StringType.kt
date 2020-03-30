// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/**
 * An [AttributeType] for a value best represented as a [String].
 */
open class StringType(val tag: Tag, name: String) : AttributeTypeImpl<String>(name, String::class.java) {
    /** An [AttributeType] for values best represented as a [String]. */
    class Set(tag: Tag, name: String) : StringType(tag, name), AttributeSetType<String> {
        override fun toString() = "StringType.Set($name)"
    }

    override fun coerce(value: Any) =
        when (value) {
            is String -> value
            is OtherString -> value.value
            else -> null
        }
}
