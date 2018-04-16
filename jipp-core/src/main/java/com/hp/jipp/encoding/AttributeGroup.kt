// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.util.BuildError
import com.hp.jipp.util.PrettyPrintable
import com.hp.jipp.util.PrettyPrinter

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.HashSet

/** A specific group of attributes found in a packet. */
data class AttributeGroup(val tag: Tag, val attributes: List<Attribute<*>>) : PrettyPrintable {

    init {
        // RFC2910: Within an attribute group, if two or more attributes have the same name, the attribute group
        // is malformed (see [RFC2911] section 3.1.3). Throw if someone attempts this.
        val exist = HashSet<String>()
        for ((_, name) in attributes) {
            if (exist.contains(name)) {
                throw BuildError("Attribute Group contains more than one '$name` in $attributes")
            }
            exist.add(name)
        }
    }

    /** Return a map of attribute names to a list of matching attributes  */
    internal val map: Map<String, Attribute<*>> by lazy {
        attributes.map { it.name to it }.toMap()
    }

    /** Return a attribute from this group.  */
    operator fun <T> get(attributeType: AttributeType<T>): Attribute<T>? {
        val attribute = map[attributeType.name] ?: return null
        return if (attributeType.isValid(attribute)) {
            @Suppress("UNCHECKED_CAST")
            attribute as Attribute<T>
        } else {
            attributeType.of(attribute)
        }
    }

    /**
     * Return all values for the specified attribute type in this group, or an empty list if not present
     */
    fun <T> getValues(attributeType: AttributeType<T>): List<T> =
            get(attributeType)?.values ?: listOf()

    /** Return the first value in this group of the given [AttributeType], or null if none present */
    fun <T> getValue(attributeType: AttributeType<T>): T? =
            get(attributeType)?.values?.get(0)

    override fun print(printer: PrettyPrinter) {
        printer.open(PrettyPrinter.OBJECT, tag.toString())
        printer.addAll(attributes)
        printer.close()
    }

    /** Write a group to the [DataOutputStream] */
    @Throws(IOException::class)
    fun write(stream: IppOutputStream) {
        stream.writeByte(tag.code)
        attributes.forEach { it.write(stream) }
    }

    companion object {

        /** Default encoders available to parse incoming data  */
        @JvmField val encoders = listOf(
                IntegerType.Encoder, UriType.Encoder, StringType.Encoder, BooleanType.Encoder, LangStringType.Encoder,
                CollectionType.Encoder, RangeOfIntegerType.Encoder, ResolutionType.Encoder, OctetStringType.Encoder)

        /** Read a group from the [DataInputStream] */
        @JvmStatic
        @Throws(IOException::class)
        fun read(input: IppInputStream, startTag: Tag): AttributeGroup {
            var more = true
            val attributes = ArrayList<Attribute<*>>()

            while (more) {
                input.mark(1)
                val valueTag = Tag.read(input)
                if (valueTag.isDelimiter) {
                    input.reset()
                    more = false
                } else {
                    attributes.add(input.readAttribute(valueTag))
                }
            }
            return groupOf(startTag, attributes)
        }

        /** Return a new [AttributeGroup] */
        @JvmStatic
        fun groupOf(startTag: Tag, vararg attributes: Attribute<*>) =
                AttributeGroup(startTag, attributes.toList())

        /** Return a new [AttributeGroup] */
        @JvmStatic
        fun groupOf(startTag: Tag, attributes: List<Attribute<*>>) =
                AttributeGroup(startTag, attributes)
    }
}
