// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.util.BuildError

/** Overridable implementation for any subclass of [Attribute]. */
open class AttributeImpl<T : Any>(
    final override val name: String,
    final override val type: AttributeType<T>,
    private val values: List<T>
) : Attribute<T>, List<T> by values {

    /** Construct an empty (no-value) attribute for a specific attribute type. */
    constructor(name: String, type: EmptyAttributeType<T>) : this(name, type, emptyList())

    init {
        if (values.isEmpty() && type !is EmptyAttributeType) {
            throw BuildError("Attribute must have values or an out-of-band tag")
        }
    }

    override fun getValue(): T? = if (values.isEmpty()) null else values[0]

    override fun toString() =
        when (values.size) {
            0 -> "$name(${(type as EmptyAttributeType).tag.name})"
            1 -> "$name=${values[0]}"
            else -> "$name=$values"
        }

    override fun equals(other: Any?) =
        if (other === this) {
            true
        } else when (other) {
            is Attribute<*> -> {
                val otherType = other.type
                if (otherType is EmptyAttributeType<*> || type is EmptyAttributeType<*>) {
                    // If either is empty then they must both be empty and have the same tag
                    otherType is EmptyAttributeType<*> && type is EmptyAttributeType<*> &&
                        otherType.tag == type.tag
                } else {
                    // Otherwise just make sure their names, type names, and values align
                    other.name == name && other.type.name == type.name && values.stringinate() == other.stringinate()
                }
            }
            is List<*> ->
                // When comparing against any other list, just compare values
                values == other
            else -> false
        }

    override fun hashCode(): Int {
        // We do not consider name/tag when hashing because equals==true must also result in equal hashes
        return values.stringinate().hashCode()
    }
}
