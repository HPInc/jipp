// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.pdl.util

import java.io.OutputStream

/** An output stream that wraps an existing array, overwriting it from the beginning. */
class WrappedByteArrayOutputStream(private val array: ByteArray, offset: Int = 0) : OutputStream() {
    /** The current write position relative to the start of [array]. */
    var pos: Int = offset
        private set(value) { field = value }

    override fun write(byte: Int) {
        array[pos++] = byte.toByte()
    }

    override fun write(source: ByteArray, offset: Int, length: Int) {
        source.copyInto(array, pos, offset, offset + length)
        pos += length
    }
}
