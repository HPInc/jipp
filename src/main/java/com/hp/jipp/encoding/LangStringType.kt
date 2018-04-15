// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.util.BuildError

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

/** An language-encoded string attribute type  */
class LangStringType(tag: Tag, override val name: String) : AttributeType<LangString>(Encoder, tag) {

    /** Return an [Attribute] of this type */
    override fun of(attribute: Attribute<*>): Attribute<LangString>? {
        if (!validInputTag(attribute.valueTag)) {
            return null
        }

        // Apply conversion from StringType to a LangStringType on demand
        // NOTE: we do not know the actual language so this may be a bad idea.
        return of(attribute.values.map { LangString(it as String) })
    }

    private fun validInputTag(fromTag: Tag): Boolean =
            when (fromTag) {
                Tag.nameWithoutLanguage -> tag == Tag.nameWithLanguage
                Tag.textWithoutLanguage -> tag == Tag.textWithLanguage
                else -> fromTag == tag
            }

    companion object Encoder : SimpleEncoder<LangString>("LangString") {
        @Throws(IOException::class)
        override fun readValue(input: DataInputStream, valueTag: Tag): LangString {
            val bytes = OctetStringType.Encoder.readValue(input, valueTag)
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
            OctetStringType.Encoder.writeValue(out, bytesOut.toByteArray())
        }

        override fun valid(valueTag: Tag): Boolean {
            return valueTag === Tag.nameWithLanguage || valueTag === Tag.textWithLanguage
        }
    }
}
