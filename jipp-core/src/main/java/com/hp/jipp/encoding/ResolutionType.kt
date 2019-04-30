// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/** An attribute type containing [Resolution] values. */
open class ResolutionType(override val name: String) : AttributeType<Resolution> {
    override fun coerce(value: Any) =
        value as? Resolution

    override fun toString() = "ResolutionType($name)"

    companion object {
        val codec = AttributeGroup.codec(Tag.resolution, {
            takeLength(AttributeGroup.INT_LENGTH + AttributeGroup.INT_LENGTH + AttributeGroup.BYTE_LENGTH)
            Resolution(readInt(), readInt(), ResolutionUnit[readByte().toInt()])
        }, {
            writeShort(AttributeGroup.INT_LENGTH + AttributeGroup.INT_LENGTH + AttributeGroup.BYTE_LENGTH)
            writeInt(it.crossFeedResolution)
            writeInt(it.feedResolution)
            writeByte(it.unit.code.toByte().toInt())
        })
    }
}
