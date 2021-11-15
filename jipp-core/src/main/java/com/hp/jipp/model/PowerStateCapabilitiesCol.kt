// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT
//
// DO NOT MODIFY. Code is auto-generated by genTypes.py. Content taken from registry at
// https://www.iana.org/assignments/ipp-registrations/ipp-registrations.xml, updated on 2021-10-14
@file:Suppress("MaxLineLength", "WildcardImport")

package com.hp.jipp.model

import com.hp.jipp.encoding.* // ktlint-disable no-wildcard-imports

/**
 * Data object corresponding to a "power-state-capabilities-col" collection as defined in:
 * [PWG5100.22](https://ftp.pwg.org/pub/pwg/candidates/cs-ippsystem10-20191122-5100.22.pdf).
 */
@Suppress("RedundantCompanionReference", "unused")
data class PowerStateCapabilitiesCol
constructor(
    var canAcceptJobs: Boolean? = null,
    var canProcessJobs: Boolean? = null,
    var powerActiveWatts: Int? = null,
    var powerInactiveWatts: Int? = null,
    var powerState: String? = null,
) : AttributeCollection {

    /** Construct an empty [PowerStateCapabilitiesCol]. */
    constructor() : this(null)

    /** Produce an attribute list from members. */
    override val attributes: List<Attribute<*>>
        get() = listOfNotNull(
            canAcceptJobs?.let { PowerStateCapabilitiesCol.canAcceptJobs.of(it) },
            canProcessJobs?.let { PowerStateCapabilitiesCol.canProcessJobs.of(it) },
            powerActiveWatts?.let { PowerStateCapabilitiesCol.powerActiveWatts.of(it) },
            powerInactiveWatts?.let { PowerStateCapabilitiesCol.powerInactiveWatts.of(it) },
            powerState?.let { PowerStateCapabilitiesCol.powerState.of(it) },
        )

    /** Defines types for each member of [PowerStateCapabilitiesCol]. */
    companion object : AttributeCollection.Converter<PowerStateCapabilitiesCol> {
        override fun convert(attributes: List<Attribute<*>>): PowerStateCapabilitiesCol =
            PowerStateCapabilitiesCol(
                extractOne(attributes, canAcceptJobs),
                extractOne(attributes, canProcessJobs),
                extractOne(attributes, powerActiveWatts),
                extractOne(attributes, powerInactiveWatts),
                extractOne(attributes, powerState),
            )
        override val cls = PowerStateCapabilitiesCol::class.java
        @Deprecated("Remove this symbol")
        @JvmField val Types = this
        @JvmField val canAcceptJobs = BooleanType("can-accept-jobs")
        @JvmField val canProcessJobs = BooleanType("can-process-jobs")
        @JvmField val powerActiveWatts = IntType("power-active-watts")
        @JvmField val powerInactiveWatts = IntType("power-inactive-watts")
        @JvmField val powerState = KeywordType("power-state")
    }
    override fun toString() = "PowerStateCapabilitiesCol(${attributes.joinToString()})"
}
