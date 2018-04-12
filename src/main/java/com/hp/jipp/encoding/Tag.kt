// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

/**
 * Value and delimiter tags as specified by RFC2910 and RFC3382
 */
data class Tag(override val name: String, override val code: Int) : Enum() {

    /** Return true if this tag is a delimiter tag  */
    val isDelimiter: Boolean
        get() = code in 0x01..0x0F

    override fun toString() = name

    companion object {

        // Delimiter tags
        @JvmField val operationAttributes = Tag("operation-attributes", 0x01)
        @JvmField val jobAttributes = Tag("job-attributes", 0x02)
        @JvmField val endOfAttributes = Tag("end-of-attributes", 0x03)
        @JvmField val printerAttributes = Tag("printer-attributes", 0x04)
        @JvmField val unsupportedAttributes = Tag("unsupported-attributes", 0x05)

        @JvmField val unsupported = Tag("unsupported", 0x10)
        @JvmField val unknown = Tag("unknown", 0x12)
        @JvmField val noValue = Tag("no-value", 0x13)

        // Integer values
        @JvmField val integerValue = Tag("integer", 0x21)
        @JvmField val booleanValue = Tag("boolean", 0x22)
        @JvmField val enumValue = Tag("enum", 0x23)

        // Octet-string values
        @JvmField val octetString = Tag("octetString", 0x30)
        @JvmField val dateTime = Tag("dateTime", 0x31)
        @JvmField val resolution = Tag("resolution", 0x32)
        @JvmField val rangeOfInteger = Tag("rangeOfInteger", 0x33)
        @JvmField val beginCollection = Tag("begCollection", 0x34)
        @JvmField val textWithLanguage = Tag("textWithLanguage", 0x35)
        @JvmField val nameWithLanguage = Tag("nameWithLanguage", 0x36)
        @JvmField val endCollection = Tag("endCollection", 0x37)

        // Character-string values
        @JvmField val textWithoutLanguage = Tag("textWithoutLanguage", 0x41)
        @JvmField val nameWithoutLanguage = Tag("nameWithoutLanguage", 0x42)
        @JvmField val keyword = Tag("keyword", 0x44)
        @JvmField val uri = Tag("uri", 0x45)
        @JvmField val uriScheme = Tag("uriScheme", 0x46)
        @JvmField val charset = Tag("charset", 0x47)
        @JvmField val naturalLanguage = Tag("naturalLanguage", 0x48)
        @JvmField val mimeMediaType = Tag("mimeMediaType", 0x49)
        @JvmField val memberAttributeName = Tag("memberAttrName", 0x4A)

        internal val codeMap: Map<Int, Tag> = Enum.toCodeMap(Enum.allFrom(Tag::class.java))
    }
}

/** Read and return a [Tag] from the input stream  */
fun DataInputStream.readTag(): Tag = readByte().toInt().toTag()

/** Write this tag to the output stream  */
@Throws(IOException::class)
fun DataOutputStream.writeTag(tag: Tag) {
    writeByte(tag.code.toByte().toInt())
}

/** Return or create a [Tag] for the supplied code */
fun Int.toTag() = Tag.codeMap[this] ?: Tag("tag(x%x)".format(this), this)
