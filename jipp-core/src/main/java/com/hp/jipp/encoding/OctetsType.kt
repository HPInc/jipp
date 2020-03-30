// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/**
 * An [AttributeType] for an `octetString` value represented as a [ByteArray].
 */
open class OctetsType(name: String) : AttributeTypeImpl<ByteArray>(name, ByteArray::class.java) {
    /** An [AttributeType] for `octetString` values represented as [ByteArray]s. */
    class Set(name: String) : OctetsType(name), AttributeSetType<ByteArray> {
        override fun toString() = "OctetsType.Set($name)"
    }

    override fun toString() = "OctetsType($name)"

    companion object {
        val codec = Codec(Tag.octetString, {
            readValueBytes()
        }, {
            writeBytesValue(it)
        })
    }
}
