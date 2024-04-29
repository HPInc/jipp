// Copyright 2020 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.pdl.util

import java.io.InputStream
import java.io.OutputStream

/** For every byte read from [source], also write to a [duplicate] [OutputStream]. */
class TeeInputStream(
    /** Closed when this stream is closed. */
    private val source: InputStream,
    /** Destination for all bytes read from [source], left open after this stream is closed. */
    private val duplicate: OutputStream
) : InputStream() {

    override fun read() =
        source.read().also { duplicate.write(it) }

    override fun available() = source.available()

    override fun close() {
        source.close()
    }

    override fun mark(readlimit: Int) {
        source.mark(readlimit)
    }

    override fun markSupported() = source.markSupported()

    override fun read(bytes: ByteArray, offset: Int, length: Int) =
        source.read(bytes, offset, length).also {
            duplicate.write(bytes, offset, length)
        }

    override fun read(bytes: ByteArray) =
        source.read(bytes).also { duplicate.write(bytes) }

    override fun reset() {
        source.reset()
    }

    override fun skip(length: Long) = source.skip(length)
}
