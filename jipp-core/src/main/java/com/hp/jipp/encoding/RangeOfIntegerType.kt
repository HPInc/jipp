// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.encoding.IntegerType.Encoder.INT_LENGTH
import java.io.IOException

/** Attribute type for [IntRange] attributes */
class RangeOfIntegerType(override val name: String) :
        AttributeType<IntRange>(Encoder, Tag.rangeOfInteger) {

    companion object Encoder : SimpleEncoder<IntRange>("rangeOfInteger") {
        @Throws(IOException::class)
        override fun readValue(input: IppInputStream, valueTag: Tag): IntRange {
            input.takeLength(INT_LENGTH + INT_LENGTH)
            val low = input.readInt()
            val high = input.readInt()
            return IntRange(low, high) // vs Closed range
        }

        @Throws(IOException::class)
        override fun writeValue(out: IppOutputStream, value: IntRange) {
            out.writeShort(INT_LENGTH + INT_LENGTH)
            out.writeInt(value.first)
            out.writeInt(value.last)
        }
        override fun valid(valueTag: Tag) = valueTag === Tag.rangeOfInteger
    }
}
