// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.dsl

import com.hp.jipp.encoding.* // ktlint-disable no-wildcard-imports
import com.hp.jipp.encoding.IppPacket.Companion.DEFAULT_VERSION_NUMBER
import com.hp.jipp.model.Operation
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
    operator fun invoke(
        operation: Operation,
        requestId: Int = DEFAULT_REQUEST_ID,
        init: InPacket.() -> Unit
    ) = with(InPacket(DEFAULT_VERSION_NUMBER, operation.code, requestId)) {
        init()
        build()
    }

    operator fun invoke(
        status: Status,
        requestId: Int = DEFAULT_REQUEST_ID,
        init: InPacket.() -> Unit
    ) = with(InPacket(DEFAULT_VERSION_NUMBER, status.code, requestId)) {
        init()
        build()
    }
}

/**
 * Context for building an IPP [IppPacket].
 */
@IppDslMarker
class InPacket constructor(
    var versionNumber: Int = DEFAULT_VERSION_NUMBER,
    var code: Int,
    var requestId: Int = ippPacket.DEFAULT_REQUEST_ID
) {
    private val groups = ArrayList<AttributeGroup>()

    /** Allow code to be set/get as a status */
    private var status: Status
        set(value) { code = value.code }
        get() = Status[code]

    /** Add a new attribute group to the packet */
    fun group(tag: Tag, init: InAttributeGroup.() -> Unit) {
        groups.add(group.invoke(tag, init))
    }

    /** Add an operation attributes group */
    fun operationAttributes(init: InAttributeGroup.() -> Unit) {
        groups.add(group.invoke(Tag.operationAttributes, init))
    }

    /** Add a job attributes group. */
    fun jobAttributes(init: InAttributeGroup.() -> Unit) {
        groups.add(group.invoke(Tag.jobAttributes, init))
    }

    /** Add a printer attributes group. */
    fun printerAttributes(init: InAttributeGroup.() -> Unit) {
        groups.add(group.invoke(Tag.printerAttributes, init))
    }

    /** Add an unsupported attributes group. */
    fun unsupportedAttributes(init: InAttributeGroup.() -> Unit) {
        groups.add(group.invoke(Tag.unsupportedAttributes, init))
    }

    /** Build the final packet with current values */
    fun build(): IppPacket = IppPacket(versionNumber, code, requestId, groups)
}

/** DSL for defining an AttributeGroup */
@Suppress("ClassName", "ClassNaming")
object group {
    operator fun invoke(tag: Tag, init: InAttributeGroup.() -> Unit): AttributeGroup {
        val context = InAttributeGroup(tag)
        context.init()
        return context.build()
    }
}

@IppDslMarker
class InAttributeGroup internal constructor(var tag: Tag) : InAttributes() {
    /** Build the final attribute group */
    internal fun build(): AttributeGroup = AttributeGroup(tag, attributes.toList())
}

/** Any context which can receive attributes */
sealed class InAttributes {
    internal val attributes = ArrayList<Attribute<*>>()

    /** Add one or more attributes to the current context */
    fun attr(vararg attribute: Attribute<*>) {
        attr(attribute.toList())
    }

    /** Add a list of attributes to the current context */
    fun attr(attributes: List<Attribute<*>>) {
        this.attributes.addAll(attributes)
    }

    /** Add an attribute to the group having one or more values */
    fun <T : Any> attr(attributeType: AttributeType<T>, value: T, vararg values: T) {
        if (values.isEmpty()) {
            // Note: must be listOf here or we end up with List<Object> during vararg conversion
            attr(attributeType.of(listOf(value)))
        } else {
            attr(attributeType.of(listOf(value) + values.toList()))
        }
    }

    fun attr(attributeType: NameType, value: String, vararg values: String) {
        if (values.isEmpty()) {
            attr(attributeType.of(value))
        } else {
            attr(attributeType.ofStrings(listOf(value) + values.toList()))
        }
    }

    fun attr(attributeType: TextType, value: String, vararg values: String) {
        if (values.isEmpty()) {
            attr(attributeType.of(value))
        } else {
            attr(attributeType.ofStrings(listOf(value) + values.toList()))
        }
    }
}
