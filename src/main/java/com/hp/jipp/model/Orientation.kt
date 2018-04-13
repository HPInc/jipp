// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.model

import com.hp.jipp.encoding.Enum
import com.hp.jipp.encoding.EnumType

/** Orientations as defined in [RFC2911 Section 4.2.10](https://tools.ietf.org/html/rfc2911#section-4.2.10). */
data class Orientation(override val name: String, override val code: Int) : Enum() {
    override fun toString() = name

    /** An attribute type for [Orientation] attributes */
    open class Type(name: String) : EnumType<Orientation>(Orientation.Encoder, name)

    companion object {
        @JvmField val portrait = Orientation("portrait", 3)
        @JvmField val landscape = Orientation("landscape", 4)
        @JvmField val reverseLandscape = Orientation("reverse-landscape", 5)
        @JvmField val reversePortrait = Orientation("reverse-portrait", 6)

        @JvmField val Encoder = EnumType.Encoder(Orientation::class.java) { code, name ->
            Orientation(name, code)
        }
    }
}
