// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.util.BuildError

import java.io.IOException

/** An language-encoded string attribute type  */
class LangStringType(tag: Tag, override val name: String) : AttributeType<LangString>(Encoder, tag) {

    /** Return an [Attribute] of this type */
    override fun convert(attribute: Attribute<*>): Attribute<LangString>? {
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
        override fun readValue(input: IppInputStream, valueTag: Tag): LangString {
            // Length should exactly match lang/string total length but we will discard
            input.readShort().toInt()
            val lang = input.readString()
            val string = input.readString()
            return LangString(string, lang)
        }

        @Throws(IOException::class)
        override fun writeValue(out: IppOutputStream, value: LangString) {
            value.lang ?: throw BuildError("Cannot write a LangString without a language")
            out.writeShort(out.stringLength(value.lang) + out.stringLength(value.string))
            out.writeString(value.lang)
            out.writeString(value.string)
        }

        override fun valid(valueTag: Tag): Boolean {
            return valueTag === Tag.nameWithLanguage || valueTag === Tag.textWithLanguage
        }
    }
}
