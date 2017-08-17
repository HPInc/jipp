package com.hp.jipp.trans

import com.hp.jipp.model.Packet
import java.io.IOException
import java.net.URI

/** Transport used to send packets and collect responses  */
abstract class Transport {

    /**
     * Deliver an IPP packet to the specified URL along with any additional data, and return the response
     * packet (or throw an error).
     *
     * Note: implementations should check [Thread.interrupted] periodically and fail gracefully.
     */
    @Throws(IOException::class)
    abstract fun sendData(uri: URI, packetData: PacketData): PacketData

    /** Shortcut for [sendData] when no additional data is delivered or expected in return */
    fun send(uri: URI, packet: Packet): Packet =
            sendData(uri, PacketData(packet)).let {
                it.close()
                it.packet
            }
}
