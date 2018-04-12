// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.model

import com.hp.jipp.encoding.Keyword
import com.hp.jipp.encoding.KeywordType

/** A keyword attribute describing duplexing features. */
class Sides(override val name: String) : Keyword() {

    /** An attribute type for [Sides] attributes */
    class Type(name: String) : KeywordType<Sides>(ENCODER, name)

    companion object {
        @JvmField val oneSided = Sides("one-sided")
        @JvmField val twoSidedLongEdge = Sides("two-sided-long-edge")
        @JvmField val twoSidedShortEdge = Sides("two-sided-short-edge")

        val ENCODER = KeywordType.encoderOf(Sides::class.java) { Sides(it) }
    }
}
