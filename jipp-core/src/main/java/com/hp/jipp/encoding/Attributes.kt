// © Copyright 2017 - 2020 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/** Utilities for the [Attribute] class. */
object Attributes {
    // Notes
    // * here, not in a companion object of [Attribute], for Java compatibility
    // * Attribute<*> return types allow Java to put them in lists with other Attribute<*>

    /** Return a typeless [Tag.unknown] attribute. */
    @JvmStatic
    fun unknown(name: String): Attribute<*> =
        EmptyAttribute<Nothing>(name, Tag.unknown)

    /** Return a typeless [Tag.unsupported] attribute. */
    @JvmStatic
    fun unsupported(name: String): Attribute<*> =
        EmptyAttribute<Nothing>(name, Tag.unsupported)

    /** Return a typeless [Tag.noValue] attribute. */
    @JvmStatic
    fun noValue(name: String): Attribute<*> =
        EmptyAttribute<Nothing>(name, Tag.noValue)
}
