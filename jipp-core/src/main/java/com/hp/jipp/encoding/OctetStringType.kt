// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import java.io.IOException

/** An [AttributeType] for octet string values (binary data) */
class OctetStringType(tag: Tag, override val name: String) : AttributeType<ByteArray>(Encoder, tag) {
    companion object Encoder : SimpleEncoder<ByteArray>("octetString") {

        @Throws(IOException::class)
        override fun writeValue(out: IppOutputStream, value: ByteArray) {
            out.writeValueBytes(value)
        }

        @Throws(IOException::class)
        override fun readValue(input: IppInputStream, valueTag: Tag): ByteArray {
            return input.readValueBytes()
        }

        override fun valid(valueTag: Tag): Boolean {
            // octetString is a fallback for all types we don't otherwise understand
            return true
        }
    }
}
