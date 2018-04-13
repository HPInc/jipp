// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

/** Attribute type for [IntRange] attributes */
class RangeOfIntegerType(override val name: String) :
        AttributeType<IntRange>(Encoder, Tag.rangeOfInteger) {

    companion object Encoder : SimpleEncoder<IntRange>("rangeOfInteger") {
        @Throws(IOException::class)
        override fun readValue(input: DataInputStream, valueTag: Tag): IntRange {
            input.takeLength(INT_LEN + INT_LEN)
            val low = input.readInt()
            val high = input.readInt()
            return IntRange(low, high) // vs Closed range
        }

        @Throws(IOException::class)
        override fun writeValue(out: DataOutputStream, value: IntRange) {
            out.writeShort(INT_LEN + INT_LEN)
            out.writeInt(value.first)
            out.writeInt(value.last)
        }
        override fun valid(valueTag: Tag) = valueTag === Tag.rangeOfInteger
    }
}
