package com.hp.jipp.encoding

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

/**
 * An attribute bearing a string for which language is irrelevant.

 * Some string types have a length-limit.
 */
class StringType(tag: Tag, name: String) : AttributeType<String>(StringType.ENCODER, tag, name) {

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

    companion object {
        private val TYPE_NAME = "String"
        private val TAG_MASK = 0x40

        @JvmField
        val ENCODER: SimpleEncoder<String> = object : SimpleEncoder<String>(TYPE_NAME) {
            @Throws(IOException::class)
            override fun writeValue(out: DataOutputStream, value: String) = out.writeString(value)

            @Throws(IOException::class)
            override fun readValue(input: DataInputStream, valueTag: Tag) = input.readString()

            override fun valid(valueTag: Tag) = valueTag.code and TAG_MASK == TAG_MASK
        }
    }
}
