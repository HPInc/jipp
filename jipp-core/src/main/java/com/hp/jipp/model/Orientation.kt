// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.model

import com.hp.jipp.encoding.Enum
import com.hp.jipp.encoding.EnumType

/** Orientations as defined in [RFC2911 Section 4.2.10](https://tools.ietf.org/html/rfc2911#section-4.2.10). */
data class Orientation(override val code: Int, override val name: String) : Enum() {
    override fun toString() = super.toString()

    /** An attribute type for [Orientation] attributes */
    open class Type(name: String) : EnumType<Orientation>(Orientation.Encoder, name)

    object Code {
        const val portrait = 3
        const val landscape = 4
        const val reverseLandscape = 5
        const val reversePortrait = 6
    }

    companion object {
        @JvmField val portrait = Orientation(Code.portrait, "portrait")
        @JvmField val landscape = Orientation(Code.landscape, "landscape")
        @JvmField val reverseLandscape = Orientation(Code.reverseLandscape, "reverse-landscape")
        @JvmField val reversePortrait = Orientation(Code.reversePortrait, "reverse-portrait")

        @JvmField val Encoder = EnumType.Encoder(Orientation::class.java) { code, name ->
            Orientation(code, name)
        }
    }
}
