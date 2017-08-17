package com.hp.jipp.encoding

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

/** Attribute type for encoding of a [Resolution] */
class ResolutionType(tag: Tag, name: String) : AttributeType<Resolution>(ENCODER, tag, name) {

    companion object {
        private val TYPE_NAME = "resolution"

        private val INT_LENGTH = 4
        private val BYTE_LENGTH = 1

        @JvmField
        val ENCODER: SimpleEncoder<Resolution> = object : SimpleEncoder<Resolution>(TYPE_NAME) {
            @Throws(IOException::class)
            override fun readValue(input: DataInputStream, valueTag: Tag): Resolution {
                input.takeLength(INT_LENGTH + INT_LENGTH + BYTE_LENGTH)
                return Resolution(input.readInt(), input.readInt(),
                        ResolutionUnit.ENCODER[input.readByte().toInt()])
            }

            @Throws(IOException::class)
            override fun writeValue(out: DataOutputStream, value: Resolution) {
                out.writeShort(INT_LENGTH + INT_LENGTH + BYTE_LENGTH)
                out.writeInt(value.crossFeedResolution)
                out.writeInt(value.feedResolution)
                out.writeByte(value.unit.code.toByte().toInt())
            }

            override fun valid(valueTag: Tag) = Tag.resolution == valueTag
        }
    }
}
