// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT
//
// DO NOT MODIFY. Code is auto-generated by genTypes.py. Content taken from registry at
// https://www.iana.org/assignments/ipp-registrations/ipp-registrations.xml, updated on 2018-04-06
@file:Suppress("MaxLineLength", "WildcardImport")

package com.hp.jipp.pwg

import com.hp.jipp.encoding.* // ktlint-disable no-wildcard-imports

/**
 * Data object corresponding to a "job-triggers-supported" collection as defined in:
 * [IPPPRESET](https://ftp.pwg.org/pub/pwg/ipp/registrations/reg-ipppreset-20171214.pdf).
 */
@Suppress("RedundantCompanionReference", "unused")
data class JobTriggersSupported
@JvmOverloads constructor(
    /** May contain any keyword from [PresetName] or a name. */
    val presetName: String? = null,
    /** Encoded form, if known. */
    val _encoded: List<Attribute<*>>? = null
) : AttributeCollection {

    /** Produce an attribute list from members, or return the original [_encoded] attribute list if present. */
    override val attributes: List<Attribute<*>> by lazy {
        _encoded ?: listOfNotNull(
            presetName?.let { Members.presetName.of(it) }
        )
    }

    /** Type for attributes of this collection */
    class Type(override val name: String) : AttributeCollection.Type<JobTriggersSupported>(Members)

    /** All member names as strings. */
    object Name {
        /** "preset-name" member name */
        const val presetName = "preset-name"
    }

    /** Builder for immutable [JobTriggersSupported] objects. */
    class Builder() {
        /** Constructs a new [Builder] pre-initialized with values in [source]. */
        constructor(source: JobTriggersSupported) : this() {
            presetName = source.presetName
        }
        /** May contain any keyword from [PresetName] or a name. */
        var presetName: String? = null

        /** Return a new [JobTriggersSupported] object containing all values initialized in this builder. */
        fun build() = JobTriggersSupported(
            presetName
        )
    }

    companion object Members : AttributeCollection.Converter<JobTriggersSupported> {
        override fun convert(attributes: List<Attribute<*>>): JobTriggersSupported =
            JobTriggersSupported(
                extractOne(attributes, presetName),
                _encoded = attributes)
        /**
         * "preset-name" member type.
         * May contain any keyword from [PresetName] or a name.
         */
        @JvmField val presetName = KeywordType(Name.presetName)
    }
}
