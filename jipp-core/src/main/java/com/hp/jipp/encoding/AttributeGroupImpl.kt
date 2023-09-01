// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import java.util.HashSet

/**
 * An implementation of [AttributeGroup].
 */
class AttributeGroupImpl(
    override val tag: DelimiterTag,
    private val attributes: MutableList<Attribute<*>>
) : AttributeGroup, List<Attribute<*>> by attributes {

    init {
        // RFC8011: The attributes within a group MUST be unique; if an attribute with
        // the same name occurs more than once, the group is malformed (see [RFC8011] section 4.1.3).
        // Select the last value for the duplicate attribute.
        val names = HashSet<String>()
        val duplicateAttributes = mutableListOf<Attribute<*>>()
        for (attribute in attributes) {
            val name = attribute.name
            if (names.contains(name) && !duplicateAttributes.any { it.name == name }) {
                attributes.findLast { it.name == name }?.let { duplicateAttributes.add(it) }
            }
            names.add(name)
        }

        duplicateAttributes.forEach { duplicate ->
            attributes.removeAll { duplicate.name == it.name }
            attributes.add(duplicate)
        }
    }

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
