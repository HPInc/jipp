// Copyright 2017 - 2021 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/**
 * An [AttributeType] for values bitwise OR'd together. Only used by CUPS.
 * Values are expressed as Long to allow for values of 0x80000000 and above, but are
 * transmitted as 4-byte integers.
 */
class BitfieldType(
    override val name: String
) : AttributeTypeImpl<Int>(name, Int::class.java) {

    override fun coerce(value: Any): Int? =
        when (value) {
            is UntypedEnum -> value.code
            is Int -> value
            else -> { println("Value: $value, ${value.javaClass}"); super.coerce(value) }
        }

    override fun toString() = "BitwiseType($name)"

    companion object {
        val codec = Codec(
            Tag.enumValue,
            {
                readIntValue().toLong()
            },
            {
                writeIntValue(it.toInt())
            }
        )
    }
}
