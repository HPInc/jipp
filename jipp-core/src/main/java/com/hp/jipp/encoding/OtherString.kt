// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/** An attribute value formatted as a String. */
data class OtherString(override val tag: Tag, override val value: String) : TaggedValue(), Stringable {
    override fun toString() = "\"$value\" ($tag)"
    override fun asString() = value
}
