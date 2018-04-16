// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import java.io.IOException
import java.net.URI

/** An [AttributeType] for [URI] attributes */
open class UriType(tag: Tag, override val name: String) : AttributeType<URI>(Encoder, tag) {
    companion object Encoder : SimpleEncoder<URI>("URI") {
        @Throws(IOException::class)
        override fun writeValue(out: IppOutputStream, value: URI) {
            StringType.Encoder.writeValue(out, value.toString())
        }

        @Throws(IOException::class)
        override fun readValue(input: IppInputStream, valueTag: Tag) =
            URI.create(StringType.Encoder.readValue(input, valueTag))!!

        override fun valid(valueTag: Tag) = valueTag === Tag.uri
    }
}
