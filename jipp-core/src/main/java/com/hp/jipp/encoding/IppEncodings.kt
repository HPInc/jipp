// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.util.ParseError
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

// Encodings to assist in reading/writing IPP to streams

/** Writes attribute value bytes to the [DataOutputStream], including a two-byte length and the bytes themselves */
@Throws(IOException::class)
fun DataOutputStream.writeValueBytes(bytes: ByteArray) {
    this.writeShort(bytes.size)
    this.write(bytes)
}

/** Writes a UTF-8 string to output as an attribute value */
@Throws(IOException::class)
fun DataOutputStream.writeString(string: String) {
    writeValueBytes(string.toByteArray(Charsets.UTF_8))
}

/** Skip (discard) a length-value pair */
@Throws(IOException::class)
fun DataInputStream.skipValueBytes() {
    val valueLength = this.readShort().toInt()
    if (valueLength.toLong() != this.skip(valueLength.toLong())) throw ParseError("Value too short")
}

/** Read and return value bytes from a length-value pair */
@Throws(IOException::class)
fun DataInputStream.readValueBytes(): ByteArray {
    val valueLength = this.readShort().toInt()
    val valueBytes = ByteArray(valueLength)
    if (valueLength > 0) {
        val actual = this.read(valueBytes)
        if (valueLength > actual) {
            throw ParseError("Value too short: expected " + valueBytes.size + " but got " + actual)
        }
    }
    return valueBytes
}

/** Read a UTF-8 string from input. */
@Throws(IOException::class)
fun DataInputStream.readString() = String(this.readValueBytes())

/** Read a two-byte length field, asserting that it is of a specific length and throwing if it isn't.  */
@Throws(IOException::class)
fun DataInputStream.takeLength(length: Int) {
    val readLength = this.readShort().toInt()
    if (readLength != length) {
        throw ParseError("Bad attribute length: expected $length, got $readLength")
    }
}

/**
 * Read an entire attribute from an input stream, based on its tag
 */
@Throws(IOException::class)
fun DataInputStream.readAttribute(finder: Encoder.Finder, valueTag: Tag): Attribute<*> {
    val name = String(this.readValueBytes())
    return readAttribute(finder.find(valueTag, name), finder, valueTag, name)
}
