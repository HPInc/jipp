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
        func: InPacket.() -> Unit
    ) = with(InPacket(DEFAULT_VERSION_NUMBER, operation.code, requestId)) {
        func()
        build()
    }

    operator fun invoke(
        status: Status,
        requestId: Int = DEFAULT_REQUEST_ID,
        func: InPacket.() -> Unit
    ) = with(InPacket(DEFAULT_VERSION_NUMBER, status.code, requestId)) {
        func()
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
    private val groups = ArrayList<InAttributeGroup>()

    /** Allow code to be set/get as a status (for responses). */
    var status: Status
        set(value) { code = value.code }
        get() = Status[code]

    /** Adds an attribute group of [tag]. */
    fun group(
        /** Tag of group to add. */
        tag: Tag,
        /** If true and a group with the same tag exists, extends that group with func(). */
        extend: Boolean = false,
        /** Function to execute on the new group. */
        func: InAttributeGroup.() -> Unit
    ) {
        groups.find { extend && it.tag == tag }?.also { inGroup ->
            inGroup.func()
        } ?: run {
            InAttributeGroup(tag).also {
                groups.add(it)
                it.func()
            }
        }
    }

    /** Add or appends to the operation attributes group. */
    fun operationAttributes(extend: Boolean = false, func: InAttributeGroup.() -> Unit) {
        group(Tag.operationAttributes, extend, func)
    }

    /** Add or appends to the job attributes group. */
    fun jobAttributes(extend: Boolean = false, func: InAttributeGroup.() -> Unit) {
        group(Tag.jobAttributes, extend, func)
    }

    /** Add or appends to the printer attributes group. */
    fun printerAttributes(extend: Boolean = false, func: InAttributeGroup.() -> Unit) {
        group(Tag.printerAttributes, extend, func)
    }

    /** Add or appends to the unsupported attributes group. */
    fun unsupportedAttributes(extend: Boolean = false, func: InAttributeGroup.() -> Unit) {
        group(Tag.unsupportedAttributes, extend, func)
    }

    /** Build the final packet with current values */
    fun build(): IppPacket = IppPacket(versionNumber, code, requestId, groups.map { it.build() })
}

/** DSL for defining an [AttributeGroup]. */
@Suppress("ClassName", "ClassNaming")
object group {
    operator fun invoke(tag: Tag, func: InAttributeGroup.() -> Unit): AttributeGroup {
        val context = InAttributeGroup(tag)
        context.func()
        return context.build()
    }
}

@IppDslMarker
class InAttributeGroup internal constructor(var tag: Tag) : InAttributes() {
    /** Build the final attribute group. */
    internal fun build(): AttributeGroup = AttributeGroup(tag, attributes.values.toList())
}

/** Any context which can receive attributes. */
sealed class InAttributes {
    internal val attributes = mutableMapOf<AttributeType<*>, Attribute<*>>()

    /** Add a list of attributes to append or replace in the current context. */
    fun attr(toAdd: List<Attribute<*>>) {
        attributes.putAll(toAdd.map { it.type!! to it })
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
