// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/** An attribute type containing integer values. */
open class IntType(override val name: String) : AttributeType<Int> {
    override fun coerce(value: Any) =
        value as? Int

    companion object {
        val codec = Codec(Tag.integerValue, {
            takeLength(AttributeGroup.INT_LENGTH)
            readInt()
        }, {
            writeShort(AttributeGroup.INT_LENGTH)
            writeInt(it)
        })
    }
}
