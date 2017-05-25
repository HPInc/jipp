package com.hp.jipp.encoding

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

class ResolutionType(tag: Tag, name: String) : AttributeType<Resolution>(ENCODER, tag, name) {

    companion object : IppEncodings {
        private val TYPE_NAME = "Resolution"

        private val INT_LENGTH = 4
        private val BYTE_LENGTH = 1

        @JvmField
        val ENCODER: Attribute.SimpleEncoder<Resolution> = object : Attribute.SimpleEncoder<Resolution>(TYPE_NAME) {
            @Throws(IOException::class)
            override fun readValue(input: DataInputStream, valueTag: Tag): Resolution {
                input.takeLength(INT_LENGTH + INT_LENGTH + BYTE_LENGTH)
                return Resolution(input.readInt(), input.readInt(),
                        ResolutionUnit.ENCODER.get(input.readByte().toInt()))
            }

            @Throws(IOException::class)
            override fun writeValue(out: DataOutputStream, value: Resolution) {
                out.writeShort(INT_LENGTH + INT_LENGTH + BYTE_LENGTH)
                out.writeInt(value.crossFeedResolution)
                out.writeInt(value.feedResolution)
                out.writeByte(value.unit.code.toByte().toInt())
            }

            override fun valid(valueTag: Tag) = Tag.Resolution == valueTag
        }
    }
}
