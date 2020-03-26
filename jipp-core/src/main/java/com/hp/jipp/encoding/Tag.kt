// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/**
 * Value and delimiter tags as specified by RFC2910 and RFC3382
 */
@Suppress("MagicNumber")
abstract class Tag : Enum() {

    /** Return true if this tag is a delimiter tag  */
    val isDelimiter: Boolean
        get() = code in delimiterRange

    /** Return true if this tag is an out-of-band tag */
    val isOutOfBand: Boolean
        get() = code in outOfBandRange

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

    /** Identify tags that indicate the current attribute has no more values */
    internal fun isEndOfValueStream() =
        isDelimiter || isOutOfBand || this == memberAttributeName || this == endCollection

    companion object {
        private val delimiterRange = 0x01..0x0F
        private val outOfBandRange = 0x10..0x1F

        /** Return or create a [Tag] for the supplied code */
        @JvmStatic
        fun fromInt(value: Int): Tag =
            codeMap[value] ?: run {
                when (value) {
                    in delimiterRange -> DelimiterTag(value, "tag(x%x)".format(value))
                    in outOfBandRange -> OutOfBandTag(value, "tag(x%x)".format(value))
                    else -> ValueTag(value, "tag(x%x)".format(value))
                }
            }

        // Delimiter tags
        @JvmField val operationAttributes = DelimiterTag(0x01, "operation-attributes")
        @JvmField val jobAttributes = DelimiterTag(0x02, "job-attributes")
        @JvmField val endOfAttributes = DelimiterTag(0x03, "end-of-attributes")
        @JvmField val printerAttributes = DelimiterTag(0x04, "printer-attributes")
        @JvmField val unsupportedAttributes = DelimiterTag(0x05, "unsupported-attributes")

        // "Out-of-band" values
        @JvmField val unsupported = OutOfBandTag(0x10, "unsupported")
        @JvmField val unknown = OutOfBandTag(0x12, "unknown")
        @JvmField val noValue = OutOfBandTag(0x13, "no-value")
        @JvmField val notSettable = OutOfBandTag(0x15, "not-settable")
        @JvmField val deleteAttribute = OutOfBandTag(0x16, "delete-attribute")
        @JvmField val adminDefine = OutOfBandTag(0x17, "admin-define")

        // Integer values
        @JvmField val integerValue = ValueTag(0x21, "integer")
        @JvmField val booleanValue = ValueTag(0x22, "boolean")
        @JvmField val enumValue = ValueTag(0x23, "enum")

        // Octet-string values
        @JvmField val octetString = ValueTag(0x30, "octetString")
        @JvmField val dateTime = ValueTag(0x31, "dateTime")
        @JvmField val resolution = ValueTag(0x32, "resolution")
        @JvmField val rangeOfInteger = ValueTag(0x33, "rangeOfInteger")
        @JvmField val beginCollection = ValueTag(0x34, "begCollection")
        @JvmField val textWithLanguage = ValueTag(0x35, "textWithLanguage")
        @JvmField val nameWithLanguage = ValueTag(0x36, "nameWithLanguage")
        @JvmField val endCollection = ValueTag(0x37, "endCollection")

        // Character-string values
        @JvmField val textWithoutLanguage = ValueTag(0x41, "textWithoutLanguage")
        @JvmField val nameWithoutLanguage = ValueTag(0x42, "nameWithoutLanguage")
        @JvmField val keyword = ValueTag(0x44, "keyword")
        @JvmField val uri = ValueTag(0x45, "uri")
        @JvmField val uriScheme = ValueTag(0x46, "uriScheme")
        @JvmField val charset = ValueTag(0x47, "charset")
        @JvmField val naturalLanguage = ValueTag(0x48, "naturalLanguage")
        @JvmField val mimeMediaType = ValueTag(0x49, "mimeMediaType")
        @JvmField val memberAttributeName = ValueTag(0x4A, "memberAttrName")

        @JvmField
        val delimiterTags = listOf(
            operationAttributes, jobAttributes, endOfAttributes, printerAttributes, unsupportedAttributes
        )

        @JvmField
        val outOfBandTag = listOf(
            unsupported, unknown, noValue, notSettable, deleteAttribute, adminDefine
        )

        @JvmField
        val valueTags = listOf(
            integerValue, booleanValue, enumValue,
            octetString, dateTime, resolution, rangeOfInteger, beginCollection, textWithLanguage, nameWithLanguage,
            endCollection,
            textWithoutLanguage, nameWithoutLanguage, keyword, uri, uriScheme, charset, naturalLanguage, mimeMediaType,
            memberAttributeName
        )

        /** All known [Tag] values. */
        @JvmField
        val all = delimiterTags + outOfBandTag + valueTags

        private val codeMap: Map<Int, Tag> = toCodeMap(all)
    }
}
