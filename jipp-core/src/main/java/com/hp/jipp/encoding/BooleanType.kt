// © Copyright 2017 - 2021 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/** An [AttributeType] for a [Boolean] value. */
open class BooleanType(name: String) : AttributeTypeImpl<Boolean>(name, Boolean::class.java) {
    /** An [AttributeType] for multiple [Boolean] values. */
    class Set(name: String) : BooleanType(name), AttributeSetType<Boolean> {
        override fun toString() = "BooleanType.Set($name)"
    }

    override fun coerce(value: Any) =
        value as? Boolean

    companion object {
        val codec = Codec(
            Tag.booleanValue,
            {
                readByteValue() != 0.toByte()
            },
            {
                writeByteValue(if (it) 0x01 else 0x00)
            }
        )
    }
}
