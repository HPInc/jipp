// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.util.ParseError
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.InputStream
import com.hp.jipp.util.repeatUntilNull
import java.io.IOException

/** An [InputStream] which can read IPP packet data. */
class IppInputStream(inputStream: InputStream) : DataInputStream(BufferedInputStream(inputStream)) {

    /** Reads a complete packet from this stream. */
    @Throws(IOException::class)
    fun readPacket() =
        IppPacket(
            readShort().toInt(),
            readShort().toInt(),
            readInt(),
            { readNextGroup() }.repeatUntilNull().toList())

    /** Reads the next attribute group from the stream. */
    private fun readNextGroup(): AttributeGroup? =
        when (val tag = Tag.read(this)) {
            Tag.endOfAttributes -> null
            else -> {
                if (!tag.isDelimiter) throw ParseError("Illegal delimiter $tag")
                AttributeGroup.read(this, tag)
            }
        }

    /** Read a length-value pair, returning it as a [ByteArray]. */
    internal fun readValueBytes(): ByteArray {
        val valueLength = readShort().toInt()
        val valueBytes = ByteArray(valueLength)
        if (valueLength > 0) {
            val actual = read(valueBytes)
            if (valueLength > actual) {
                throw ParseError("Value too short: expected " + valueBytes.size + " but got " + actual)
            }
        }
        return valueBytes
    }

    /** Read and discard a length-value pair. */
    internal fun skipValueBytes() {
        val valueLength = readShort().toLong()
        if (valueLength != skip(valueLength)) throw ParseError("Value too short")
    }

    /** Read a length of an expected amount, throwing if something else is found. */
    internal fun takeLength(length: Int) {
        val readLength = readShort().toInt()
        if (readLength != length) {
            throw ParseError("Bad attribute length: expected $length, got $readLength")
        }
    }

    /** Read the next string (including length) from the stream. */
    internal fun readString() = String(readValueBytes())

    companion object {
        internal const val LENGTH_LENGTH: Int = 2
        internal const val TAG_LEN: Int = 2
    }
}
