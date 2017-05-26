package com.hp.jipp.encoding

import com.google.common.base.Optional
import com.google.common.collect.Lists
import com.hp.jipp.util.BuildError

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

/** An language-encoded string attribute type  */
class LangStringType(tag: Tag, name: String) : AttributeType<LangString>(LangStringType.ENCODER, tag, name) {

    override fun of(attribute: Attribute<*>): Optional<Attribute<LangString>> {
        if (!(attribute.valueTag == Tag.NameWithoutLanguage && tag == Tag.NameWithLanguage) ||
                attribute.valueTag == Tag.TextWithoutLanguage && tag == Tag.TextWithLanguage) {
            return Optional.absent<Attribute<LangString>>()
        }
        // TODO: If we don't know the language this is actually a dangerous thing to do
        // Apply conversion from StringType to a LangStringType on demand
        return Optional.of(of(attribute.values.map { LangString(it as String) }))
    }

    companion object {
        private val TYPE_NAME = "LangString"

        @JvmField
        val ENCODER: Attribute.SimpleEncoder<LangString> = object : Attribute.SimpleEncoder<LangString>(TYPE_NAME) {
            @Throws(IOException::class)
            override fun readValue(input: DataInputStream, valueTag: Tag): LangString {
                val bytes = OctetStringType.ENCODER.readValue(input, valueTag)
                val inBytes = DataInputStream(ByteArrayInputStream(bytes))

                val lang = inBytes.readString()
                val string = inBytes.readString()
                return LangString(string, lang)
            }

            @Throws(IOException::class)
            override fun writeValue(out: DataOutputStream, value: LangString) {
                val bytesOut = ByteArrayOutputStream()
                val dataOut = DataOutputStream(bytesOut)
                val lang = value.lang
                if (!lang.isPresent) {
                    throw BuildError("Cannot write a LangString without a language")
                }
                dataOut.writeString(lang.get())
                dataOut.writeString(value.string)
                OctetStringType.ENCODER.writeValue(out, bytesOut.toByteArray())
            }

            override fun valid(valueTag: Tag): Boolean {
                return valueTag === Tag.NameWithLanguage || valueTag === Tag.TextWithLanguage
            }
        }
    }
}
