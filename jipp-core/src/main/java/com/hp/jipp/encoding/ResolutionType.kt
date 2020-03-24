// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/** An attribute type containing [Resolution] values. */
open class ResolutionType(override val name: String) : AttributeType<Resolution> {
    override fun coerce(value: Any) =
        value as? Resolution

    override fun toString() = "ResolutionType($name)"

    companion object {
        val codec = Codec(Tag.resolution, {
            takeLength(IppStreams.INT_LENGTH + IppStreams.INT_LENGTH + IppStreams.BYTE_LENGTH)
            Resolution(readInt(), readInt(), ResolutionUnit[readByte().toInt()])
        }, {
            writeShort(IppStreams.INT_LENGTH + IppStreams.INT_LENGTH + IppStreams.BYTE_LENGTH)
            writeInt(it.crossFeedResolution)
            writeInt(it.feedResolution)
            writeByte(it.unit.code.toByte().toInt())
        })
    }
}
