// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/**
 * A type for `octetString` types, represented natively as [ByteArray] values.
 */
open class OctetsType(override val name: String) : AttributeType<ByteArray> {
    override fun coerce(value: Any) =
        value as? ByteArray

    companion object {
        val codec = AttributeGroup.codec(Tag.octetString, {
            readValueBytes()
        }, {
            writeValueBytes(it)
        })
    }
}
