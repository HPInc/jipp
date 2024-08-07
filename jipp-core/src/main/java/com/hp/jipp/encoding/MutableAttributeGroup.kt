// Copyright 2017 - 2020 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.encoding.AttributeGroup.Companion.groupOf

/**
 * An [AttributeGroup] which may be altered. Insertion order is preserved.
 *
 * Note:
 *   * the addition of any [Attribute] replaces any existing one with the same [AttributeType].
 *   * use [drop] or [minusAssign], not [remove].
 */
@Suppress("TooManyFunctions")
open class MutableAttributeGroup @JvmOverloads constructor(
    /** Tag for this group. */
    override var tag: DelimiterTag,
    /** Initial attributes for this group, if any. */
    attributes: List<Attribute<*>> = listOf()
) : AttributeGroup, AbstractList<Attribute<*>>() {

    private val map: LinkedHashMap<String, Attribute<*>> = linkedMapOf()

    init {
        putAll(attributes)
    }

    override val size
        get() = map.size

    /** Allow a DSL-like syntax where the contents of this group can be mutated. */
    operator fun invoke(mutator: MutableAttributeGroup.() -> Unit) = apply {
        mutator()
    }

    /** Put [attribute] into this group */
    operator fun plusAssign(attribute: Attribute<*>) = put(attribute)

    /** Put [attributes] into this group */
    operator fun plusAssign(attributes: Iterable<Attribute<*>>) = putAll(attributes)

    /** Return the [Attribute] at [index]. */
    override fun get(index: Int): Attribute<*> = map.values.elementAt(index)

    /** Return the [Attribute] having a type of [name]. */
    override operator fun get(name: String) =
        map[name]

    /** Return the [Attribute] having [type] if any. */
    @Suppress("UNCHECKED_CAST") // We know type corresponds to T because that's all we allow in.
    override fun <T : Any> get(type: AttributeType<T>): Attribute<T>? =
        map[type.name]?.let { type.coerce(it) }

    /** Assign an attribute from its native value type. */
    operator fun <T : Any> set(type: AttributeType<T>, value: T) {
        map[type.name] = type.of(value)
    }

    /** Assign an attribute with multiple values from its native value type. */
    operator fun <T : Any> set(type: AttributeSetType<T>, values: Iterable<T>) {
        map[type.name] = type.of(values)
    }

    /** Assign an attribute from its native value type. */
    fun <T : Any> put(type: AttributeType<T>, value: T) {
        map[type.name] = type.of(value)
    }

    /** Assign an attribute from one or more values. */
    fun <T : Any> put(type: AttributeSetType<T>, values: Iterable<T>) {
        map[type.name] = type.of(values)
    }

    /** Assign an attribute from one or more values. */
    fun <T : Any> put(type: AttributeSetType<T>, value: T, vararg values: T) {
        map[type.name] = type.of(listOf(value) + values)
    }

    /** Put [attribute] into this group. */
    fun <T : Any> put(attribute: Attribute<T>) {
        map[attribute.name] = attribute
    }

    /** Put [attributes] into this group. */
    fun put(vararg attributes: Attribute<*>) {
        for (attribute in attributes) {
            map[attribute.name] = attribute
        }
    }

    /** Add or replace an attribute having one value to the group. */
    fun put(type: NameType, value: String) {
        put(type.of(value))
    }

    /** Add or replace an attribute to the group having one or more values. */
    fun put(type: NameType.Set, value: String, vararg values: String) {
        put(type.of((listOf(value) + values.toList()).map { Name(it) }))
    }

    /** Add or replace an attribute having one value to the group. */
    fun put(type: KeywordOrNameType, value: String) {
        put(type.of(value))
    }

    /** Add or replace an attribute to the group having one or more values. */
    fun put(type: KeywordOrNameType.Set, value: String, vararg values: String) {
        put(type.of((listOf(value) + values.toList()).map { KeywordOrName(it) }))
    }

    /** Add or replace an attribute having one value to the group. */
    fun put(textType: TextType, value: String) {
        put(textType.of(value))
    }

    /** Add or replace an attribute to the group having one or more values. */
    fun put(textType: TextType.Set, value: String, vararg values: String) {
        put(textType.of((listOf(value) + values.toList()).map { Text(it) }))
    }

    /** Put [attributes] into this group. */
    fun putAll(attributes: Iterable<Attribute<*>>) {
        attributes.forEach {
            map[it.name] = it
        }
    }

    @Deprecated("use put()", ReplaceWith("put(attribute)"))
    fun addAll(@Suppress("UNUSED_PARAMETER") index: Int, attributes: List<Attribute<*>>) {
        putAll(attributes)
    }

    @Deprecated("use put()", ReplaceWith("put(attribute)"))
    fun add(@Suppress("UNUSED_PARAMETER") index: Int, attribute: Attribute<*>) {
        put(attribute)
    }

    /** Add attributes to this group. */
    @Deprecated("use put()", ReplaceWith("put(attributeType.of(value, values...))"))
    fun add(attribute: Attribute<*>) {
        put(attribute)
    }

    /** Add attributes to this group. */
    @Deprecated("use put()", ReplaceWith("put(attributeType.of(value, values...))"))
    fun add(vararg attributes: Attribute<*>) {
        putAll(attributes.toList())
    }

    /** Add attributes to this group. */
    @Deprecated("use putAll()", ReplaceWith("putAll(attributes)"))
    fun addAll(attributes: Iterable<Attribute<*>>) {
        putAll(attributes)
    }

    /** Add a list of attributes to append or replace in the current context. */
    @Deprecated("use putAll()", ReplaceWith("putAll(toAdd)"))
    fun attr(toAdd: List<Attribute<*>>) {
        putAll(toAdd)
    }

    /** Add one or more attributes to be appended or replaced in the current context. */
    @Deprecated("use put()", ReplaceWith("put(attribute)"))
    fun attr(vararg attribute: Attribute<*>) {
        putAll(attribute.toList())
    }

    /** Add or replace an attribute having one value to the group. */
    @Deprecated("use put()", ReplaceWith("put(attributeType, value)"))
    fun <T : Any> attr(attributeType: AttributeType<T>, value: T) {
        put(attributeType.of(value))
    }

    /** Add or replace an attribute having one or more values to the group. */
    @Deprecated("use put()", ReplaceWith("put(attributeType, value, values)"))
    fun <T : Any> attr(attributeType: AttributeSetType<T>, value: T, vararg values: T) {
        put(attributeType.of(listOf(value) + values.toList()))
    }

    /** Add or replace an attribute having one value to the group. */
    @Deprecated("use put()", ReplaceWith("put(attributeType, value)"))
    fun attr(nameType: NameType, value: String) {
        put(nameType.of(value))
    }

    /** Add or replace an attribute to the group having one or more values. */
    @Deprecated("use put()", ReplaceWith("put(attributeType, value, values)"))
    fun attr(nameType: NameType.Set, value: String, vararg values: String) {
        put(nameType.of((listOf(value) + values.toList()).map { Name(it) }))
    }

    /** Add or replace an attribute having one value to the group. */
    @Deprecated("use put()", ReplaceWith("put(attributeType, value)"))
    fun attr(textType: TextType, value: String) {
        put(textType.of(value))
    }

    /** Add or replace an attribute to the group having one or more values. */
    @Deprecated("use put()", ReplaceWith("put(attributeType, value, values)"))
    fun attr(textType: TextType.Set, value: String, vararg values: String) {
        put(textType.of((listOf(value) + values.toList()).map { Text(it) }))
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
