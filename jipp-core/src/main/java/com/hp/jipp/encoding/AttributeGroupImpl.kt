// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.util.BuildError
import java.util.HashSet

/**
 * An implementation of [AttributeGroup].
 */
class AttributeGroupImpl(
    override val tag: DelimiterTag,
    private val attributes: List<Attribute<*>>
) : AttributeGroup, List<Attribute<*>> by attributes {

    /** Return the attribute corresponding to the specified [name]. */
    override operator fun get(name: String): Attribute<*>? = firstOrNull { it.name == name }

    /** Return the attribute as conforming to the supplied attribute type. */
    override operator fun <T : Any> get(type: AttributeType<T>): Attribute<T>? =
        get(type.name)?.let {
            type.coerce(it)
        }

    override fun equals(other: Any?) =
        when {
            other === this -> true
            other is AttributeGroup -> other.tag == tag && attributes == other
            other is List<*> -> attributes == other
            else -> false
        }

    override fun hashCode(): Int {
        // Note: tag is not included because we might need to hash this with other List objects
        return attributes.hashCode()
    }

    override fun toString(): String {
        return "AttributeGroup($tag, $attributes)"
    }
}
