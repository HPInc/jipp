// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import java.io.IOException

/** An encoder for simple values */
abstract class SimpleEncoder<T>(override val typeName: String) : Encoder<T>() {
    /** Read a single value from the input stream, making use of the set of encoders */
    @Throws(IOException::class)
    abstract fun readValue(input: IppInputStream, valueTag: Tag): T

    @Throws(IOException::class)
    override fun readValue(input: IppInputStream, finder: Finder, valueTag: Tag): T {
        return readValue(input, valueTag)
    }
}
