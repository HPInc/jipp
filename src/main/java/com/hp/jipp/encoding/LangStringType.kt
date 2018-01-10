package com.hp.jipp.encoding

import com.hp.jipp.util.BuildError

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

/** An language-encoded string attribute type  */
class LangStringType(tag: Tag, name: String) : AttributeType<LangString>(LangStringType.ENCODER, tag, name) {

    /** Return an [Attribute] of this type */
    override fun of(attribute: Attribute<*>): Attribute<LangString>? {
        if (!validInputTag(attribute.valueTag)) {
            return null
        }

        // TODO: If we don't know the language this is actually a dangerous thing to do
        // Apply conversion from StringType to a LangStringType on demand
        return of(attribute.values.map { LangString(it as String) })
    }

    private fun validInputTag(fromTag: Tag): Boolean =
            when (fromTag) {
                Tag.nameWithoutLanguage -> tag == Tag.nameWithLanguage
                Tag.textWithoutLanguage -> tag == Tag.textWithLanguage
                else -> fromTag == tag
            }

    companion object {
        private val TYPE_NAME = "LangString"

        @JvmField
        val ENCODER: SimpleEncoder<LangString> = object : SimpleEncoder<LangString>(TYPE_NAME) {
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
                value.lang ?: throw BuildError("Cannot write a LangString without a language")
                dataOut.writeString(value.lang)
                dataOut.writeString(value.string)
                OctetStringType.ENCODER.writeValue(out, bytesOut.toByteArray())
            }

            override fun valid(valueTag: Tag): Boolean {
                return valueTag === Tag.nameWithLanguage || valueTag === Tag.textWithLanguage
            }
        }
    }
}
