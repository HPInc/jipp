// Copyright 2017 - 2023 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/**
 * An implementation of [AttributeGroup].
 */
class AttributeGroupImpl(
    override val tag: DelimiterTag,
    attributes: List<Attribute<*>>
) : AttributeGroup, List<Attribute<*>> by attributes.handleDuplicates() {
    private val attributes = toList()

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

    companion object {
        /**
         * Return a list of attributes with the last value of a duplicate, if present.
         *
         * Note: As per RFC8011: The attributes within a group MUST be unique; if an attribute with
         * the same name occurs more than once, the group is malformed (see [RFC8011] section 4.1.3).
         */
        @JvmStatic
        fun List<Attribute<*>>.handleDuplicates(): List<Attribute<*>> {
            return groupBy { it.name }.values.map { it.last() }
        }
    }
}
