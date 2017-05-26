package com.hp.jipp.encoding

import com.hp.jipp.util.ParseError
import com.hp.jipp.util.Util
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

// Encodings to assist in reading/writing IPP to streams

@Throws(IOException::class)
fun DataOutputStream.writeValueBytes(bytes: ByteArray) {
    this.writeShort(bytes.size)
    this.write(bytes)
}

/** Writes a UTF-8 string to output */
@Throws(IOException::class)
fun DataOutputStream.writeString(string: String) {
    writeValueBytes(string.toByteArray(charset(Util.UTF8)))
}

/** Skip (discard) a length-value pair  */
@Throws(IOException::class)
fun DataInputStream.skipValueBytes() {
    val valueLength = this.readShort().toInt()
    if (valueLength.toLong() != this.skip(valueLength.toLong())) throw ParseError("Value too short")
}

/** Read and return value bytes from a length-value pair  */
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

/** Reads a UTF-8 string from input */
@Throws(IOException::class)
fun DataInputStream.readString() = String(this.readValueBytes())

/** Reads a two-byte length field, asserting that it is of a specific length  */
@Throws(IOException::class)
fun DataInputStream.takeLength(length: Int) {
    val readLength = this.readShort().toInt()
    if (readLength != length) {
        throw ParseError("Bad attribute length: expected $length, got $readLength")
    }
}
