// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT
//
// DO NOT MODIFY. Code is auto-generated by genTypes.py. Content taken from registry at
// https://www.iana.org/assignments/ipp-registrations/ipp-registrations.xml, updated on 2020-02-20
@file:Suppress("MaxLineLength", "WildcardImport")

package com.hp.jipp.model

import com.hp.jipp.encoding.* // ktlint-disable no-wildcard-imports

/**
 * Data object corresponding to a "job-triggers-supported" collection as defined in:
 * [IPPPRESET](https://ftp.pwg.org/pub/pwg/ipp/registrations/reg-ipppreset-20171214.pdf).
 */
@Suppress("RedundantCompanionReference", "unused")
data class JobTriggersSupported
constructor(
    /** May contain any keyword from [PresetName] or a name. */
    var presetName: KeywordOrName? = null
) : AttributeCollection {

    /** Construct an empty [JobTriggersSupported]. */
    constructor() : this(null)

    /** Produce an attribute list from members. */
    override val attributes: List<Attribute<*>> by lazy {
        listOfNotNull(
            presetName?.let { Types.presetName.of(it) }
        )
    }

    /** Type for attributes of this collection */
    class Type(override val name: String) : AttributeCollection.Type<JobTriggersSupported>(JobTriggersSupported)

    /** All member names as strings. */
    object Name {
        /** "preset-name" member name */
        const val presetName = "preset-name"
    }

    /** Types for each member attribute. */
    object Types {
        val presetName = KeywordOrNameType(Name.presetName)
    }

    /** Defines types for each member of [JobTriggersSupported] */
    companion object : AttributeCollection.Converter<JobTriggersSupported> {
        override fun convert(attributes: List<Attribute<*>>): JobTriggersSupported =
            JobTriggersSupported(
                extractOne(attributes, Types.presetName)
            )
    }
    override fun toString() = "JobTriggersSupported(${attributes.joinToString()})"
}
