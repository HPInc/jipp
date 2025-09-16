// © Copyright 2017 - 2020 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.util.toHexString

/** An attribute value formatted as an array of octets. */
data class OtherOctets(override val tag: ValueTag, override val value: ByteArray) : TaggedValue {
    override fun equals(other: Any?): Boolean {
        if (other !is OtherOctets) return false
        return other.tag == tag && value contentEquals other.value
    }

    override fun hashCode(): Int {
        return (tag.hashCode() * HASH_PRIME + value.contentHashCode())
    }

    override fun toString() = "${value.toHexString()} ($tag)"

    companion object {
        private const val HASH_PRIME = 31
    }
}
