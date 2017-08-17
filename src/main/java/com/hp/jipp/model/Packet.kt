package com.hp.jipp.model

import com.hp.jipp.encoding.AttributeGroup
import com.hp.jipp.encoding.AttributeType
import com.hp.jipp.encoding.Enum
import com.hp.jipp.encoding.EnumType
import com.hp.jipp.util.ParseError
import com.hp.jipp.encoding.Tag
import com.hp.jipp.encoding.readGroup
import com.hp.jipp.encoding.readTag
import com.hp.jipp.encoding.writeGroup
import com.hp.jipp.encoding.writeTag
import com.hp.jipp.util.PrettyPrinter
import com.hp.jipp.util.toList

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

/**
 * A request packet as specified in RFC2910.
 */
data class Packet constructor(val versionNumber: Int = DEFAULT_VERSION_NUMBER, val code: Int, val requestId: Int,
                              val attributeGroups: List<AttributeGroup> = listOf()) {

    @JvmOverloads
    constructor(versionNumber: Int = DEFAULT_VERSION_NUMBER, code: Int, requestId: Int,
                vararg groups: AttributeGroup) : this(versionNumber, code, requestId, listOf(*groups))

    constructor(code: Code, requestId: Int, vararg groups: AttributeGroup):
            this(code = code.code, requestId = requestId, attributeGroups = listOf(*groups))

    /**
     * Return this response packet's Status code
     */
    val status: Status
        get() = getCode(Status.ENCODER)

    /**
     * Return this request packet's Operation code
     */
    val operation: Operation
        get() = getCode(Operation.ENCODER)

    // Return a Enum corresponding to this packet's code.
    private fun <T : Enum> getCode(encoder: EnumType.Encoder<T>): T = encoder[code]

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

    /** Parses packets  */
    interface Parser {
        /** Parse a single packet out of the supplied [DataInputStream]. */
        @Throws(IOException::class)
        fun parse(input: DataInputStream): Packet
    }

    private fun prefix(): String {
        return "Packet(v=x" + Integer.toHexString(versionNumber) +
                " code=" + getCode(Code.ENCODER) +
                " rId=x" + Integer.toHexString(requestId) +
                ")"
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
        val DEFAULT_VERSION_NUMBER = 0x0101

        /** Return a parser with knowledge of specified attribute types  */
        @JvmStatic fun parserOf(attributeTypes: List<AttributeType<*>>): Parser {
            val attributeTypeMap = attributeTypes.map {
                it.name to it
            }.toMap()

            return object : Parser {
                @Throws(IOException::class)
                override fun parse(input: DataInputStream) = input.readPacket(attributeTypeMap)
            }
        }

    }
}

@Throws(IOException::class)
private fun DataInputStream.readPacket(attributeTypes: Map<String, AttributeType<*>>): Packet {

    val versionNumber = readShort().toInt()
    val code = readShort().toInt()
    val requestId = readInt()

    return Packet(versionNumber, code, requestId, { readNextGroup(attributeTypes) }.toList())
}

private fun DataInputStream.readNextGroup(attributeTypes: Map<String, AttributeType<*>>): AttributeGroup? {
    val tag = readTag()
    return when (tag) {
        Tag.endOfAttributes -> null
        else -> {
            if (!tag.isDelimiter) throw ParseError("Illegal delimiter " + tag)
            readGroup(tag, attributeTypes)
        }
    }
}

/** Write a Packet to this [DataOutputStream] as per RFC2910  */
@Throws(IOException::class)
fun DataOutputStream.writePacket(packet: Packet) {
    writeShort(packet.versionNumber)
    writeShort(packet.code)
    writeInt(packet.requestId)
    packet.attributeGroups.forEach(this::writeGroup)
    writeTag(Tag.endOfAttributes)
}
