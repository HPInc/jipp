package com.hp.jipp.trans

import com.hp.jipp.model.Packet
import java.io.Closeable
import java.io.InputStream

/** An IPP Packet, along with associated additional data, if any */
data class PacketData(val packet: Packet, val data: InputStream?) : Closeable {
    constructor(packet: Packet) : this(packet, null)

    /** Closes the supplied data stream, if present */
    override fun close() {
        data?.close()
    }
}
