// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.URI

/** An [AttributeType] for [URI] attributes */
open class UriType(tag: Tag, override val name: String) : AttributeType<URI>(ENCODER, tag) {
    companion object {
        private val TYPE_NAME = "URI"

        @JvmField val ENCODER: SimpleEncoder<URI> = object : SimpleEncoder<URI>(TYPE_NAME) {
            @Throws(IOException::class)
            override fun writeValue(out: DataOutputStream, value: URI) {
                StringType.ENCODER.writeValue(out, value.toString())
            }

            @Throws(IOException::class)
            override fun readValue(input: DataInputStream, valueTag: Tag) =
                URI.create(StringType.ENCODER.readValue(input, valueTag))

            override fun valid(valueTag: Tag) = valueTag === Tag.uri
        }
    }
}
