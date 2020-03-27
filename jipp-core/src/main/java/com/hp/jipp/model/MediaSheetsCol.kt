// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT
//
// DO NOT MODIFY. Code is auto-generated by genTypes.py. Content taken from registry at
// https://www.iana.org/assignments/ipp-registrations/ipp-registrations.xml, updated on 2020-02-20
@file:Suppress("MaxLineLength", "WildcardImport")

package com.hp.jipp.model

import com.hp.jipp.encoding.* // ktlint-disable no-wildcard-imports

/**
 * Data object corresponding to a "media-sheets-col" collection as defined in:
 * [APRIL2015F2F](https://ftp.pwg.org/pub/pwg/ipp/minutes/ippv2-f2f-minutes-20150429.pdf).
 */
@Suppress("RedundantCompanionReference", "unused")
data class MediaSheetsCol
constructor(
    var blank: Int? = null,
    var fullColor: Int? = null,
    var highlightColor: Int? = null,
    var monochrome: Int? = null
) : AttributeCollection {

    /** Construct an empty [MediaSheetsCol]. */
    constructor() : this(null, null, null, null)

    /** Produce an attribute list from members. */
    override val attributes: List<Attribute<*>> by lazy {
        listOfNotNull(
            blank?.let { Types.blank.of(it) },
            fullColor?.let { Types.fullColor.of(it) },
            highlightColor?.let { Types.highlightColor.of(it) },
            monochrome?.let { Types.monochrome.of(it) }
        )
    }

    /** Type for attributes of this collection */
    class Type(override val name: String) : AttributeCollection.Type<MediaSheetsCol>(MediaSheetsCol)

    /** All member names as strings. */
    object Name {
        /** "blank" member name */
        const val blank = "blank"
        /** "full-color" member name */
        const val fullColor = "full-color"
        /** "highlight-color" member name */
        const val highlightColor = "highlight-color"
        /** "monochrome" member name */
        const val monochrome = "monochrome"
    }

    /** Types for each member attribute. */
    object Types {
        val blank = IntType(Name.blank)
        val fullColor = IntType(Name.fullColor)
        val highlightColor = IntType(Name.highlightColor)
        val monochrome = IntType(Name.monochrome)
    }

    /** Defines types for each member of [MediaSheetsCol] */
    companion object : AttributeCollection.Converter<MediaSheetsCol> {
        override fun convert(attributes: List<Attribute<*>>): MediaSheetsCol =
            MediaSheetsCol(
                extractOne(attributes, Types.blank),
                extractOne(attributes, Types.fullColor),
                extractOne(attributes, Types.highlightColor),
                extractOne(attributes, Types.monochrome)
            )
    }
    override fun toString() = "MediaSheetsCol(${attributes.joinToString()})"
}
