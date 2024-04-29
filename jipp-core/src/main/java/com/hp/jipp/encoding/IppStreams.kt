// Copyright 2020 - 2021 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/** Utility object for reading/writing IPP protocol elements. */
internal object IppStreams {
    const val INT_LENGTH = 4
    const val LENGTH_LENGTH = 2
    const val BYTE_LENGTH = 1
    const val TAG_LEN = 2

    /** Return the length of this string as it would be encoded in the output stream. */
    fun stringLength(string: String) = LENGTH_LENGTH + string.toByteArray(Charsets.UTF_8).size

    /** Codecs for core types. */
    val codecs =
        listOf(
            IntType.codec,
            BooleanType.codec,
            EnumType.codec,
            Codec(
                Tag.octetString,
                {
                    readValueBytes()
                },
                {
                    writeBytesValue(it)
                }
            ),
            DateTimeType.codec,
            ResolutionType.codec,
            IntRangeType.codec,
            IntOrIntRangeType.codec,
            CollectionType.codec,
            TextType.codec,
            NameType.codec,
            OctetsType.codec,
            KeyValues.codec,
            Codec(
                { it.isOctetString || it.isInteger },
                { tag ->
                    // Used when we don't know how to interpret the content. Even with integers,
                    // we don't know whether to expect a short or byte or int or whatever.
                    OtherOctets(tag, readValueBytes())
                },
                {
                    writeBytesValue(it.value)
                }
            ),
            KeywordType.codec,
            KeywordOrNameType.codec, // Must follow both Keyword and Name
            UriType.codec,
            Codec(
                { it.isCharString },
                { tag ->
                    // Handle other harder-to-type values here:
                    // uriScheme, naturalLanguage, mimeMediaType, charset etc.
                    OtherString(tag, readString())
                },
                {
                    writeStringValue(it.value)
                }
            )
        )

    /** Map for looking up codecs by native Java class. */
    val clsToCodec = codecs.map { it.cls to it }.toMap()

    /** Map for looking up codecs by [Tag]. */
    val tagToCodec = Tag.valueTags.map { tag -> tag to codecs.firstOrNull { it.handlesTag(tag) } }
        .filter { it.second != null }
        .toMap()
}
