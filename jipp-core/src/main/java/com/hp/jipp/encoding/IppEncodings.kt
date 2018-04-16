// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.encoding.IppInputStream.Companion.LENGTH_LENGTH
import com.hp.jipp.util.ParseError
import com.hp.jipp.util.toSequence
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream

class IppOutputStream(outputStream: OutputStream) : DataOutputStream(outputStream) {
    fun writeValueBytes(bytes: ByteArray) {
        writeShort(bytes.size)
        write(bytes)
    }

    fun writeString(string: String) {
        writeValueBytes(string.toByteArray(Charsets.UTF_8))
    }

    fun stringLength(string: String) = LENGTH_LENGTH + string.toByteArray(Charsets.UTF_8).size

    fun <T> writeValue(encoder: Encoder<T>, value: T) {
        encoder.writeValue(this, value)
    }
}

class IppInputStream(
    inputStream: InputStream,
    private val finder: Encoder.Finder
) : DataInputStream(BufferedInputStream(inputStream)) {

    constructor(inputStream: InputStream, attributeTypes: List<AttributeType<*>>) : this(
            inputStream, Encoder.finderOf(attributeTypes.map {
        it.name to it
    }.toMap(), AttributeGroup.encoders))

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

    fun readString() = String(this.readValueBytes())

    fun readAttribute(valueTag: Tag): Attribute<*> {
        val name = String(this.readValueBytes())
        return readAttribute(finder.find(valueTag, name), valueTag, name)
    }

    fun takeLength(length: Int) {
        val readLength = this.readShort().toInt()
        if (readLength != length) {
            throw ParseError("Bad attribute length: expected $length, got $readLength")
        }
    }

    fun <T> readAttribute(encoder: Encoder<T>, valueTag: Tag, name: String): Attribute<T> {
        val all = listOf(encoder.readValue(this, finder, valueTag)) +
                { readAdditionalValue(encoder, valueTag) }.toSequence()
        return Attribute(valueTag, name, all, encoder)
    }

    private fun <T> readAdditionalValue(encoder: Encoder<T>, valueTag: Tag): T? {
        // We need to look ahead so mark maximum amount
        if (available() < TAG_LEN + LENGTH_LENGTH) return null
        mark(TAG_LEN + LENGTH_LENGTH)

        return if (Tag.read(this) == valueTag && readShort().toInt() == 0) {
            // Tag matches and no name, so this is an additional value
            encoder.readValue(this, finder, valueTag)
        } else {
            // NOT an additional value so reset stream and return null
            reset()
            null
        }
    }

    companion object {
        internal const val LENGTH_LENGTH: Int = 2
        internal const val TAG_LEN: Int = 2
    }
}
