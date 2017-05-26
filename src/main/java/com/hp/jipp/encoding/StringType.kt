package com.hp.jipp.encoding

import com.google.common.base.Optional

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

/**
 * An attribute bearing a string for which language is irrelevant.

 * Some string types have a length-limit.
 */
class StringType(tag: Tag, name: String) : AttributeType<String>(StringType.ENCODER, tag, name) {

    // TODO: Return ? type
    override fun of(attribute: Attribute<*>): Optional<Attribute<String>> {
        if (!(attribute.valueTag == Tag.NameWithLanguage && tag == Tag.NameWithoutLanguage) ||
                attribute.valueTag == Tag.TextWithLanguage && tag == Tag.TextWithoutLanguage) {
            return Optional.absent<Attribute<String>>()
        }
        return Optional.of(of(attribute.values.map { (it as LangString).string }))
    }

    companion object {
        private val TYPE_NAME = "String"

        @JvmField
        val ENCODER: Attribute.SimpleEncoder<String> = object : Attribute.SimpleEncoder<String>(TYPE_NAME) {
            @Throws(IOException::class)
            override fun writeValue(out: DataOutputStream, value: String) = out.writeString(value)

            @Throws(IOException::class)
            override fun readValue(input: DataInputStream, valueTag: Tag) = input.readString()

            override fun valid(valueTag: Tag) = valueTag.code and 0x40 == 0x40
        }
    }
}
