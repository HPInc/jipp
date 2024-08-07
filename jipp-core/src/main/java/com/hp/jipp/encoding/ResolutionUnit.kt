// Copyright 2017 - 2019 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/** A unit of measurement used to describe resolution. */
data class ResolutionUnit(override val code: Int, override val name: String) : Enum() {
    override fun toString() = super.toString()

    /** Raw codes which may be used for direct comparisons. */
    object Code {
        const val dotsPerInch = 3
        const val dotsPerCentimeter = 4
    }

    companion object {
        @JvmField val dotsPerInch = ResolutionUnit(Code.dotsPerInch, "dpi")
        @JvmField val dotsPerCentimeter = ResolutionUnit(Code.dotsPerCentimeter, "dpcm")

        /** All known resolution values */
        @JvmField val all = listOf(dotsPerInch, dotsPerCentimeter).map { it.code to it }.toMap()

        operator fun get(value: Int): ResolutionUnit =
            all[value] ?: ResolutionUnit(value, "???")
    }
}
