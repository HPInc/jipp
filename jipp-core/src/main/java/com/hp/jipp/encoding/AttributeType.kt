// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/**
 * A named attribute type which may be used to construct [Attribute] objects containing values of type [T].
 */
interface AttributeType<T : Any> {
    val name: String

    /** Return an attribute containing one or more values of type [T]. */
    fun of(values: List<T>): Attribute<T> {
        val self = this
        return object : Attribute<T> {
            override val name = self.name
            override val values = values
            override val tag: Tag? = null
            override val type: AttributeType<T> = this@AttributeType
            override fun toString() = "$name = $values"
            override fun equals(other: Any?) =
                if (other is Attribute<*>) equalTo(other) else super.equals(other)
        }
    }

    /** Return an attribute containing supplied values. */
    fun of(value: T, vararg values: T): Attribute<T> {
        return of(listOf(value) + values.toList())
    }

    /** Return an empty attribute (containing no values) for this type but substituting a tag. */
    fun empty(tag: Tag): Attribute<T> {
        val self = this
        return object : Attribute<T> {
            override val name = self.name
            override val values = emptyList<T>()
            override val tag: Tag = tag
            override val type: AttributeType<T> = this@AttributeType
            override fun toString() = "$name($tag)"
            override fun equals(other: Any?) =
                if (other is Attribute<*>) equalTo(other) else super.equals(other)
        }
    }

    /** Return a "no-value" attribute of this type. */
    fun noValue() = empty(Tag.noValue)

    /** Return an "unknown" attribute of this type. */
    fun unknown() = empty(Tag.unknown)

    /** Return an "unsupported" attribute of this type. */
    fun unsupported() = empty(Tag.unsupported)

    /**
     * Coerce an attribute of a different type to this type, if possible.
     * This method is used to convert received attribute data into a more
     * convenient type.
     */
    fun coerce(attribute: Attribute<*>): Attribute<T>? =
        if (attribute.tag != null) {
            // Allow coercion of empty attributes (having an out-of-band tag)
            empty(attribute.tag!!)
        } else {
            val coercedValues = attribute.values.mapNotNull { coerce(it) }
            if (coercedValues.isNotEmpty()) {
                of(coercedValues)
            } else {
                null
            }
        }

    /** Coerce a single value to this attributes type [T], if possible. */
    fun coerce(value: Any): T?
}
