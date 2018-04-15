// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/** A unit of measurement used to describe resolution */
data class ResolutionUnit(override val code: Int, override val name: String) : Enum() {

    override fun toString() = name

    companion object {
        @JvmField val dotsPerInch = ResolutionUnit(3, "dpi")

        @JvmField val dotsPerCentimeter = ResolutionUnit(4, "dpcm")

        /** The encoder for converting integers to Operation objects  */
        @JvmField
        val Encoder = EnumType.Encoder(ResolutionUnit::class.java) { code, name ->
            ResolutionUnit(code, name)
        }
    }
}
