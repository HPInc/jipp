// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/** A unit of measurement used to describe resolution */
data class ResolutionUnit(override val code: Int, override val name: String) : Enum() {
    override fun toString() = super.toString()

    /** Raw codes which may be used for direct comparisons */
    object Code {
        const val dotsPerInch = 3
        const val dotsPerCentimeter = 4
    }

    companion object {
        @JvmField val dotsPerInch = ResolutionUnit(Code.dotsPerInch, "dpi")
        @JvmField val dotsPerCentimeter = ResolutionUnit(Code.dotsPerCentimeter, "dpcm")

        /** The encoder for converting integers to this Enum object */
        @JvmField
        val Encoder = EnumType.Encoder(ResolutionUnit::class.java) { code, name ->
            ResolutionUnit(code, name)
        }
    }
}
