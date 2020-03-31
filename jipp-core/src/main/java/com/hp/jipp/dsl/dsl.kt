// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.dsl

import com.hp.jipp.encoding.AttributeGroup
import com.hp.jipp.encoding.AttributeGroup.Companion.mutableGroupOf
import com.hp.jipp.encoding.DelimiterTag
import com.hp.jipp.encoding.IppPacket
import com.hp.jipp.encoding.IppPacket.Companion.DEFAULT_REQUEST_ID
import com.hp.jipp.encoding.IppPacket.Companion.DEFAULT_VERSION_NUMBER
import com.hp.jipp.encoding.MutableAttributeGroup
import com.hp.jipp.model.Operation
import com.hp.jipp.model.Status

/**
 * DSL for defining an IPP packet. By default, the packet's `versionNumber` is set to
 * [DEFAULT_VERSION_NUMBER] and its `requestId` is set to [DEFAULT_REQUEST_ID].
 */
@Suppress("ClassName", "ClassNaming")
object ippPacket {
    /** The default request ID (1001), which can be overridden with `requestId = 123` */
    operator fun invoke(
        operation: Operation,
        requestId: Int = DEFAULT_REQUEST_ID,
        func: IppPacket.Builder.() -> Unit
    ) = with(IppPacket.Builder(operation.code, requestId = requestId)) {
        func()
        build()
    }

    @Suppress("DEPRECATION")
    operator fun invoke(
        status: Status,
        requestId: Int = DEFAULT_REQUEST_ID,
        func: IppPacket.Builder.() -> Unit
    ) = with(IppPacket.Builder(status.code, requestId = requestId)) {
        func()
        build()
    }
}

/** DSL for defining an [AttributeGroup]. */
@Suppress("ClassName", "ClassNaming")
@Deprecated("Use IppPacket builders")
object group {
    operator fun invoke(tag: DelimiterTag, func: MutableAttributeGroup.() -> Unit) =
        mutableGroupOf(tag).apply { func() }.toGroup()
}
