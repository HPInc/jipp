package com.hp.jipp.encoding

import com.google.common.collect.ImmutableList
import com.hp.jipp.util.ParseError

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

/**
 * A type for attribute collections.

 * @see [RFC3382](https://tools.ietf.org/html/rfc3382)
 */
class CollectionType(name: String) : AttributeType<AttributeCollection>(CollectionType.ENCODER, Tag.BeginCollection, name) {
    companion object : IppEncodings {
        private val TYPE_NAME = "Collection"

        /** Used to terminate a collection  */
        private val EndCollectionAttribute = OctetStringType(Tag.EndCollection, "").of()

        @JvmField val ENCODER: Attribute.BaseEncoder<AttributeCollection> = object : Attribute.BaseEncoder<AttributeCollection>() {

            override val type: String
                get() = TYPE_NAME

            @Throws(IOException::class)
            override fun writeValue(out: DataOutputStream, value: AttributeCollection) {
                out.writeShort(0) // Empty value

                for (attribute in value.attributes) {
                    // Write a MemberAttributeName attribute
                    Tag.MemberAttributeName.write(out)
                    out.writeShort(0)
                    out.writeString(attribute.name)

                    // Write the attribute, but without its name
                    attribute.withName("").write(out)
                }

                // Terminating attribute
                EndCollectionAttribute.write(out)
            }

            @Throws(IOException::class)
            override fun readValue(input: DataInputStream, finder: Attribute.EncoderFinder, valueTag: Tag): AttributeCollection {
                input.skipValueBytes()
                val builder = ImmutableList.Builder<Attribute<*>>()

                // Read attribute pairs until EndCollection is reached.
                while (true) {
                    val tag = Tag.read(input)
                    if (tag === Tag.EndCollection) {
                        // Skip the rest of this attr and return.
                        input.skipValueBytes()
                        input.skipValueBytes()
                        break
                    } else if (tag === Tag.MemberAttributeName) {
                        input.skipValueBytes()
                        val memberName = input.readString()
                        val memberTag = Tag.read(input)

                        // Read and throw away the blank attribute name
                        input.readValueBytes()
                        builder.add(finder.find(memberTag, memberName).read(input, finder, memberTag, memberName))

                    } else {
                        throw ParseError("Bad tag in collection: " + tag)
                    }
                }
                return AttributeCollection(builder.build())
            }

            override fun valid(valueTag: Tag): Boolean {
                return valueTag === Tag.BeginCollection
            }
        }
    }
}
