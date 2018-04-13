// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.dsl

import com.hp.jipp.encoding.* // ktlint-disable no-wildcard-imports
import com.hp.jipp.model.Operation
import com.hp.jipp.model.IppPacket
import com.hp.jipp.model.IppPacket.Companion.DEFAULT_VERSION_NUMBER
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
 * Context for building an IPP [IppPacket].
 */
@IppDslMarker
class IppPacketContext constructor(var versionNumber: Int = DEFAULT_VERSION_NUMBER,
                                   var code: Int, var requestId: Int = ippPacket.DEFAULT_REQUEST_ID) {
    private val groups = ArrayList<AttributeGroup>()

    /** Allow code to be set/get as a status */
    private var status: Status
        set(value) { code = value.code }
        get() = Status.Encoder[code]

    /** Add a new attribute group to the packet */
    fun group(tag: Tag, init: AttributeGroupContext.() -> Unit) {
        groups.add(group.invoke(tag, init))
    }

    /** Add an operation attributes group */
    fun operationAttributes(init: AttributeGroupContext.() -> Unit) {
        groups.add(group.invoke(Tag.operationAttributes, init))
    }

    fun jobAttributes(init: AttributeGroupContext.() -> Unit) {
        groups.add(group.invoke(Tag.jobAttributes, init))
    }

    /** Build the final packet with current values */
    fun build(): IppPacket = IppPacket(versionNumber, code, requestId, groups)
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

/** Any context which can receive attributes */
sealed class AttributesContext {
    internal val attributes = ArrayList<Attribute<*>>()

    fun attr(vararg attribute: Attribute<*>) {
        attributes.addAll(attribute.toList())
    }

    /** Add an attribute to the group having one or more values */
    fun <T> attr(attributeType: AttributeType<T>, value: T, vararg values: T) {
        if (values.isEmpty()) {
            attr(attributeType.of(value))
        } else {
            attr(attributeType.of(listOf(value) + values.toList()))
        }
    }

    /** Add a collection */
    fun col(collectionType: CollectionType, init: CollectionContext.() -> Unit) {
        attr(collectionType(CollectionContext().let {
            it.init()
            it.build()
        }))
    }
}

/** Return a new Collection based on a collection type and the contents supplied in the block */
operator fun CollectionType.invoke(init: CollectionContext.() -> Unit) =
        this(CollectionContext().let {
            it.init()
            it.build()
        })

@IppDslMarker
class AttributeGroupContext internal constructor(var tag: Tag) : AttributesContext() {
    /** Build the final attribute group */
    internal fun build(): AttributeGroup = AttributeGroup(tag, attributes.toList())
}

@IppDslMarker
class CollectionContext : AttributesContext() {
    /** Build the final collection */
    internal fun build(): AttributeCollection = AttributeCollection(attributes.toList())
}
