package com.hp.jipp.encoding

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

/** Attribute type for [IntRange] attributes */
class RangeOfIntegerType(override val name: String) :
        AttributeType<IntRange>(ENCODER, Tag.rangeOfInteger) {
    companion object {
        private const val TYPE_NAME = "rangeOfInteger"

        @JvmField val ENCODER: SimpleEncoder<IntRange> = object : SimpleEncoder<IntRange>(TYPE_NAME) {
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
}
