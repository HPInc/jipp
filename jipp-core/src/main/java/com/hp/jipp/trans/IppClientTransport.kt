// Copyright 2017 - 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.trans

import java.io.IOException
import java.net.URI

/** Transport used to send packets and collect responses from a IPP server */
interface IppClientTransport {

    /**
     * Deliver an IPP packet to the specified URL along with any additional data, and return the response
     * packet (or throw an error).
     *
     * Note: implementations should check [Thread.interrupted] periodically and fail gracefully.
     */
    @Throws(IOException::class)
    fun sendData(uri: URI, request: IppPacketData): IppPacketData
}
