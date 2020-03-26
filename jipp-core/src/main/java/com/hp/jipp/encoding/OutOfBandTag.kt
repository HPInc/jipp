// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.util.BuildError

/**
 * An IPP Out-of-Band Tag.
 */
class OutOfBandTag(override val code: Int, override val name: String) : Tag() {
    init {
        if (!isOutOfBand) {
            throw BuildError("Group tag $this code must be in the delimiter range")
        }
    }

    override fun hashCode() = code.hashCode()

    override fun equals(other: Any?) =
        if (other is Tag) {
            other.code == code
        } else super.equals(other)

    override fun toString() = name
}
