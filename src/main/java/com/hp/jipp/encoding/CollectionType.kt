package com.hp.jipp.encoding

import com.hp.jipp.util.ParseError

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

/**
 * A type for attribute collections.

 * @see [RFC3382](https://tools.ietf.org/html/rfc3382)
 */
class CollectionType(name: String) :
        AttributeType<AttributeCollection>(CollectionType.ENCODER, Tag.beginCollection, name) {
    companion object {
        private val TYPE_NAME = "Collection"

        // Terminates a collection
        private val endCollectionAttribute = OctetStringType(Tag.endCollection, "").of()

        @JvmField val ENCODER: Encoder<AttributeCollection> = object : Encoder<AttributeCollection>() {

            override val type: String
                get() = TYPE_NAME

            @Throws(IOException::class)
            override fun writeValue(out: DataOutputStream, value: AttributeCollection) {
                out.writeShort(0) // Empty value

                for (attribute in value.attributes) {
                    // Write a memberAttributeName attribute
                    out.writeTag(Tag.memberAttributeName)
                    out.writeShort(0)
                    out.writeString(attribute.name)

                    // Write the attribute, but without its name
                    out.writeAttribute(attribute.withName(""))
                }

                // Terminating attribute
                out.writeAttribute(endCollectionAttribute)
            }

            @Throws(IOException::class)
            override fun readValue(input: DataInputStream, finder: Finder, valueTag: Tag): AttributeCollection {
                input.skipValueBytes()
                val builder = ArrayList<Attribute<*>>()

                // Read attribute pairs until endCollection is reached.
                while (true) {
                    val tag = input.readTag()
                    if (tag === Tag.endCollection) {
                        // Skip the rest of this attr and return.
                        input.skipValueBytes()
                        input.skipValueBytes()
                        break
                    } else if (tag === Tag.memberAttributeName) {
                        input.skipValueBytes()
                        val memberName = input.readString()
                        val memberTag = input.readTag()

                        // Read and throw away the blank attribute name
                        input.readValueBytes()
                        val encoder = finder.find(memberTag, memberName)
                        builder.add(input.readAttribute(encoder, finder, memberTag, memberName))

                    } else {
                        throw ParseError("Bad tag in collection: " + tag)
                    }
                }
                return AttributeCollection(builder)
            }

            override fun valid(valueTag: Tag): Boolean {
                return valueTag === Tag.beginCollection
            }
        }
    }
}
