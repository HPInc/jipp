package com.hp.jipp.dsl

import com.hp.jipp.encoding.Attribute
import com.hp.jipp.encoding.AttributeGroup
import com.hp.jipp.encoding.AttributeType
import com.hp.jipp.encoding.Tag
import com.hp.jipp.model.Operation
import com.hp.jipp.model.Packet
import com.hp.jipp.model.Packet.Companion.DEFAULT_VERSION_NUMBER

@DslMarker annotation class IppDslMarker

/**
 * DSL for defining an IPP packet. By default, the packet's `versionNumber` is set to
 * [DEFAULT_VERSION_NUMBER] and its `requestId` is set to 1001.
 */
@Suppress("ClassName")
object ippPacket {
    operator fun invoke(operation: Operation, requestId: Int = 1001, init: IppPacketContext.() -> Unit): Packet {
        val context = IppPacketContext(DEFAULT_VERSION_NUMBER, operation.code, requestId)
        context.init()
        return context.build()
    }
}


/**
 * Context for building an IPP [Packet].
 */
@IppDslMarker
class IppPacketContext internal constructor(var versionNumber: Int,
                                            var code: Int, var requestId: Int) {
    private val groups = ArrayList<AttributeGroup>()

    /** Add a new attribute group to the packet */
    fun group(tag: Tag, init: AttributeGroupContext.() -> Unit) {
        groups.add(group.invoke(tag, init))
    }

    /** Build the final packet with current values */
    fun build(): Packet = Packet(versionNumber, code, requestId, groups)
}

/** DSL for defining an AttributeGroup */
@Suppress("ClassName")
object group {
    operator fun invoke(tag: Tag, init: AttributeGroupContext.() -> Unit): AttributeGroup {
        val context = AttributeGroupContext(tag)
        context.init()
        return context.build()
    }
}

@IppDslMarker
class AttributeGroupContext internal constructor(var tag: Tag)  {
    private val attributes = ArrayList<Attribute<*>>()

    /** Add an attribute to the group having a specified value */
    fun <T> attr(attributeType: AttributeType<T>, value: T) {
        attributes.add(attributeType.of(value))
    }

    /** Add an attribute to the group having a number of values */
    fun <T> attr(attributeType: AttributeType<T>, vararg values: T) {
        attributes.add(attributeType.of(values.toList()))
    }

    /** Add a list of attributes to the group */
    fun attrs(list: List<Attribute<*>>) {
        attributes.addAll(list)
    }

    /** Build the final attribute group */
    internal fun build(): AttributeGroup = AttributeGroup(tag, attributes.toList())
}
