// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.trans

import com.hp.jipp.model.IppPacket
import java.io.IOException
import java.net.URI

/** Transport used to send packets and collect responses from a IPP server */
abstract class IppClientTransport {

    /**
     * Deliver an IPP packet to the specified URL along with any additional data, and return the response
     * packet (or throw an error).
     *
     * Note: implementations should check [Thread.interrupted] periodically and fail gracefully.
     */
    @Throws(IOException::class)
    abstract fun sendData(uri: URI, request: IppPacketData): IppPacketData

    /** Shortcut for [sendData] when no additional data is delivered or expected in return */
    fun send(uri: URI, ippPacket: IppPacket): IppPacket =
            sendData(uri, IppPacketData(ippPacket)).let {
                it.close()
                it.ippPacket
            }
}
