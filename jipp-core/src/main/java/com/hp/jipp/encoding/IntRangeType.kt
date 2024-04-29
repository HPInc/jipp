// Copyright 2017 - 2021 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/** An [AttributeType] for an [IntRange] value. */
open class IntRangeType(name: String) : AttributeTypeImpl<IntRange>(name, IntRange::class.java) {
    /** An [AttributeType] for multiple [IntRange] values. */
    class Set(name: String) : IntRangeType(name), AttributeSetType<IntRange> {
        override fun toString() = "IntRangeType.Set($name)"
    }

    companion object {
        val codec = Codec(
            Tag.rangeOfInteger,
            {
                takeLength(IppStreams.INT_LENGTH + IppStreams.INT_LENGTH)
                IntRange(readInt(), readInt())
            },
            {
                writeShort(IppStreams.INT_LENGTH + IppStreams.INT_LENGTH)
                writeInt(it.first)
                writeInt(it.last)
            }
        )
    }
}
