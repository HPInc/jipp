// Copyright 2017 - 2020 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/** An attribute value formatted as a String. */
data class OtherString(override val tag: ValueTag, override val value: String) : TaggedValue, Stringable {
    override fun toString() = "\"$value\" ($tag)"
    override fun asString() = value
}
