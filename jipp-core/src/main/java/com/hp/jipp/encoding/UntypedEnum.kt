// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/** An enum lacking any formal Enum type (meaning its values cannot be decoded). */
data class UntypedEnum(override val code: Int) : Enum() {
    override val name = "???"
    override fun toString() = "enum($code)"
}
