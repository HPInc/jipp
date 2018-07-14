// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.model

import com.hp.jipp.encoding.AttributeGroup
import com.hp.jipp.encoding.AttributeType
import com.hp.jipp.encoding.IppInputStream
import com.hp.jipp.encoding.IppOutputStream
import com.hp.jipp.encoding.Tag
import com.hp.jipp.pwg.Operation
import com.hp.jipp.pwg.Status
import com.hp.jipp.util.ParseError
import com.hp.jipp.util.PrettyPrinter
import com.hp.jipp.util.repeatUntilNull
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * An IPP packet consisting of header information and zero or more attribute groups.
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

    constructor(status: Status, requestId: Int, vararg groups: AttributeGroup):
        this(code = status.code, requestId = requestId, attributeGroups = groups.toList())

    constructor(operation: Operation, requestId: Int, vararg groups: AttributeGroup):
        this(code = operation.code, requestId = requestId, attributeGroups = groups.toList())

    /** Return this response packet's code as a [Status]. */
    val status: Status by lazy {
        Status[code]
    }

    /** Return this request packet's code as an [Operation] */
    val operation: Operation by lazy {
        Operation[code]
    }

    /** Get the attribute group having a delimiter */
    operator fun get(groupDelimiter: Tag): AttributeGroup? =
        attributeGroups.firstOrNull { it.tag == groupDelimiter }

    /** Return all values found within the specified group and having the same attribute type */
    fun <T : Any> getValues(groupDelimiter: Tag, type: AttributeType<T>): List<T> =
        this[groupDelimiter]?.get(type)?.values ?: listOf()

    fun <T : Any> getStrings(groupDelimiter: Tag, type: AttributeType<T>): List<String> =
        this[groupDelimiter]?.get(type)?.strings() ?: listOf()

    fun <T : Any> getValue(groupDelimiter: Tag, type: AttributeType<T>): T? =
        this[groupDelimiter]?.get(type)?.value

    /** Make a copy of this packet but replace with the supplied attribute groups */
    fun withAttributeGroups(attributeGroups: List<AttributeGroup>): IppPacket =
        copy(attributeGroups = attributeGroups)

    /** Write this packet to the [OutputStream] as per RFC2910.  */
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

    private fun prefix(): String {
        return "IppPacket(v=0x" + Integer.toHexString(versionNumber) +
            ", c=" + statusOrOperationString(code) +
            ", r=0x" + Integer.toHexString(requestId) +
            ")"
    }

    private fun statusOrOperationString(code: Int) =
        (Operation.all[code] ?: Status.all[code] ?: code).toString()

    override fun toString(): String {
        return prefix() + " " + attributeGroups
    }

    companion object {
        const val DEFAULT_VERSION_NUMBER = 0x0101

        @JvmStatic
        @Throws(IOException::class)
        @Deprecated("use read()", ReplaceWith("read(input)", "com.hp.jipp.model.IppPacket.Companion.read"))
        fun parse(input: InputStream): IppPacket =
            read(input)

        @JvmStatic
        @Throws(IOException::class)
        fun read(input: InputStream): IppPacket {
            val ippInput = input as? IppInputStream ?: IppInputStream(input)
            return IppPacket(ippInput.readShort().toInt(),
                ippInput.readShort().toInt(),
                ippInput.readInt(),
                { readNextGroup(ippInput) }.repeatUntilNull().toList())
        }

        private fun readNextGroup(input: IppInputStream): AttributeGroup? {
            val tag = Tag.read(input)
            return when (tag) {
                Tag.endOfAttributes -> null
                else -> {
                    if (!tag.isDelimiter) throw ParseError("Illegal delimiter $tag")
                    AttributeGroup.read(input, tag)
                }
            }
        }
    }
}
