// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import java.util.HashSet

/**
 * An implementation of [AttributeGroup].
 */
class AttributeGroupImpl(
    override val tag: DelimiterTag,
    private val attributes: List<Attribute<*>>,
    private val attributeList: MutableList<Attribute<*>> = mutableListOf()
) : AttributeGroup, List<Attribute<*>> by attributeList {

    init {
        // RFC8011: The attributes within a group MUST be unique; if an attribute with
        // the same name occurs more than once, the group is malformed (see [RFC8011] section 4.1.3).
        // Select the last value for the duplicate attribute.
        attributeList.clear()
        val names = HashSet<String>()
        for (attribute in attributes) {
            val name = attribute.name
            if (!names.contains(name)) {
                attributeList.add(attributes.last { it.name == name})
            }
            names.add(name)
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
            other is AttributeGroup -> other.tag == tag && attributeList == other
            other is List<*> -> attributeList == other
            else -> false
        }

    override fun hashCode(): Int {
        // Note: tag is not included because we might need to hash this with other List objects
        return attributeList.hashCode()
    }

    override fun toString(): String {
        return "AttributeGroup($tag, $attributeList)"
    }
}
