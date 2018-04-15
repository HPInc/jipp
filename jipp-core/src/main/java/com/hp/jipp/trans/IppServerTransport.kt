// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.trans

import java.io.IOException
import java.net.URI

/** Transport used to receive requests and respond to an IPP client */
interface IppServerTransport {
    /**
     * Handle IPP requests and data, returning a response packet and optional data
     *
     * Note: implementations should check [Thread.interrupted] periodically and fail gracefully.
     */
    @Throws(IOException::class)
    fun handle(uri: URI, request: IppPacketData): IppPacketData
}
