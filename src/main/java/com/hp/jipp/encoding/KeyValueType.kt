package com.hp.jipp.encoding

import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.*

class KeyValueType(name: String) : AttributeType<Map<String, String>>(ENCODER, Tag.OctetString, name) {
    companion object {
        @JvmField val ENCODER = object : SimpleEncoder<Map<String, String>>("KeyValue") {
            val ELEMENT_SEPARATOR = ";"
            val PART_SEPARATOR = "="

            override fun writeValue(out: DataOutputStream, value: Map<String, String>) =
                StringType.ENCODER.writeValue(out, encode(value))

            internal fun encode(input: Map<String, String>): String {
                val out = StringBuilder()
                input.forEach {
                    out.append(it.key)
                    out.append(PART_SEPARATOR)
                    out.append(it.value)
                    out.append(ELEMENT_SEPARATOR)
                }
                return out.toString()
            }

            override fun valid(valueTag: Tag) = valueTag == Tag.OctetString

            override fun readValue(input: DataInputStream, valueTag: Tag): Map<String, String> =
                    decode(StringType.ENCODER.readValue(input, valueTag))

            internal fun decode(input: String): Map<String, String> =
                input.split(ELEMENT_SEPARATOR).mapNotNull {
                    val parts = it.split(PART_SEPARATOR)
                    if (parts.size == 2 && parts[0].isNotEmpty() && parts[1].isNotEmpty()) {
                        return@mapNotNull parts[0] to parts[1]
                    } else {
                        return@mapNotNull null
                    }
                }.toMap()
        }
    }

    // Include these to allow java to see the correct types instead of Map<String, ? extends String>

    override fun of(vararg values: Map<String, String>): Attribute<Map<String, String>> =
            super.of(Arrays.asList(*values))

    override fun of(values: List<Map<String, String>>): Attribute<Map<String, String>> =
            super.of(values)
}
