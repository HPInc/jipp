// Copyright 2017 - 2021 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/** An [AttributeType] for an [Enum] value. */
open class EnumType<T : Enum>(
    override val name: String,
    val factory: (code: Int) -> T
) : AttributeType<T> {
    /** An [AttributeType] for multiple [Enum] values. */
    open class Set<T : Enum>(
        name: String,
        factory: (code: Int) -> T
    ) : EnumType<T>(name, factory), AttributeSetType<T> {
        /** Return an [Attribute] of this type with multiple values. */
        fun of(value: Int, vararg values: Int) =
            of((listOf(value) + values.toList()).map { factory(it) })

        override fun toString() = "EnumType.Set($name)"
    }

    fun of(value: Int) = of(factory(value))

    override fun coerce(value: Any) =
        when (value) {
            is UntypedEnum -> factory(value.code)
            is Enum -> factory(value.code)
            is Int -> factory(value)
            else -> null
        }

    override fun toString() = "EnumType($name)"

    companion object {
        val codec = Codec<Enum>(
            Tag.enumValue,
            {
                UntypedEnum(readIntValue())
            },
            {
                writeIntValue(it.code)
            }
        )
    }
}
