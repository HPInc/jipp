// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.util.ParseError
import java.net.URI

/** An attribute type for `uri` attributes. */
open class UriType(override val name: String) : AttributeType<URI> {
    override fun coerce(value: Any) =
        value as? URI

    override fun toString() = "UriType($name)"

    companion object {
        val codec = Codec<URI>(Tag.uri, {
            val uriString = readString()
            try {
                URI.create(uriString)
            } catch (e: IllegalArgumentException) {
                throw ParseError("Could not parse URI $uriString", e)
            }
        }, {
            writeString(it.toString())
        })
    }
}
