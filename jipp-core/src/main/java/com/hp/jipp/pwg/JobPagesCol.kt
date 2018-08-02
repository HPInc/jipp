// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT
//
// DO NOT MODIFY. Code is auto-generated by genTypes.py. Content taken from registry at
// https://www.iana.org/assignments/ipp-registrations/ipp-registrations.xml, updated on 2018-04-06
@file:Suppress("MaxLineLength", "WildcardImport")

package com.hp.jipp.pwg

import com.hp.jipp.encoding.* // ktlint-disable no-wildcard-imports

/**
 * Data object corresponding to a "job-pages-col" collection as defined in:
 * [APRIL2015F2F](http://ftp.pwg.org/pub/pwg/ipp/minutes/ippv2-f2f-minutes-20150429.pdf).
 */
@Suppress("RedundantCompanionReference", "unused")
data class JobPagesCol
@JvmOverloads constructor(
    val fullColor: Int? = null,
    val monochrome: Int? = null,
    /** Encoded form, if known. */
    val _encoded: List<Attribute<*>>? = null
) : AttributeCollection {

    /** Produce an attribute list from members, or return the original [_encoded] attribute list if present. */
    override val attributes: List<Attribute<*>> by lazy {
        _encoded ?: listOfNotNull(
            fullColor?.let { Members.fullColor.of(it) },
            monochrome?.let { Members.monochrome.of(it) }
        )
    }

    /** Type for attributes of this collection */
    class Type(override val name: String) : AttributeCollection.Type<JobPagesCol>(Members)

    /** All member names as strings. */
    object Name {
        /** "full-color" member name */
        const val fullColor = "full-color"
        /** "monochrome" member name */
        const val monochrome = "monochrome"
    }

    /** Builder for immutable [JobPagesCol] objects. */
    class Builder() {
        /** Constructs a new [Builder] pre-initialized with values in [source]. */
        constructor(source: JobPagesCol) : this() {
            fullColor = source.fullColor
            monochrome = source.monochrome
        }
        var fullColor: Int? = null
        var monochrome: Int? = null

        /** Return a new [JobPagesCol] object containing all values initialized in this builder. */
        fun build() = JobPagesCol(
            fullColor,
            monochrome
        )
    }

    companion object Members : AttributeCollection.Converter<JobPagesCol> {
        override fun convert(attributes: List<Attribute<*>>): JobPagesCol =
            JobPagesCol(
                extractOne(attributes, fullColor),
                extractOne(attributes, monochrome),
                _encoded = attributes)
        /**
         * "full-color" member type.
         */
        @JvmField val fullColor = IntType(Name.fullColor)
        /**
         * "monochrome" member type.
         */
        @JvmField val monochrome = IntType(Name.monochrome)
    }
}
