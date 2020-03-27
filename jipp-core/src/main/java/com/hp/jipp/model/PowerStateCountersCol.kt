// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT
//
// DO NOT MODIFY. Code is auto-generated by genTypes.py. Content taken from registry at
// https://www.iana.org/assignments/ipp-registrations/ipp-registrations.xml, updated on 2020-02-20
@file:Suppress("MaxLineLength", "WildcardImport")

package com.hp.jipp.model

import com.hp.jipp.encoding.* // ktlint-disable no-wildcard-imports

/**
 * Data object corresponding to a "power-state-counters-col" collection as defined in:
 * [PWG5100.22](https://ftp.pwg.org/pub/pwg/candidates/cs-ippsystem10-20191122-5100.22.pdf).
 */
@Suppress("RedundantCompanionReference", "unused")
data class PowerStateCountersCol
constructor(
    var hibernateTransitions: Int? = null,
    var onTransitions: Int? = null,
    var standbyTransitions: Int? = null,
    var suspendTransitions: Int? = null
) : AttributeCollection {

    /** Construct an empty [PowerStateCountersCol]. */
    constructor() : this(null, null, null, null)

    /** Produce an attribute list from members. */
    override val attributes: List<Attribute<*>> by lazy {
        listOfNotNull(
            hibernateTransitions?.let { Types.hibernateTransitions.of(it) },
            onTransitions?.let { Types.onTransitions.of(it) },
            standbyTransitions?.let { Types.standbyTransitions.of(it) },
            suspendTransitions?.let { Types.suspendTransitions.of(it) }
        )
    }

    /** Type for attributes of this collection */
    class Type(override val name: String) : AttributeCollection.Type<PowerStateCountersCol>(PowerStateCountersCol)

    /** All member names as strings. */
    object Name {
        /** "hibernate-transitions" member name */
        const val hibernateTransitions = "hibernate-transitions"
        /** "on-transitions" member name */
        const val onTransitions = "on-transitions"
        /** "standby-transitions" member name */
        const val standbyTransitions = "standby-transitions"
        /** "suspend-transitions" member name */
        const val suspendTransitions = "suspend-transitions"
    }

    /** Types for each member attribute. */
    object Types {
        val hibernateTransitions = IntType(Name.hibernateTransitions)
        val onTransitions = IntType(Name.onTransitions)
        val standbyTransitions = IntType(Name.standbyTransitions)
        val suspendTransitions = IntType(Name.suspendTransitions)
    }

    /** Defines types for each member of [PowerStateCountersCol] */
    companion object : AttributeCollection.Converter<PowerStateCountersCol> {
        override fun convert(attributes: List<Attribute<*>>): PowerStateCountersCol =
            PowerStateCountersCol(
                extractOne(attributes, Types.hibernateTransitions),
                extractOne(attributes, Types.onTransitions),
                extractOne(attributes, Types.standbyTransitions),
                extractOne(attributes, Types.suspendTransitions)
            )
    }
    override fun toString() = "PowerStateCountersCol(${attributes.joinToString()})"
}
