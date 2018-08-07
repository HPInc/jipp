// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT
//
// DO NOT MODIFY. Code is auto-generated by genTypes.py. Content taken from registry at
// https://www.iana.org/assignments/ipp-registrations/ipp-registrations.xml, updated on 2018-04-06
@file:Suppress("MaxLineLength")

package com.hp.jipp.pwg

import com.hp.jipp.encoding.KeyValueType

/**
 * All known [KeyValueType] types, used for better decoding of [IppPacket] data.
 */
object KeyValueTypes {
    @JvmField val all: Map<String, KeyValueType> = listOf(
        PrinterStatusGroup.printerOutputTray,
        OperationGroup.documentMetadata,
        PrinterStatusGroup.printerFinisherSupplies,
        PrinterStatusGroup.printerAlert,
        PrinterStatusGroup.printerSupply,
        PrinterStatusGroup.printerFinisher,
        PrinterStatusGroup.printerInputTray
    ).map { it.name to it }.toMap()
}
