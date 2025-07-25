// Copyright 2020 - 2025 HP Development Company, L.P.
// SPDX-License-Identifier: MIT
//
// DO NOT MODIFY. Code is auto-generated by genTypes.py. Content taken from registry at
// https://www.iana.org/assignments/ipp-registrations/ipp-registrations.xml, updated on 2025-05-13
@file:Suppress("MaxLineLength", "WildcardImport")

package com.hp.jipp.model

import com.hp.jipp.encoding.* // ktlint-disable no-wildcard-imports

/**
 * Data object corresponding to a "power-event-policy-col" collection as defined in:
 * [PWG5100.22](https://ftp.pwg.org/pub/pwg/candidates/cs-ippsystem11-20250328-5100.22.pdf).
 */
@Suppress("RedundantCompanionReference", "unused")
data class PowerEventPolicyCol
constructor(
    var eventId: Int? = null,
    var eventName: String? = null,
    var requestPowerState: String? = null,
) : AttributeCollection {

    /** Construct an empty [PowerEventPolicyCol]. */
    constructor() : this(null)

    /** Produce an attribute list from members. */
    override val attributes: List<Attribute<*>>
        get() = listOfNotNull(
            eventId?.let { PowerEventPolicyCol.eventId.of(it) },
            eventName?.let { PowerEventPolicyCol.eventName.of(it) },
            requestPowerState?.let { PowerEventPolicyCol.requestPowerState.of(it) },
        )

    /** Defines types for each member of [PowerEventPolicyCol]. */
    companion object : AttributeCollection.Converter<PowerEventPolicyCol> {
        override fun convert(attributes: List<Attribute<*>>): PowerEventPolicyCol =
            PowerEventPolicyCol(
                extractOne(attributes, eventId),
                extractOne(attributes, eventName)?.value,
                extractOne(attributes, requestPowerState),
            )
        override val cls = PowerEventPolicyCol::class.java
        @Deprecated("Remove this symbol")
        @JvmField val Types = this
        @JvmField val eventId = IntType("event-id")
        @JvmField val eventName = NameType("event-name")
        @JvmField val requestPowerState = KeywordType("request-power-state")
    }
    override fun toString() = "PowerEventPolicyCol(${attributes.joinToString()})"
}
