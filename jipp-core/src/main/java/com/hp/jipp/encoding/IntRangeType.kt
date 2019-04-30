// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/** An attribute type containing [IntRange] values. */
open class IntRangeType(override val name: String) : AttributeType<IntRange> {
    override fun coerce(value: Any) =
        value as? IntRange

    override fun toString() = "IntRangeType($name)"

    companion object {
        val codec = AttributeGroup.codec(Tag.rangeOfInteger, {
            takeLength(AttributeGroup.INT_LENGTH + AttributeGroup.INT_LENGTH)
            IntRange(readInt(), readInt())
        }, {
            writeShort(AttributeGroup.INT_LENGTH + AttributeGroup.INT_LENGTH)
            writeInt(it.first)
            writeInt(it.last)
        })
    }
}
