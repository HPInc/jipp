// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/** An [AttributeType] for an [Int] value. */
open class IntType(name: String) : AttributeTypeImpl<Int>(name, Int::class.java) {
    /** An [AttributeType] for multiple [Int] values. */
    class Set(name: String) : IntType(name), AttributeSetType<Int> {
        override fun toString() = "IntType.Set($name)"
    }

    override fun coerce(value: Any) =
        value as? Int

    companion object {
        val codec = Codec(Tag.integerValue, {
            readIntValue()
        }, {
            writeIntValue(it)
        })
    }
}
