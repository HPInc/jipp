// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.trans

import com.hp.jipp.model.IppPacket
import java.io.Closeable
import java.io.InputStream

/** An IPP Packet, along with associated additional data, if any */
data class IppPacketData(val packet: IppPacket, val data: InputStream?) : Closeable {
    constructor(ippPacket: IppPacket) : this(ippPacket, null)

    /** Closes the supplied data stream, if present */
    override fun close() {
        data?.close()
    }
}
