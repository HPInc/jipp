// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.util.ParseError
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.InputStream

class IppInputStream(inputStream: InputStream) : DataInputStream(BufferedInputStream(inputStream)) {

    /** Read a length-value pair, returning it as a [ByteArray] */
    fun readValueBytes(): ByteArray {
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

    /** Skip (discard) a length-value pair */
    fun skipValueBytes() {
        val valueLength = readShort().toLong()
        if (valueLength != skip(valueLength)) throw ParseError("Value too short")
    }

    /** Read a length of an expected amount, throwing if something else is found. */
    fun takeLength(length: Int) {
        val readLength = readShort().toInt()
        if (readLength != length) {
            throw ParseError("Bad attribute length: expected $length, got $readLength")
        }
    }

    /** Read the next string (including its length field) from the stream */
    fun readString() = String(readValueBytes())

    companion object {
        internal const val LENGTH_LENGTH: Int = 2
        internal const val TAG_LEN: Int = 2
    }
}
