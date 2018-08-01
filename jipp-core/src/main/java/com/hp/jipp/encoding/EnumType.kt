// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.encoding.AttributeGroup.Companion.codec

/** An attribute type based on [Enum] type of [T]*/
open class EnumType<T : Enum>(
    override val name: String,
    val factory: (code: Int) -> T
) : AttributeType<T> {
    fun of(vararg values: Int) = of(values.map { factory(it) })

    override fun toString() = "EnumType($name)"

    override fun coerce(value: Any) =
        when (value) {
            is UntypedEnum -> factory(value.code)
            is Enum -> factory(value.code)
            is Int -> factory(value)
            else -> null
        }

    companion object {
        val codec = codec<Enum>(Tag.enumValue, {
            takeLength(AttributeGroup.INT_LENGTH)
            UntypedEnum(readInt())
        }, {
            writeShort(AttributeGroup.INT_LENGTH)
            writeInt(it.code)
        })
    }
}
