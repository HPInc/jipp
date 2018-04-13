// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

/**
 * An attribute bearing a string for which language is irrelevant.

 * Some string types have a length-limit.
 */
open class StringType(tag: Tag, override val name: String) : AttributeType<String>(Encoder, tag) {

    override fun of(attribute: Attribute<*>): Attribute<String>? {
        val values: List<String> = attribute.values.mapNotNull {
            when (it) {
                is String -> it
                is LangString -> it.string
                else -> null
            }
        }
        return if (values.isNotEmpty()) of(values) else null
    }

    companion object Encoder : SimpleEncoder<String>("String") {
        private const val TAG_MASK = 0x40
        @Throws(IOException::class)
        override fun writeValue(out: DataOutputStream, value: String) = out.writeString(value)

        @Throws(IOException::class)
        override fun readValue(input: DataInputStream, valueTag: Tag) = input.readString()

        override fun valid(valueTag: Tag) = valueTag.code and TAG_MASK == TAG_MASK
    }
}
