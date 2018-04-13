// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

/** Attribute type for encoding of a [Resolution] */
class ResolutionType(tag: Tag, override val name: String) : AttributeType<Resolution>(Encoder, tag) {

    companion object Encoder : SimpleEncoder<Resolution>("resolution") {

        private const val INT_LENGTH = 4
        private const val BYTE_LENGTH = 1

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
