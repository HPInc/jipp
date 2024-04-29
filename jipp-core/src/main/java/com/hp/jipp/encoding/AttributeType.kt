// Copyright 2017 - 2020 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/**
 * A named attribute type which may be used to construct [Attribute] objects containing 0 or one values of type [T].
 */
interface AttributeType<T : Any> {
    val name: String

    /** Return an [Attribute] of this type containing a single value. */
    fun of(value: T): Attribute<T> =
        AttributeImpl(name, this, listOf(value))

    /** Return an empty attribute (containing no values) for this type but substituting a tag. */
    fun empty(tag: OutOfBandTag): Attribute<T> =
        EmptyAttribute<T>(name, tag)

    /** Return a "no-value" attribute of this type. */
    fun noValue() = empty(Tag.noValue)

    /** Return an "unknown" attribute of this type. */
    fun unknown() = empty(Tag.unknown)

    /** Return an "unsupported" attribute of this type. */
    fun unsupported() = empty(Tag.unsupported)

    /**
     * Convert an attribute of a different type to use [T], if possible.
     */
    fun coerce(attribute: Attribute<*>): Attribute<T>? {
        val otherType = attribute.type
        return if (otherType is EmptyAttributeType) {
            // Allow coercion of empty attributes (having an out-of-band tag)
            empty(otherType.tag)
        } else {
            val coercedValues = attribute.mapNotNull { coerce(it) }
            if (coercedValues.isNotEmpty()) {
                AttributeImpl(name, this, coercedValues)
            } else {
                null
            }
        }
    }

    /**
     * Convert any single value to [T], if possible.
     */
    fun coerce(value: Any): T?
}
