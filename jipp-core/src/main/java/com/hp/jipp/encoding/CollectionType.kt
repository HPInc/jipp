// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.encoding.AttributeGroup.Companion.readAnyAttribute
import com.hp.jipp.encoding.AttributeGroup.Companion.readTag
import com.hp.jipp.encoding.AttributeGroup.Companion.writeAttribute
import com.hp.jipp.util.ParseError

/** An attribute type for collections of [T]. */
open class CollectionType<T : AttributeCollection>(
    override val name: String,
    private val factory: (UntypedCollection) -> T
) : AttributeType<T> {

    override fun coerce(value: Any) =
        if (value is UntypedCollection) {
            factory(value)
        } else {
            null
        }

    override fun toString() = "CollectionType($name)"

    companion object {

        val codec = Codec<AttributeCollection>(Tag.beginCollection, {
                skipValueBytes()
                UntypedCollection(readCollectionAttributes())
            }, {
                writeShort(0) // Empty value
                for (attribute in it.attributes) {
                    writeTag(Tag.memberAttributeName)
                    writeShort(0)
                    writeString(attribute.name)
                    /** Write the attribute with a blank name */
                    writeAttribute(this, attribute, name = "")
                }
                writeAttribute(this, endCollectionAttribute)
            })

        private val endCollectionAttribute = EmptyAttribute("", Tag.endCollection)

        private fun IppInputStream.readCollectionAttributes(): List<Attribute<*>> {
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
                        val memberTag = readTag()
                        // Read and throw away the (blank) attribute value
                        readValueBytes()
                        attributes.add(readAnyAttribute(memberName, memberTag))
                    }
                    else ->
                        throw ParseError("Bad tag in collection: $tag")
                }
            }
        }
    }
}
