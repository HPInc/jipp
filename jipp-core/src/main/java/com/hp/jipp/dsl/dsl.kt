// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.dsl

import com.hp.jipp.encoding.* // ktlint-disable no-wildcard-imports
import com.hp.jipp.encoding.AttributeGroup.Companion.mutableGroupOf
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
    private val groups = ArrayList<MutableAttributeGroup>()

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
        func: MutableAttributeGroup.() -> Unit
    ) {
        groups.find { extend && it.tag == tag }?.also { inGroup ->
            inGroup.func()
        } ?: run {
            mutableGroupOf(tag).also {
                groups.add(it)
                it.func()
            }
        }
    }

    /** Add or appends to the operation attributes group. */
    fun operationAttributes(extend: Boolean = false, func: MutableAttributeGroup.() -> Unit) {
        group(Tag.operationAttributes, extend, func)
    }

    /** Add or appends to the job attributes group. */
    fun jobAttributes(extend: Boolean = false, func: MutableAttributeGroup.() -> Unit) {
        group(Tag.jobAttributes, extend, func)
    }

    /** Add or appends to the printer attributes group. */
    fun printerAttributes(extend: Boolean = false, func: MutableAttributeGroup.() -> Unit) {
        group(Tag.printerAttributes, extend, func)
    }

    /** Add or appends to the unsupported attributes group. */
    fun unsupportedAttributes(extend: Boolean = false, func: MutableAttributeGroup.() -> Unit) {
        group(Tag.unsupportedAttributes, extend, func)
    }

    /** Build the final packet with current values */
    fun build(): IppPacket = IppPacket(versionNumber, code, requestId, groups.map { it.toGroup() })
}

/** DSL for defining an [AttributeGroup]. */
@Suppress("ClassName", "ClassNaming")
object group {
    operator fun invoke(tag: Tag, func: MutableAttributeGroup.() -> Unit): AttributeGroup {
        val context = mutableGroupOf(tag)
        context.func()
        return context.toGroup()
    }
}
