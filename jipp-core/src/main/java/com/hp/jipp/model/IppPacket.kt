// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.model

import com.hp.jipp.encoding.AttributeGroup
import com.hp.jipp.encoding.AttributeType
import com.hp.jipp.encoding.Encoder
import com.hp.jipp.encoding.Enum
import com.hp.jipp.encoding.EnumType
import com.hp.jipp.encoding.IppInputStream
import com.hp.jipp.encoding.IppOutputStream
import com.hp.jipp.encoding.Tag
import com.hp.jipp.util.ParseError
import com.hp.jipp.util.PrettyPrinter
import com.hp.jipp.util.toList
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * An IPP packet as specified in RFC2910.
 */
data class IppPacket constructor(
    val versionNumber: Int = DEFAULT_VERSION_NUMBER,
    val code: Int,
    val requestId: Int,
    val attributeGroups: List<AttributeGroup> = listOf()
) {
    @JvmOverloads
    constructor(
        versionNumber: Int = DEFAULT_VERSION_NUMBER,
        code: Int,
        requestId: Int,
        vararg groups: AttributeGroup
    ) : this(versionNumber, code, requestId, groups.toList())

    constructor(code: Code, requestId: Int, vararg groups: AttributeGroup):
            this(code = code.code, requestId = requestId, attributeGroups = groups.toList())

    /**
     * Return this response packet's Status code
     */
    val status: Status
        get() = getCode(Status.Encoder)

    /**
     * Return this request packet's Operation code
     */
    val operation: Operation
        get() = getCode(Operation.Encoder)

    // Return a Enum corresponding to this packet's code.
    private fun <T : Enum> getCode(encoder: EnumType.Encoder<T>): T = encoder[code]

    /** Return a copy of this packet with attribute groups replaced */
    fun withAttributeGroups(groups: List<AttributeGroup>) = copy(attributeGroups = groups)

    /** Returns the first attribute with the specified delimiter  */
    fun getAttributeGroup(delimiter: Tag): AttributeGroup? {
        return attributeGroups.firstOrNull { it.tag === delimiter }
    }

    /** Return the first value of an [AttributeType] in an [AttributeGroup] having the supplied [Tag], if any */
    fun <T> getValue(groupDelimiter: Tag, attributeType: AttributeType<T>): T? =
        getAttributeGroup(groupDelimiter)?.getValue(attributeType)

    /**
     * Return all values of an [AttributeType] from an [AttributeGroup] having the supplied [Tag], or an empty
     * list if none.
     */
    fun <T> getValues(groupDelimiter: Tag, attributeType: AttributeType<T>): List<T> =
        getAttributeGroup(groupDelimiter)?.getValues(attributeType) ?: emptyList()

    private fun prefix(): String {
        return "IppPacket(v=x" + Integer.toHexString(versionNumber) +
                " code=" + getCode(Code.Encoder) +
                " rId=x" + Integer.toHexString(requestId) +
                ")"
    }

    /** Write a Packet to this [OutputStream] as per RFC2910  */
    @Throws(IOException::class)
    fun write(output: OutputStream) {
        val ippOutput = IppOutputStream(output)
        ippOutput.writeShort(versionNumber)
        ippOutput.writeShort(code)
        ippOutput.writeInt(requestId)
        attributeGroups.forEach { it.write(ippOutput) }
        Tag.endOfAttributes.write(ippOutput)
    }

    /** Return a pretty-printed version of this packet (including separators and line breaks) */
    fun prettyPrint(maxWidth: Int, indent: String) = PrettyPrinter(prefix(), PrettyPrinter.OBJECT, indent, maxWidth)
            .addAll(attributeGroups)
            .print()

    override fun toString(): String {
        return prefix() + " " + attributeGroups
    }

    companion object {
        /** Default version number to be sent in a packet (0x0101 for IPP 1.1)  */
        const val DEFAULT_VERSION_NUMBER = 0x0101

        @Throws(IOException::class)
        private fun readPacket(input: IppInputStream): IppPacket {

            val versionNumber = input.readShort().toInt()
            val code = input.readShort().toInt()
            val requestId = input.readInt()

            return IppPacket(versionNumber, code, requestId, { readNextGroup(input) }.toList())
        }

        private fun readNextGroup(input: IppInputStream):
                AttributeGroup? {
            val tag = Tag.read(input)
            return when (tag) {
                Tag.endOfAttributes -> null
                else -> {
                    if (!tag.isDelimiter) throw ParseError("Illegal delimiter $tag")
                    AttributeGroup.read(input, tag)
                }
            }
        }

        @JvmStatic @JvmOverloads
        fun parse(input: InputStream, finder: Encoder.Finder = Types.allFinder): IppPacket {
            val ippInput = IppInputStream(input, finder)
            return readPacket(ippInput)
        }
    }
}
