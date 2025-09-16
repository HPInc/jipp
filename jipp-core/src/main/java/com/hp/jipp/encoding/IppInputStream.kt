// © Copyright 2017 - 2021 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.model.EnumTypes
import com.hp.jipp.model.KeyValuesTypes
import com.hp.jipp.util.ParseError
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.IOException
import java.io.InputStream

/** An [InputStream] which can read IPP packet data. */
@Suppress("TooManyFunctions") // This utility class must handle many different data types.
class IppInputStream(inputStream: InputStream) : DataInputStream(BufferedInputStream(inputStream)) {

    /** Reads a complete packet from this stream. */
    @Throws(IOException::class)
    fun readPacket() =
        IppPacket(
            readShort().toInt(), // Version
            readShort().toInt(), // Code / Status
            readInt(), // ID
            generateSequence { readGroup() }.toList()
        )

    /**
     * Returns the next [AttributeGroup] from the stream or null if there are no more groups.
     */
    fun readGroup(): AttributeGroup? =
        readTag()?.takeIf { it != Tag.endOfAttributes }?.let { tag ->
            if (tag !is DelimiterTag) throw ParseError("Illegal delimiter $tag")
            readAttributeGroup(tag)
        } // Note: a null tag means no endOfAttributes tag (which is not valid) but we ignore it.

    /** Read and return the next [Tag] in the input if possible. */
    private fun readTag(): Tag? = read().takeIf { it >= 0 }?.let { Tag.fromInt(it) }

    /**
     * Read an entire attribute group if available in the input stream.
     */
    internal fun readAttributeGroup(tag: DelimiterTag) =
        AttributeGroup.groupOf(tag, generateSequence { readNextAttribute() }.toList())

    /** Read the next attribute if present. */
    private fun readNextAttribute(): Attribute<*>? {
        mark(1)
        return readTag()?.let { tag ->
            if (tag.isDelimiter) {
                reset()
                null
            } else {
                readAnyAttribute(tag)
            }
        }
    }

    /** Read and return an attribute with all of its values. */
    private fun readAnyAttribute(initTag: Tag): Attribute<*> =
        readAnyAttribute(readString(), initTag)

    /** Read the next string (including length) from the stream. */
    internal fun readString() = String(readValueBytes())

    /** Read and return an attribute with all of its values, having its attribute name already. */
    private fun readAnyAttribute(attributeName: String, initTag: Tag): Attribute<*> =
        when (initTag) {
            is OutOfBandTag -> {
                readValueBytes()
                EmptyAttribute<Nothing>(attributeName, initTag)
            }
            is ValueTag ->
                IppStreams.codecs.find { it.handlesTag(initTag) }?.let {
                    UnknownAttribute(
                        attributeName,
                        listOf(readValue(it, initTag, attributeName)) +
                            generateSequence { readNextValue(attributeName) }
                    )
                } ?: throw ParseError("No codec found for tag $initTag")
            else -> throw ParseError("invalid attribute tag $initTag")
        }

    @Suppress("ReturnCount")
    private fun <T : Any> readValue(codec: Codec<T>, tag: ValueTag, attributeName: String): Any {
        // Apply a special case for enum values which we can match with all known [EnumTypes]
        if (tag == Tag.enumValue) {
            EnumTypes.all[attributeName]?.also {
                // Note: !! is safe because we know EnumTypes can handle Int input
                return it.coerce(readIntValue())!!
            }
        } else if (tag == Tag.octetString) {
            KeyValuesTypes.all[attributeName]?.also {
                return KeyValues.codec.readValue(this, tag)
            }
        }
        return codec.readValue(this, tag)
    }

    /** Return an encoded int value. */
    internal fun readIntValue(): Int {
        takeLength(IppStreams.INT_LENGTH)
        return readInt()
    }

    /** Read a length of an expected amount, throwing if something else is found. */
    internal fun takeLength(length: Int) {
        val readLength = readShort().toInt()
        if (readLength != length) {
            throw ParseError("Bad attribute length: expected $length, got $readLength")
        }
    }

    /** Read a length-value pair, returning it as a [ByteArray]. */
    internal fun readValueBytes(): ByteArray {
        val valueLength = readShort().toInt()
        val valueBytes = ByteArray(valueLength)
        if (valueLength > 0) {
            readFully(valueBytes)
        }
        return valueBytes
    }

    /** Read the next value for the given attributeName. */
    @Suppress("ReturnCount") // Best way to handle errors in this case
    private fun readNextValue(attributeName: String): Any? {
        mark(IppStreams.TAG_LEN + IppStreams.LENGTH_LENGTH)
        return readTag()?.let { tag ->
            if (tag.isEndOfValueStream() || readShort().toInt() != 0) {
                // Non-value tag or non-empty name means its a completely different attribute.
                reset()
                null
            } else if (tag is ValueTag) {
                val codec = IppStreams.tagToCodec[tag] // Fast lookup
                    ?: IppStreams.codecs.firstOrNull { it.handlesTag(tag) } // Slower, more thorough lookup
                    ?: throw ParseError("No codec found for tag $tag")
                readValue(codec, tag, attributeName)
            } else null
        }
    }

    internal fun readCollectionAttributes(): List<Attribute<*>> {
        val attributes = mutableListOf<Attribute<*>>()
        while (true) {
            when (val tag = readTag()) {
                Tag.endCollection -> {
                    skipValueBytes()
                    skipValueBytes()
                    return attributes
                }
                Tag.memberAttributeName -> {
                    skipValueBytes()
                    val memberName = readString()
                    val memberTag = readTag() ?: throw ParseError("Missing member tag in $tag")
                    // Read and throw away the (blank) attribute value
                    readValueBytes()
                    attributes.add(readAnyAttribute(memberName, memberTag))
                }
                else -> throw ParseError("Bad tag in collection: $tag")
            }
        }
    }

    /** Read and discard a length-value pair. */
    internal fun skipValueBytes() {
        val valueLength = readShort().toLong()
        if (valueLength != skip(valueLength)) throw ParseError("Value too short")
    }

    internal fun readByteValue(): Byte {
        takeLength(IppStreams.BYTE_LENGTH)
        return readByte()
    }
}
