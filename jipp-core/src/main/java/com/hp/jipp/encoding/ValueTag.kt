// © Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/**
 * An IPP Value tag.
 */
class ValueTag(override val code: Int, override val name: String) : Tag() {
    override fun toString() = name

    override fun hashCode() = code.hashCode()

    override fun equals(other: Any?) =
        if (other is Tag) {
            other.code == code
        } else super.equals(other)
}
