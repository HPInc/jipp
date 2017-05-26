package com.hp.jipp.encoding

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

class IntegerType(tag: Tag, name: String) : AttributeType<Int>(IntegerType.ENCODER, tag, name) {
    companion object {
        private val TYPE_NAME = "Integer"

        @JvmField val ENCODER: SimpleEncoder<Int> = object : SimpleEncoder<Int>(TYPE_NAME) {
            @Throws(IOException::class)
            override fun readValue(input: DataInputStream, valueTag: Tag): Int {
                input.takeLength(4)
                return input.readInt()
            }

            @Throws(IOException::class)
            override fun writeValue(out: DataOutputStream, value: Int) {
                out.writeShort(4)
                out.writeInt(value)
            }

            override fun valid(valueTag: Tag): Boolean {
                return Tag.IntegerValue == valueTag || Tag.EnumValue == valueTag
            }
        }
    }
}
