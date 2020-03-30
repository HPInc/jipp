// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.util.BuildError
import java.io.DataOutputStream
import java.io.OutputStream

/** An [OutputStream] for writing an [IppPacket]. */
class IppOutputStream(outputStream: OutputStream) : DataOutputStream(outputStream) {

    fun write(packet: IppPacket) {
        with(packet) {
            writeShort(versionNumber)
            writeShort(code)
            writeInt(requestId)
            attributeGroups.forEach { write(it) }
            write(Tag.endOfAttributes)
        }
    }

    /** Write a series of bytes to the output stream, prefixed by length. */
    internal fun writeBytesValue(bytes: ByteArray) {
        writeShort(bytes.size)
        write(bytes)
    }

    internal fun writeIntValue(value: Int) {
        writeShort(IppStreams.INT_LENGTH)
        writeInt(value)
    }

    internal fun writeByteValue(value: Int) {
        writeShort(IppStreams.BYTE_LENGTH)
        writeByte(value)
    }

    /** Write a string to the output stream, prefixed by length. */
    internal fun writeStringValue(string: String) {
        writeBytesValue(string.toByteArray(Charsets.UTF_8))
    }

    /** Write [tag] to this stream. */
    private fun write(tag: Tag) {
        writeByte(tag.code)
    }

    /** Write [group] to this stream. */
    private fun write(group: AttributeGroup) {
        write(group.tag)
        group.forEach { write(it) }
    }

    /** Write [attribute] to this stream. */
    private fun write(attribute: Attribute<*>, name: String = attribute.name) {
        val type = attribute.type
        if (type is EmptyAttributeType) {
            // Write the out-of-band tag
            write(type.tag)
            writeStringValue(name)
            writeShort(0) // 0 value length = no values
        } else {
            writeValueAttribute(attribute, name)
        }
    }

    /** Write an attribute having known value(s) to this stream. */
    private fun writeValueAttribute(attribute: Attribute<*>, name: String = attribute.name) {
        var nameToWrite = name
        attribute.forEach { value ->
            val encoder = IppStreams.clsToCodec[value.javaClass]
                ?: IppStreams.codecs.firstOrNull { it.cls.isAssignableFrom(value.javaClass) }
                ?: throw BuildError("Cannot handle $value: ${value.javaClass}")

            // If the attribute has an enforced tag then apply it
            val tag = if (attribute.type is StringType) {
                (attribute.type as StringType).tag
            } else {
                encoder.tagOf(value)
            }
            write(tag)
            writeStringValue(nameToWrite)
            encoder.writeValue(this, value)

            // Only write attribute name for the first item
            nameToWrite = ""
        }
    }

    internal fun writeCollectionAttributes(attributes: List<Attribute<*>>) {
        writeShort(0) // Empty value
        for (attribute in attributes) {
            write(Tag.memberAttributeName)
            writeShort(0)
            writeStringValue(attribute.name)
            /** Write the attribute with a blank name */
            write(attribute, name = "")
        }
        // Write an empty attribute to end the collection
        write(Tag.endCollection)
        writeStringValue("")
        writeShort(0) // 0 value length = no values
    }
}
