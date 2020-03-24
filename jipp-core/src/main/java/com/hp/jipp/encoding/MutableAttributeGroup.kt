// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.encoding.AttributeGroup.Companion.groupOf

/**
 * An [AttributeGroup] which may be altered.
 *
 * Note:
 *   * the addition of any attribute will replace any existing attribute having the same type.
 *   * use [drop] or [minusAssign], not [remove].
 */
@Suppress("TooManyFunctions")
open class MutableAttributeGroup @JvmOverloads constructor(
    /** Tag for this group. */
    override var tag: Tag,
    /** Initial attributes for this group, if any. */
    attributes: List<Attribute<*>> = listOf()
) : AttributeGroup, AbstractList<Attribute<*>>() {

    private val map: LinkedHashMap<String, Attribute<out Any>> = linkedMapOf()

    init {
        map.putAll(attributes.map { it.name to it })
    }

    override val size
        get() = map.size

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

    override fun get(index: Int): Attribute<*> = map.values.elementAt(index)

    override operator fun get(name: String) =
        map[name]

    @Suppress("UNCHECKED_CAST") // We know type corresponds to T because that's all we allow in.
    override fun <T : Any> get(type: AttributeType<T>): Attribute<T>? =
        map[type.name] as Attribute<T>?

    /** Add attributes to this group. */
    fun add(attribute: Attribute<out Any>) {
        map[attribute.name] = attribute
    }

    /** Add attributes to this group. */
    fun addAll(attributes: Collection<Attribute<out Any>>) {
        attributes.forEach { add(it) }
    }

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

    /** Remove an attribute of the specified [type], returning the removed attribute, if any. */
    @Suppress("UNCHECKED_CAST") // We know type corresponds to T because that's all we allow in.
    fun <T : Any> drop(type: AttributeType<T>): Attribute<T>? =
        map.remove(type.name) as Attribute<T>?

    /** Remove [attribute], returning true if it was removed. */
    @Suppress("UNCHECKED_CAST") // We know type corresponds to T because that's all we allow in.
    fun drop(attribute: Attribute<*>): Boolean =
        map.remove(attribute.name) != null

    operator fun <T : Any> minusAssign(type: AttributeType<T>) {
        map.remove(type.name)
    }

    operator fun minusAssign(attribute: Attribute<*>) {
        map.remove(attribute.name)
    }

    /** Return a copy of this object as a non-mutable [AttributeGroup]. */
    fun toGroup(): AttributeGroup = groupOf(tag, toList())

    override fun equals(other: Any?) =
        when {
            other === this -> true
            other is AttributeGroup -> other.tag == tag && map.values.stringinate() == other.stringinate()
            other is List<*> -> map.values.stringinate() == other.stringinate()
            else -> false
        }

    override fun hashCode(): Int {
        // Note: tag is not included because we might need to hash this with other List objects
        return map.values.stringinate().hashCode()
    }
    override fun toString(): String {
        return "MutableAttributeGroup($tag, ${map.values})"
    }
}
