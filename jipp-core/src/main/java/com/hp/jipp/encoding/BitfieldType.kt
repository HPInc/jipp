// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/** An [AttributeType] for values bitwise OR'd together. Only used by CUPS. */
open class BitwiseType(
    override val name: String,
) : AttributeTypeImpl<Int>(name, Int::class.java) {

    override fun coerce(value: Any) =
        super.coerce(value) ?: (value as? Int)?.toInt()

    override fun toString() = "BitwiseType($name)"

    companion object {
        val codec = Codec(Tag.enumValue, {
            readIntValue()
        }, {
            writeIntValue(it)
        })
    }
}
