// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/** An object that has a native string representation. */
interface Stringable {
    /**
     * Return the most basic string representation of a value without any decorators or extra info
     * which might be present.
     */
    fun asString(): String
}

/** Convert a list of items that might be [Stringable] into strings and leave everything else alone. */
internal fun Collection<*>.stringinate(): List<*> =
    map { if (it is Stringable) it.asString() else it }
