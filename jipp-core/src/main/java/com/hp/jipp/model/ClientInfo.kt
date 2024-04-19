// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT
//
// DO NOT MODIFY. Code is auto-generated by genTypes.py. Content taken from registry at
// https://www.iana.org/assignments/ipp-registrations/ipp-registrations.xml, updated on 2023-11-27
@file:Suppress("MaxLineLength", "WildcardImport")

package com.hp.jipp.model

import com.hp.jipp.encoding.* // ktlint-disable no-wildcard-imports

/**
 * Data object corresponding to a "client-info" collection as defined in:
 * [PWG5100.7](https://ftp.pwg.org/pub/pwg/candidates/cs-ippjobext21-20230210-5100.7.pdf).
 */
@Suppress("RedundantCompanionReference", "unused")
data class ClientInfo
constructor(
    var clientName: String? = null,
    var clientPatches: String? = null,
    var clientStringVersion: String? = null,
    var clientType: ClientType? = null,
    var clientVersion: ByteArray? = null,
) : AttributeCollection {

    /** Construct an empty [ClientInfo]. */
    constructor() : this(null)

    /** Produce an attribute list from members. */
    override val attributes: List<Attribute<*>>
        get() = listOfNotNull(
            clientName?.let { ClientInfo.clientName.of(it) },
            clientPatches?.let { ClientInfo.clientPatches.of(it) },
            clientStringVersion?.let { ClientInfo.clientStringVersion.of(it) },
            clientType?.let { ClientInfo.clientType.of(it) },
            clientVersion?.let { ClientInfo.clientVersion.of(it) },
        )

    /** Defines types for each member of [ClientInfo]. */
    companion object : AttributeCollection.Converter<ClientInfo> {
        override fun convert(attributes: List<Attribute<*>>): ClientInfo =
            ClientInfo(
                extractOne(attributes, clientName)?.value,
                extractOne(attributes, clientPatches)?.value,
                extractOne(attributes, clientStringVersion)?.value,
                extractOne(attributes, clientType),
                extractOne(attributes, clientVersion),
            )
        override val cls = ClientInfo::class.java
        @Deprecated("Remove this symbol")
        @JvmField val Types = this
        @JvmField val clientName = NameType("client-name")
        @JvmField val clientPatches = TextType("client-patches")
        @JvmField val clientStringVersion = TextType("client-string-version")
        /**
         * "client-type" member type.
         */
        @JvmField val clientType = ClientType.Type("client-type")
        @JvmField val clientVersion = OctetsType("client-version")
    }
    override fun toString() = "ClientInfo(${attributes.joinToString()})"
}
