// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/** An attribute type containing [IntRange] values. */
open class IntRangeType(override val name: String) : AttributeType<IntRange> {
    override fun coerce(value: Any) =
        value as? IntRange

    override fun toString() = "IntRangeType($name)"

    companion object {
        val codec = Codec(Tag.rangeOfInteger, {
            takeLength(IppStreams.INT_LENGTH + IppStreams.INT_LENGTH)
            IntRange(readInt(), readInt())
        }, {
            writeShort(IppStreams.INT_LENGTH + IppStreams.INT_LENGTH)
            writeInt(it.first)
            writeInt(it.last)
        })
    }
}
