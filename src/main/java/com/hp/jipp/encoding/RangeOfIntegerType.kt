package com.hp.jipp.encoding

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

class RangeOfIntegerType(name: String) :
        AttributeType<IntRange>(ENCODER, Tag.RangeOfInteger, name) {
    companion object {
        private val TYPE_NAME = "RangeOfInteger"

        @JvmField val ENCODER: Attribute.SimpleEncoder<IntRange> = object : Attribute.SimpleEncoder<IntRange>(TYPE_NAME) {
            @Throws(IOException::class)
            override fun readValue(input: DataInputStream, valueTag: Tag): IntRange {
                input.takeLength(8)
                val low = input.readInt()
                val high = input.readInt()
                return IntRange(low, high) // vs Closed range
            }

            @Throws(IOException::class)
            override fun writeValue(out: DataOutputStream, value: IntRange) {
                out.writeShort(8)
                out.writeInt(value.first)
                out.writeInt(value.last)
            }
            override fun valid(valueTag: Tag) = valueTag === Tag.RangeOfInteger
        }
    }
}
