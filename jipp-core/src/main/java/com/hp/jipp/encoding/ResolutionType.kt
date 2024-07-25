// Copyright 2017 - 2021 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/** An [AttributeType] for a [Resolution] value. */
open class ResolutionType(override val name: String) : AttributeType<Resolution> {
    /** An [AttributeType] for multiple [Resolution] values. */
    class Set(name: String) : ResolutionType(name), AttributeSetType<Resolution> {
        override fun toString() = "ResolutionType.Set($name)"
    }

    override fun coerce(value: Any) =
        value as? Resolution

    override fun toString() = "ResolutionType($name)"

    companion object {
        val codec = Codec(
            Tag.resolution,
            {
                takeLength(IppStreams.INT_LENGTH + IppStreams.INT_LENGTH + IppStreams.BYTE_LENGTH)
                Resolution(readInt(), readInt(), ResolutionUnit[readByte().toInt()])
            },
            {
                writeShort(IppStreams.INT_LENGTH + IppStreams.INT_LENGTH + IppStreams.BYTE_LENGTH)
                writeInt(it.crossFeedResolution)
                writeInt(it.feedResolution)
                writeByte(it.unit.code.toByte().toInt())
            }
        )
    }
}
