// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.encoding.AttributeGroup.Companion.groupOf

/**
 * A group of attributes which may be altered.
 */
@Suppress("TooManyFunctions")
abstract class MutableAttributeGroup : AttributeGroup, AbstractList<Attribute<*>>() {
    abstract override var tag: Tag

    /** Add an attribute to this group. */
    abstract fun add(attribute: Attribute<out Any>)

    /** Add attributes to this group. */
    fun addAll(attributes: Collection<Attribute<out Any>>) {
        attributes.forEach { add(it) }
    }

    /** Add or replace the attribute value for [type]. */
    inline operator fun <reified T : Any> set(type: AttributeType<T>, value: T) {
        add(type.of(value))
    }

    /** Add or replace the attribute value for [type]. */
    inline operator fun <reified T : Any> set(type: AttributeType<T>, values: List<T>) {
        add(type.of(values))
    }

    operator fun plusAssign(attribute: Attribute<out Any>) = add(attribute)

    operator fun plusAssign(attributes: Collection<Attribute<out Any>>) = addAll(attributes)

    /** Remove an attribute of the specified [type], returning true if an attribute was removed. */
    abstract fun <T : Any> remove(type: AttributeType<T>): Attribute<T>?

    /** Return a copy of this object as a non-mutable [AttributeGroup]. */
    fun toGroup(): AttributeGroup = groupOf(tag, toList())

    /** Add a list of attributes to append or replace in the current context. */
    fun attr(toAdd: List<Attribute<*>>) {
        addAll(toAdd)
    }

    /** Add one or more attributes to be appended or replaced in the current context. */
    fun attr(vararg attribute: Attribute<*>) {
        attr(attribute.toList())
    }

    /** Add or replace an attribute to the group having one or more values. */
    fun <T : Any> attr(attributeType: AttributeType<T>, value: T, vararg values: T) {
        if (values.isEmpty()) {
            // Note: must be listOf here or we end up with List<Object> during vararg conversion
            attr(attributeType.of(listOf(value)))
        } else {
            attr(attributeType.of(listOf(value) + values.toList()))
        }
    }

    /** Add or replace an attribute to the group having one or more values. */
    fun attr(attributeType: NameType, value: String, vararg values: String) {
        if (values.isEmpty()) {
            attr(attributeType.of(value))
        } else {
            attr(attributeType.ofStrings(listOf(value) + values.toList()))
        }
    }

    /** Add or replace an attribute to the group having one or more values. */
    fun attr(attributeType: TextType, value: String, vararg values: String) {
        if (values.isEmpty()) {
            attr(attributeType.of(value))
        } else {
            attr(attributeType.ofStrings(listOf(value) + values.toList()))
        }
    }
}
