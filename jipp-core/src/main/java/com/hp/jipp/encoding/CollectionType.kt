// © Copyright 2017 - 2021 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/** An attribute type for collections of [T]. */
open class CollectionType<T : AttributeCollection>(
    override val name: String,
    private val factory: (UntypedCollection) -> T
) : AttributeType<T> {

    override fun coerce(value: Any) =
        (value as? UntypedCollection)?.let { factory(it) }

    override fun toString() = "CollectionType($name)"

    companion object {
        val codec = Codec<AttributeCollection>(
            Tag.beginCollection,
            {
                skipValueBytes()
                UntypedCollection(readCollectionAttributes())
            },
            {
                writeCollectionAttributes(it.attributes)
            }
        )
    }
}
