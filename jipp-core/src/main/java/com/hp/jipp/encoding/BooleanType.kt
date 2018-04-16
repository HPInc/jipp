// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

/** A boolean attribute type */
class BooleanType(override val name: String) : AttributeType<Boolean>(Encoder, Tag.booleanValue) {
    companion object Encoder : SimpleEncoder<Boolean>("Boolean") {
        @Throws(IOException::class)
        override fun writeValue(out: IppOutputStream, value: Boolean) {
            out.writeShort(1)
            out.writeByte(if (value) 0x01 else 0x00)
        }

        @Throws(IOException::class)
        override fun readValue(input: IppInputStream, valueTag: Tag): Boolean {
            input.takeLength(1)
            return input.readByte().toInt() != 0
        }

        override fun valid(valueTag: Tag): Boolean {
            return valueTag === Tag.booleanValue
        }
    }
}
