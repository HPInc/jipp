// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT
//
// DO NOT MODIFY. Code is auto-generated by genTypes.py. Content taken from registry at
// https://www.iana.org/assignments/ipp-registrations/ipp-registrations.xml, updated on 2018-04-06
@file:Suppress("MaxLineLength", "WildcardImport")

package com.hp.jipp.pwg

import com.hp.jipp.encoding.* // ktlint-disable no-wildcard-imports

/**
 * Data object corresponding to a "job-impressions-col" collection as defined in:
 * [APRIL2015F2F](http://ftp.pwg.org/pub/pwg/ipp/minutes/ippv2-f2f-minutes-20150429.pdf).
 */
@Suppress("RedundantCompanionReference", "unused")
data class JobImpressionsCol
@JvmOverloads constructor(
    val blank: Int? = null,
    val blankTwoSided: Int? = null,
    val fullColor: Int? = null,
    val fullColorTwoSided: Int? = null,
    val highlightColor: Int? = null,
    val highlightColorTwoSided: Int? = null,
    val monochrome: Int? = null,
    val monochromeTwoSided: Int? = null,
    /** Encoded form, if known. */
    val _encoded: List<Attribute<*>>? = null
) : AttributeCollection {

    /** Produce an attribute list from members, or return the original [_encoded] attribute list if present. */
    override val attributes: List<Attribute<*>> by lazy {
        _encoded ?: listOfNotNull(
            blank?.let { Members.blank.of(it) },
            blankTwoSided?.let { Members.blankTwoSided.of(it) },
            fullColor?.let { Members.fullColor.of(it) },
            fullColorTwoSided?.let { Members.fullColorTwoSided.of(it) },
            highlightColor?.let { Members.highlightColor.of(it) },
            highlightColorTwoSided?.let { Members.highlightColorTwoSided.of(it) },
            monochrome?.let { Members.monochrome.of(it) },
            monochromeTwoSided?.let { Members.monochromeTwoSided.of(it) }
        )
    }

    /** Type for attributes of this collection */
    class Type(override val name: String) : AttributeCollection.Type<JobImpressionsCol>(Members)

    /** All member names as strings. */
    object Name {
        /** "blank" member name */
        const val blank = "blank"
        /** "blank-two-sided" member name */
        const val blankTwoSided = "blank-two-sided"
        /** "full-color" member name */
        const val fullColor = "full-color"
        /** "full-color-two-sided" member name */
        const val fullColorTwoSided = "full-color-two-sided"
        /** "highlight-color" member name */
        const val highlightColor = "highlight-color"
        /** "highlight-color-two-sided" member name */
        const val highlightColorTwoSided = "highlight-color-two-sided"
        /** "monochrome" member name */
        const val monochrome = "monochrome"
        /** "monochrome-two-sided" member name */
        const val monochromeTwoSided = "monochrome-two-sided"
    }

    /** Builder for immutable [JobImpressionsCol] objects. */
    class Builder() {
        /** Constructs a new [Builder] pre-initialized with values in [source]. */
        constructor(source: JobImpressionsCol) : this() {
            blank = source.blank
            blankTwoSided = source.blankTwoSided
            fullColor = source.fullColor
            fullColorTwoSided = source.fullColorTwoSided
            highlightColor = source.highlightColor
            highlightColorTwoSided = source.highlightColorTwoSided
            monochrome = source.monochrome
            monochromeTwoSided = source.monochromeTwoSided
        }
        var blank: Int? = null
        var blankTwoSided: Int? = null
        var fullColor: Int? = null
        var fullColorTwoSided: Int? = null
        var highlightColor: Int? = null
        var highlightColorTwoSided: Int? = null
        var monochrome: Int? = null
        var monochromeTwoSided: Int? = null

        /** Return a new [JobImpressionsCol] object containing all values initialized in this builder. */
        fun build() = JobImpressionsCol(
            blank,
            blankTwoSided,
            fullColor,
            fullColorTwoSided,
            highlightColor,
            highlightColorTwoSided,
            monochrome,
            monochromeTwoSided
        )
    }

    companion object Members : AttributeCollection.Converter<JobImpressionsCol> {
        override fun convert(attributes: List<Attribute<*>>): JobImpressionsCol =
            JobImpressionsCol(
                extractOne(attributes, blank),
                extractOne(attributes, blankTwoSided),
                extractOne(attributes, fullColor),
                extractOne(attributes, fullColorTwoSided),
                extractOne(attributes, highlightColor),
                extractOne(attributes, highlightColorTwoSided),
                extractOne(attributes, monochrome),
                extractOne(attributes, monochromeTwoSided),
                _encoded = attributes)
        /**
         * "blank" member type.
         */
        @JvmField val blank = IntType(Name.blank)
        /**
         * "blank-two-sided" member type.
         */
        @JvmField val blankTwoSided = IntType(Name.blankTwoSided)
        /**
         * "full-color" member type.
         */
        @JvmField val fullColor = IntType(Name.fullColor)
        /**
         * "full-color-two-sided" member type.
         */
        @JvmField val fullColorTwoSided = IntType(Name.fullColorTwoSided)
        /**
         * "highlight-color" member type.
         */
        @JvmField val highlightColor = IntType(Name.highlightColor)
        /**
         * "highlight-color-two-sided" member type.
         */
        @JvmField val highlightColorTwoSided = IntType(Name.highlightColorTwoSided)
        /**
         * "monochrome" member type.
         */
        @JvmField val monochrome = IntType(Name.monochrome)
        /**
         * "monochrome-two-sided" member type.
         */
        @JvmField val monochromeTwoSided = IntType(Name.monochromeTwoSided)
    }
}
