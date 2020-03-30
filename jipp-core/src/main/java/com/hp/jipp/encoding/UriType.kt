// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.util.ParseError
import java.net.URI

/** An [AttributeType] for a [URI] value. */
open class UriType(name: String) : AttributeTypeImpl<URI>(name, URI::class.java) {
    /** An [AttributeType] for multiple [URI] values. */
    class Set(name: String) : UriType(name), AttributeSetType<URI> {
        override fun toString() = "UriType.Set($name)"
    }

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
            writeStringValue(it.toString())
        })
    }
}
