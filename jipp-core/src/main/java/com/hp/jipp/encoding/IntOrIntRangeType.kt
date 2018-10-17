// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/** Attribute type for attributes whose values are either integers or [IntRange] objects */
class IntOrIntRangeType(
    override val name: String
) : AttributeType<IntOrIntRange> {

    override fun coerce(value: Any) =
        when (value) {
            is Int -> IntOrIntRange(value)
            is IntRange -> IntOrIntRange(value)
            is IntOrIntRange -> value
            else -> null
        }

    /** Return an attribute containing the supplied integer value(s) */
    fun ofIntegers(integers: List<Int>) = of(integers.map { IntOrIntRange(it) })

    /** Return an attribute containing the supplied integer value(s) */
    fun of(vararg integers: Int) = ofIntegers(integers.toList())

    /** Return an attribute containing the supplied range(s) */
    fun ofRanges(ranges: List<IntRange>) = of(ranges.map { IntOrIntRange(it) })

    /** Return an attribute containing the supplied range(s) */
    fun of(vararg ranges: IntRange) = ofRanges(ranges.toList())

    companion object {
        val codec = AttributeGroup.codec<IntOrIntRange>({ false }, {
            throw IllegalArgumentException("Cannot read")
        }, {
            if (it.tag == Tag.integerValue) {
                writeShort(AttributeGroup.INT_LENGTH)
                writeInt(it.start)
            } else {
                writeShort(AttributeGroup.INT_LENGTH + AttributeGroup.INT_LENGTH)
                writeInt(it.start)
                writeInt(it.endInclusive)
            }
        })
    }
}
