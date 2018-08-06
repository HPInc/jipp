// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.util.BuildError

/** Overridable implementation for any subclass of [Attribute]. */
open class BaseAttribute<T : Any>(
    override val name: String,
    override val type: AttributeType<T>?,
    final override val tag: Tag?,
    private val values: List<T>
) : Attribute<T>, List<T> by values {

    /** Construct an attribute for a specific attribute type containing certain values. */
    constructor(name: String, type: AttributeType<T>, values: List<T>) : this(name, type, null, values)

    /** Construct an empty (no-value) attribute for a specific attribute type. */
    constructor(name: String, type: AttributeType<T>, tag: Tag) : this(name, type, tag, emptyList())

    /** Construct an attribute with unknown attribute type, containing certain values. */
    constructor(name: String, values: List<T>) : this(name, null, null, values)

    /** Construct an empty (no-value) attribute for an unknown attribute type. */
    constructor(name: String, tag: Tag) : this(name, null, tag, emptyList())

    init {
        if (values.isEmpty() && !tagAllowsEmpty(tag)) {
            throw BuildError("Attribute must have values or an out-of-band tag")
        }
    }

    private fun tagAllowsEmpty(tag: Tag?) =
        when {
            tag == null -> false
            tag.isCollection -> true
            tag.isOutOfBand -> true
            else -> false
        }

    override fun getValue(): T? = if (values.isEmpty()) null else values[0]

    override fun toString() = if (tag == null) {
        "$name = $values"
    } else {
        "$name($tag)"
    }

    override fun equals(other: Any?) =
        if (other === this) {
            true
        } else when (other) {
            is Attribute<*> ->
                // When comparing an attribute, evaluate its name and tag in addition to values
                other.name == name && other.tag == tag && values == other
            is List<*> ->
                // When comparing against any other list, just compare values
                values == other
            else -> false
        }

    override fun hashCode(): Int {
        // We do not consider name/tag when hashing because equals==true must also result in equal hashes
        return values.hashCode()
    }
}
