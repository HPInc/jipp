// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

/**
 * Value and delimiter tags as specified by RFC2910 and RFC3382
 */
@Suppress("MagicNumber")
data class Tag(override val code: Int, override val name: String) : Enum() {

    /** Return true if this tag is a delimiter tag  */
    val isDelimiter: Boolean
        get() = code in 0x01..0x0F

    /** Return true if this tag is an out-of-band tag */
    val isOutOfBand: Boolean
        get() = code in 0x10..0x1F

    val isCollection: Boolean
        get() = this == beginCollection || this == endCollection || this == memberAttributeName

    /** Return true if this tag is encoded as an integer */
    val isInteger: Boolean
        get() = code in 0x20..0x2F

    /** Return true if this tag is encoded as an octet string */
    val isOctetString: Boolean
        get() = code in 0x30..0x3F

    /** Return true if this tag is encoded as a character string */
    val isCharString: Boolean
        get() = code in 0x40..0x4F

    override fun toString() = name

    /** Write this tag to the output stream  */
    @Throws(IOException::class)
    fun write(out: DataOutputStream) {
        out.writeByte(code.toByte().toInt())
    }

    companion object {
        /** Read and return a [Tag] from the input stream  */
        @JvmStatic
        fun read(input: DataInputStream): Tag = fromInt(input.readByte().toInt())

        /** Return or create a [Tag] for the supplied code */
        @JvmStatic
        fun fromInt(value: Int) = Tag.codeMap[value] ?: Tag(value, "tag(x%x)".format(value))

        // Delimiter tags
        @JvmField val operationAttributes = Tag(0x01, "operation-attributes")
        @JvmField val jobAttributes = Tag(0x02, "job-attributes")
        @JvmField val endOfAttributes = Tag(0x03, "end-of-attributes")
        @JvmField val printerAttributes = Tag(0x04, "printer-attributes")
        @JvmField val unsupportedAttributes = Tag(0x05, "unsupported-attributes")

        // "Out-of-band" values
        @JvmField val unsupported = Tag(0x10, "unsupported")
        @JvmField val unknown = Tag(0x12, "unknown")
        @JvmField val noValue = Tag(0x13, "no-value")
        @JvmField val notSettable = Tag(0x15, "not-settable")
        @JvmField val deleteAttribute = Tag(0x16, "delete-attribute")
        @JvmField val adminDefine = Tag(0x17, "admin-define")

        // Integer values
        @JvmField val integerValue = Tag(0x21, "integer")
        @JvmField val booleanValue = Tag(0x22, "boolean")
        @JvmField val enumValue = Tag(0x23, "enum")

        // Octet-string values
        @JvmField val octetString = Tag(0x30, "octetString")
        @JvmField val dateTime = Tag(0x31, "dateTime")
        @JvmField val resolution = Tag(0x32, "resolution")
        @JvmField val rangeOfInteger = Tag(0x33, "rangeOfInteger")
        @JvmField val beginCollection = Tag(0x34, "begCollection")
        @JvmField val textWithLanguage = Tag(0x35, "textWithLanguage")
        @JvmField val nameWithLanguage = Tag(0x36, "nameWithLanguage")
        @JvmField val endCollection = Tag(0x37, "endCollection")

        // Character-string values
        @JvmField val textWithoutLanguage = Tag(0x41, "textWithoutLanguage")
        @JvmField val nameWithoutLanguage = Tag(0x42, "nameWithoutLanguage")
        @JvmField val keyword = Tag(0x44, "keyword")
        @JvmField val uri = Tag(0x45, "uri")
        @JvmField val uriScheme = Tag(0x46, "uriScheme")
        @JvmField val charset = Tag(0x47, "charset")
        @JvmField val naturalLanguage = Tag(0x48, "naturalLanguage")
        @JvmField val mimeMediaType = Tag(0x49, "mimeMediaType")
        @JvmField val memberAttributeName = Tag(0x4A, "memberAttrName")

        /** All known [Tag] values. */
        @JvmField val all = Enum.allFrom<Tag>(Tag::class.java)

        private val codeMap: Map<Int, Tag> = Enum.toCodeMap(Enum.allFrom(Tag::class.java))
    }
}
