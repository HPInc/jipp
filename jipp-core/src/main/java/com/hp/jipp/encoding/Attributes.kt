// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/** Utilities for the [Attribute] class. */
object Attributes {
    // Note: here, not in a companion object of [Attribute], for Java compatibility

    /** Return a typeless [Tag.unknown] attribute. */
    @JvmStatic
    fun unknown(name: String): Attribute<Nothing> =
        BaseAttribute(name, null, Tag.unknown)

    /** Return a typeless [Tag.unsupported] attribute. */
    @JvmStatic
    fun unsupported(name: String): Attribute<Nothing> =
        BaseAttribute(name, null, Tag.unsupported)

    /** Return a typeless [Tag.noValue] attribute. */
    @JvmStatic
    fun noValue(name: String): Attribute<Nothing> =
        BaseAttribute(name, null, Tag.noValue)
}
