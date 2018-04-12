// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

/** An [AttributeType] for octet string values (binary data) */
class OctetStringType(tag: Tag, override val name: String) : AttributeType<ByteArray>(OctetStringType.ENCODER, tag) {
    companion object {
        private val TYPE_NAME = "octetString"

        @JvmField
        val ENCODER: SimpleEncoder<ByteArray> = object : SimpleEncoder<ByteArray>(TYPE_NAME) {

            @Throws(IOException::class)
            override fun writeValue(out: DataOutputStream, value: ByteArray) {
                out.writeValueBytes(value)
            }

            @Throws(IOException::class)
            override fun readValue(input: DataInputStream, valueTag: Tag): ByteArray {
                return input.readValueBytes()
            }

            override fun valid(valueTag: Tag): Boolean {
                // octetString is a fallback for all types we don't otherwise understand
                return true
            }
        }
    }
}
