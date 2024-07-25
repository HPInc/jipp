// Copyright 2017 - 2023 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/**
 * Value and delimiter tags as specified by [RFC8010](https://tools.ietf.org/html/rfc8010) and
 * [RFC8011](https://tools.ietf.org/html/rfc8010).
 */
@Suppress("MagicNumber")
abstract class Tag : Enum() {

    /** True if this tag is used to delimit attribute groups. */
    val isDelimiter: Boolean
        get() = code in delimiterRange

    /** True if this tag indicates an out-of-band attribute (having no value). */
    val isOutOfBand: Boolean
        get() = code in outOfBandRange

    /** True if this [Tag] is used to encode collections. */
    val isCollection: Boolean
        get() = this == beginCollection || this == endCollection || this == memberAttributeName

    /** True if this tag denotes a value encoded as an integer. */
    val isInteger: Boolean
        get() = code in 0x20..0x2F

    /** True if this tag denotes a value encoded as an octet string. */
    val isOctetString: Boolean
        get() = code in 0x30..0x3F

    /** True if this tag denotes a value encoded as a character string. */
    val isCharString: Boolean
        get() = code in 0x40..0x4F

    /** True if this tag denotes an attribute with no more values. */
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
        @JvmField val subscriptionAttributes = DelimiterTag(0x06, "subscription-attributes-tag")
        @JvmField val eventNotificationAttributes = DelimiterTag(0x07, "event-notification-attributes-tag")
        @JvmField val resourceAttributes = DelimiterTag(0x08, "resource-attributes-tag")
        @JvmField val documentAttributes = DelimiterTag(0x09, "document-attributes-tag")
        @JvmField val systemAttributes = DelimiterTag(0x0A, "system-attributes-tag")

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
            operationAttributes, jobAttributes, endOfAttributes, printerAttributes, unsupportedAttributes,
            subscriptionAttributes, eventNotificationAttributes, resourceAttributes, documentAttributes,
            systemAttributes
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
