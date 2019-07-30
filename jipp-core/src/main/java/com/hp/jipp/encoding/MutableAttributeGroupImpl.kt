// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/**
 * An implementation of [MutableAttributeGroup].
 */
internal class MutableAttributeGroupImpl(
    override var tag: Tag,
    attributes: List<Attribute<*>>
) : MutableAttributeGroup() {

    private val map: LinkedHashMap<AttributeType<out Any>, Attribute<out Any>> = linkedMapOf()

    init {
        map.putAll(attributes.map { it.type!! to it })
    }

    override val size: Int
        get() = map.size

    override fun get(index: Int): Attribute<*> = map.values.elementAt(index)

    override operator fun get(name: String) =
        map.keys.firstOrNull { it.name == name }?.let { key -> map[key] }

    @Suppress("UNCHECKED_CAST") // We know type corresponds to T because that's all we allow in.
    override fun <T : Any> get(type: AttributeType<T>): Attribute<T>? =
        map[type] as Attribute<T>?

    /** Add attributes to this group. */
    override fun add(attribute: Attribute<out Any>) {
        map[attribute.type!!] = attribute
    }

    /** Remove an attribute of the specified [type], returning true if an attribute was removed. */
    @Suppress("UNCHECKED_CAST") // We know type corresponds to T because that's all we allow in.
    override fun <T : Any> remove(type: AttributeType<T>): Attribute<T>? =
        map.remove(type) as Attribute<T>?

    override fun write(output: IppOutputStream) {
        toGroup().write(output)
    }
}
