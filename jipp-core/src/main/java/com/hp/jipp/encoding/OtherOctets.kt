// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.util.toHexString

/** An attribute value formatted as an array of octets. */
data class OtherOctets(override val tag: Tag, override val value: ByteArray) : TaggedValue() {
    override fun equals(other: Any?): Boolean {
        TODO("Implement")
    }

    override fun hashCode(): Int {
        TODO("Implement")
    }

    override fun toString() = "${value.toHexString()} ($tag)"
}
