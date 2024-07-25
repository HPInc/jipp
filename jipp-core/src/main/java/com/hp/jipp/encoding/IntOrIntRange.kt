// Copyright 2017 - 2020 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/** An int or range of int */
class IntOrIntRange private constructor(
    val start: Int,
    val endInclusive: Int,
    /** If true, this data was originally intended as a single integer, and [start] == [endInclusive] */
    val simpleInt: Boolean
) : TaggedValue {

    /** Constructs an object based on a range */
    constructor(range: IntRange) : this (range.first, range.last, false)

    /** Constructs an object based on a range */
    constructor(start: Int, endInclusive: Int) : this (start, endInclusive, false)

    /** Constructs an object based on a single integer */
    constructor(value: Int) : this (value, value, true)

    override val tag: ValueTag
        get() = if (simpleInt) Tag.integerValue else Tag.rangeOfInteger

    /** An [Int] or [IntRange] value. */
    override val value: Any
        get() = if (simpleInt) start else range

    override fun equals(other: Any?) =
        if (other === this) true else when (other) {
            is IntOrIntRange -> value == other.value
            else -> false
        }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return value.toString()
    }

    /** The integer range or integer in the form of an [IntRange] */
    val range by lazy {
        IntRange(start, endInclusive)
    }
}
