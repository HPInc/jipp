// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/** An int or range of int */
data class IntOrIntRange constructor(
    val start: Int,
    val endInclusive: Int,
    /** If true, this data was originally intended as a single integer, and [start] == [endInclusive] */
    val simpleInt: Boolean
) : TaggedValue() {

    override val tag: Tag
        get() = if (simpleInt) Tag.integerValue else Tag.rangeOfInteger
    override val value: Any
        get() = if (simpleInt) start else range

    /** Constructs an object based on a range */
    constructor(range: IntRange) : this (range.start, range.endInclusive, false)

    /** Constructs an object based on a range */
    constructor(start: Int, endInclusive: Int) : this (start, endInclusive, false)

    /** Constructs an object based on a single integer */
    constructor(value: Int) : this (value, value, true)

    /** The integer range or integer in the form of an [IntRange] */
    val range by lazy {
        IntRange(start, endInclusive)
    }
}
