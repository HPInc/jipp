package com.hp.jipp.encoding

import com.google.common.collect.Range

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

class RangeOfIntegerType(name: String) : AttributeType<Range<Int>>(RangeOfIntegerType.ENCODER, Tag.RangeOfInteger, name) {
    companion object : IppEncodings {
        private val TYPE_NAME = "RangeOfInteger"

        @JvmField val ENCODER: Attribute.SimpleEncoder<Range<Int>> = object : Attribute.SimpleEncoder<Range<Int>>(TYPE_NAME) {
            @Throws(IOException::class)
            override fun readValue(input: DataInputStream, valueTag: Tag): Range<Int> {
                input.takeLength(8)
                val low = input.readInt()
                val high = input.readInt()
                return Range.closed(low, high)
            }

            @Throws(IOException::class)
            override fun writeValue(out: DataOutputStream, value: Range<Int>) {
                out.writeShort(8)
                out.writeInt(value.lowerEndpoint())
                out.writeInt(value.upperEndpoint())
            }

            override fun valid(valueTag: Tag): Boolean {
                return valueTag === Tag.RangeOfInteger
            }
        }
    }
}
