package com.hp.jipp.dsl

import com.hp.jipp.encoding.Attribute
import com.hp.jipp.encoding.AttributeGroup
import com.hp.jipp.encoding.AttributeType
import com.hp.jipp.encoding.Tag
import com.hp.jipp.model.Operation
import com.hp.jipp.model.Packet
import com.hp.jipp.model.Packet.Companion.DEFAULT_VERSION_NUMBER
import com.hp.jipp.model.Status

@DslMarker annotation class IppDslMarker

/**
 * DSL for defining an IPP packet. By default, the packet's `versionNumber` is set to
 * [DEFAULT_VERSION_NUMBER] and its `requestId` is set to [DEFAULT_REQUEST_ID].
 */
@Suppress("ClassName", "ClassNaming")
object ippPacket {
    /** The default request ID (1001), which can be overridden with `requestId = 123` */
    const val DEFAULT_REQUEST_ID = 1001
    operator fun invoke(operation: Operation,
                        requestId: Int = DEFAULT_REQUEST_ID,
                        init: IppPacketContext.() -> Unit) =
            with(IppPacketContext(DEFAULT_VERSION_NUMBER, operation.code, requestId)) {
                init()
                build()
            }

    operator fun invoke(status: Status,
                        requestId: Int = DEFAULT_REQUEST_ID,
                        init: IppPacketContext.() -> Unit) =
            with(IppPacketContext(DEFAULT_VERSION_NUMBER, status.code, requestId)) {
                init()
                build()
            }
}

/**
 * Context for building an IPP [Packet].
 */
@IppDslMarker
class IppPacketContext internal constructor(var versionNumber: Int,
                                            var code: Int, var requestId: Int) {
    private val groups = ArrayList<AttributeGroup>()

    /** Allow code to be set/get as a status */
    private var status: Status
        set(value: Status) { code = value.code }
        get() = Status.ENCODER[code]

    /** Add a new attribute group to the packet */
    fun group(tag: Tag, init: AttributeGroupContext.() -> Unit) {
        groups.add(group.invoke(tag, init))
    }

    /** Build the final packet with current values */
    fun build(): Packet = Packet(versionNumber, code, requestId, groups)
}

/** DSL for defining an AttributeGroup */
@Suppress("ClassName", "ClassNaming")
object group {
    operator fun invoke(tag: Tag, init: AttributeGroupContext.() -> Unit): AttributeGroup {
        val context = AttributeGroupContext(tag)
        context.init()
        return context.build()
    }
}

@IppDslMarker
class AttributeGroupContext internal constructor(var tag: Tag) {
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
