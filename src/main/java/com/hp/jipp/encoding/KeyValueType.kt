// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import java.io.DataInputStream
import java.io.DataOutputStream

/** Attribute type for Key/Value pairs */
class KeyValueType(override val name: String) : AttributeType<Map<String, String>>(Encoder, Tag.octetString) {
    companion object Encoder : SimpleEncoder<Map<String, String>>("KeyValue") {
        private const val ELEMENT_SEPARATOR = ";"
        private const val PART_SEPARATOR = "="

        override fun writeValue(out: DataOutputStream, value: Map<String, String>) =
                StringType.Encoder.writeValue(out, encode(value))

        private fun encode(input: Map<String, String>): String {
            val out = StringBuilder()
            input.forEach {
                out.append(it.key)
                out.append(PART_SEPARATOR)
                out.append(it.value)
                out.append(ELEMENT_SEPARATOR)
            }
            return out.toString()
        }

        override fun valid(valueTag: Tag) = valueTag == Tag.octetString

        override fun readValue(input: DataInputStream, valueTag: Tag): Map<String, String> =
                decode(StringType.Encoder.readValue(input, valueTag))

        private fun decode(input: String): Map<String, String> =
                input.split(ELEMENT_SEPARATOR)
                        .map { it.split(PART_SEPARATOR) }
                        .filter { it.size == 2 && it[0].isNotEmpty() && it[1].isNotEmpty() }
                        .map { it[0] to it[1] }
                        .toMap()
    }

    // Include these to allow java to see the correct types instead of Map<String, ? extends String>

    override fun of(value: Map<String, String>, vararg values: Map<String, String>): Attribute<Map<String, String>> =
            super.invoke(listOf(value) + values.toList())

    override fun of(values: List<Map<String, String>>): Attribute<Map<String, String>> =
            super.invoke(values)
}
