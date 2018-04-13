// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.util.ParseError

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

/**
 * A type for attribute collections.

 * See RFC3382 (https://tools.ietf.org/html/rfc3382)
 */
open class CollectionType(override val name: String) :
        AttributeType<AttributeCollection>(Encoder, Tag.beginCollection) {

    /** Return a collection containing the supplied attributes */
    fun of(vararg attributes: Attribute<*>) = this(AttributeCollection(attributes.toList()))

    companion object Encoder : com.hp.jipp.encoding.Encoder<AttributeCollection>() {
        override val typeName
            get() = "Collection"

        // Terminates a collection
        private val endCollectionAttribute = OctetStringType(Tag.endCollection, "").empty()

        @Throws(IOException::class)
        override fun writeValue(out: DataOutputStream, value: AttributeCollection) {
            out.writeShort(0) // Empty value

            for (attribute in value.attributes) {
                // Write a memberAttributeName attribute
                Tag.memberAttributeName.write(out)
                out.writeShort(0)
                out.writeString(attribute.name)

                // Write the attribute, but without its name
                attribute.withName("").write(out)
            }

            // Terminating attribute
            endCollectionAttribute.write(out)
        }

        @Throws(IOException::class)
        override fun readValue(input: DataInputStream, finder: Finder, valueTag: Tag): AttributeCollection {
            input.skipValueBytes()
            val builder = ArrayList<Attribute<*>>()

            // Read attribute pairs until endCollection is reached.
            while (true) {
                val tag = Tag.read(input)
                if (tag === Tag.endCollection) {
                    // Skip the rest of this attr and return.
                    input.skipValueBytes()
                    input.skipValueBytes()
                    break
                } else if (tag === Tag.memberAttributeName) {
                    input.skipValueBytes()
                    val memberName = input.readString()
                    val memberTag = Tag.read(input)

                    // Read and throw away the blank attribute name
                    input.readValueBytes()
                    val encoder = finder.find(memberTag, memberName)
                    builder.add(input.readAttribute(encoder, finder, memberTag, memberName))

                } else {
                    throw ParseError("Bad tag in collection: $tag")
                }
            }
            return AttributeCollection(builder)
        }

        override fun valid(valueTag: Tag): Boolean {
            return valueTag === Tag.beginCollection
        }
    }
}
