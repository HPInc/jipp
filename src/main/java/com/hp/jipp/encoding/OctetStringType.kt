package com.hp.jipp.encoding

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

class OctetStringType(tag: Tag, name: String) : AttributeType<ByteArray>(OctetStringType.ENCODER, tag, name) {
    companion object {
        private val TYPE_NAME = "OctetString"

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
                // OctetString is a fallback for all types we don't otherwise understand
                return true
            }
        }
    }
}
