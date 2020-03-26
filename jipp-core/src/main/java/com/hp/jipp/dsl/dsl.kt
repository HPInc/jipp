// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.dsl

import com.hp.jipp.encoding.AttributeGroup
import com.hp.jipp.encoding.AttributeGroup.Companion.mutableGroupOf
import com.hp.jipp.encoding.IppPacket
import com.hp.jipp.encoding.IppPacket.Companion.DEFAULT_REQUEST_ID
import com.hp.jipp.encoding.IppPacket.Companion.DEFAULT_VERSION_NUMBER
import com.hp.jipp.encoding.MutableAttributeGroup
import com.hp.jipp.encoding.Tag
import com.hp.jipp.model.Operation
import com.hp.jipp.model.Status

@DslMarker annotation class IppDslMarker

/**
 * DSL for defining an IPP packet. By default, the packet's `versionNumber` is set to
 * [DEFAULT_VERSION_NUMBER] and its `requestId` is set to [DEFAULT_REQUEST_ID].
 */
@Suppress("ClassName", "ClassNaming")
@Deprecated("Use IppPacket builders")
object ippPacket {
    /** The default request ID (1001), which can be overridden with `requestId = 123` */
    @Suppress("DEPRECATION")
    operator fun invoke(
        operation: Operation,
        requestId: Int = DEFAULT_REQUEST_ID,
        func: InPacket.() -> Unit
    ) = with(InPacket(DEFAULT_VERSION_NUMBER, operation.code, requestId)) {
        func()
        build()
    }

    @Suppress("DEPRECATION")
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
@Deprecated("Use IppPacket builders")
class InPacket constructor(
    var versionNumber: Int = DEFAULT_VERSION_NUMBER,
    var code: Int,
    var requestId: Int = DEFAULT_REQUEST_ID
) {
    private val groups = ArrayList<MutableAttributeGroup>()

    /** Allow set/get of request [Operation] (stored in [code]). */
    var operation: Operation
        set(value) { code = value.code }
        get() = Operation[code]

    /** Allow set/get of response [Status] (stored in [code]). */
    var status: Status
        set(value) { code = value.code }
        get() = Status[code]

    /** Append a new [AttributeGroup] of [tag] as filled out by [func]. */
    fun group(tag: Tag, func: MutableAttributeGroup.() -> Unit) {
        mutableGroupOf(tag).also {
            groups.add(it)
            it.func()
        }
    }

    /**
     * If a group with [tag] exists, extend the last group matching [tag] with [func],
     * otherwise add a new group.
     */
    fun extend(tag: Tag, func: MutableAttributeGroup.() -> Unit) {
        groups.findLast { it.tag == tag }?.also { inGroup ->
            inGroup.func()
        } ?: group(tag, func)
    }

    /** Add a copy of [group] to the packet. */
    fun group(group: AttributeGroup) {
        groups.add(group.toMutable())
    }

    /** Add or appends to the operation attributes group. */
    fun operationAttributes(func: MutableAttributeGroup.() -> Unit) {
        group(Tag.operationAttributes, func)
    }

    /** Add or appends to the job attributes group. */
    fun jobAttributes(func: MutableAttributeGroup.() -> Unit) {
        group(Tag.jobAttributes, func)
    }

    /** Add or appends to the printer attributes group. */
    fun printerAttributes(func: MutableAttributeGroup.() -> Unit) {
        group(Tag.printerAttributes, func)
    }

    /** Add or appends to the unsupported attributes group. */
    fun unsupportedAttributes(func: MutableAttributeGroup.() -> Unit) {
        group(Tag.unsupportedAttributes, func)
    }

    /** Build the final packet with current values */
    fun build(): IppPacket = IppPacket(versionNumber, code, requestId, groups.map { it.toGroup() })
}

/** DSL for defining an [AttributeGroup]. */
@Suppress("ClassName", "ClassNaming")
@Deprecated("Use IppPacket builders")
object group {
    operator fun invoke(tag: Tag, func: MutableAttributeGroup.() -> Unit) =
        mutableGroupOf(tag).apply { func() }.toGroup()
}
