// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.trans.IppStreams

/** An [AttributeType] for a [Int] or [IntRange] value. */
open class IntOrIntRangeType(
    name: String
) : AttributeTypeImpl<IntOrIntRange>(name, IntOrIntRange::class.java) {
    /** An [AttributeType] for multiple [Name] and keyword values. */
    class Set(name: String) : IntOrIntRangeType(name), AttributeSetType<IntOrIntRange> {
        /** Return an [Attribute] containing [IntRange]s as given. */
        fun of(value: IntRange, vararg values: IntRange) =
            of((listOf(value) + values.toList()).map { IntOrIntRange(it) })

        /** Return an [Attribute] containing [Int]s as given. */
        fun of(value: Int, vararg values: Int) =
            of((listOf(value) + values.toList()).map { IntOrIntRange(it) })

        override fun toString() = "IntOrIntRangeType.Set($name)"
    }

    /** Return an [Attribute] containing a single value of type [IntOrIntRange]. */
    fun of(value: Int): Attribute<IntOrIntRange> =
        of(IntOrIntRange(value))

    /** Return an [Attribute] containing a single value of type [IntOrIntRange]. */
    fun of(value: IntRange): Attribute<IntOrIntRange> =
        of(IntOrIntRange(value))

    override fun coerce(value: Any) =
        when (value) {
            is Int -> IntOrIntRange(value)
            is IntRange -> IntOrIntRange(value)
            is IntOrIntRange -> value
            else -> null
        }

    companion object {
        val codec = Codec<IntOrIntRange>({ false }, {
            throw IllegalArgumentException("Cannot read")
        }, {
            if (it.tag == Tag.integerValue) {
                writeIntValue(it.start)
            } else {
                writeShort(IppStreams.INT_LENGTH + IppStreams.INT_LENGTH)
                writeInt(it.start)
                writeInt(it.endInclusive)
            }
        })
    }
}
