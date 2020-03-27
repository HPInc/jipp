// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/**
 * A named attribute type which may be used to construct [Attribute] objects containing values of type [T].
 */
interface AttributeType<T : Any> {
    val name: String

    /** Return an attribute containing one or more values of type [T]. */
    fun of(values: Iterable<T>): Attribute<T> {
        val self = this
        return BaseAttribute(self.name, this, values)
    }

    /** Return an attribute containing supplied values. */
    fun of(value: T, vararg values: T): Attribute<T> {
        return of(listOf(value) + values.toList())
    }

    /** Return an empty attribute (containing no values) for this type but substituting a tag. */
    fun empty(
        /** Out-of-bound tag */
        tag: OutOfBandTag
    ): Attribute<T> {
        val self = this
        return BaseAttribute(self.name, this@AttributeType, tag)
    }

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
        val tag = attribute.tag
        return if (tag is OutOfBandTag) {
            // Allow coercion of empty attributes (having an out-of-band tag)
            empty(tag)
        } else {
            val coercedValues = attribute.mapNotNull { coerce(it) }
            if (coercedValues.isNotEmpty()) {
                of(coercedValues)
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
