// © Copyright 2020 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/**
 * A named attribute type which may be used to construct [Attribute] objects containing 0 or more values of type [T].
 *
 * Note: "Set" refers to the IPP "1setOf" syntax, which is an ordered series of values which may contain duplicates.
 */
interface AttributeSetType<T : Any> : AttributeType<T> {
    /** Return an [Attribute] containing [values]. */
    fun of(values: Iterable<T>): Attribute<T> =
        AttributeImpl(name, this, values.toList())

    /** Return an [Attribute] containing multiple values of [T]. */
    fun of(value: T, vararg values: T): Attribute<T> =
        of(listOf(value) + values.toList())
}
