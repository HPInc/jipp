// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.model

import com.hp.jipp.encoding.Enum
import com.hp.jipp.encoding.EnumType

/** Finishings as defined in [RFC8011 Section 5.2.6](https://tools.ietf.org/html/rfc8011#section-5.2.6). */
data class Finishings(override val code: Int, override val name: String) : Enum() {

    override fun toString() = name

    /** An attribute type for [Finishings] attributes */
    class Type(name: String) : EnumType<Finishings>(Encoder, name)

    companion object {
        @JvmField val none = Finishings(3, "none")
        @JvmField val staple = Finishings(4, "staple")
        @JvmField val punch = Finishings(5, "punch")
        @JvmField val cover = Finishings(6, "cover")
        @JvmField val bind = Finishings(7, "bind")
        @JvmField val saddleStitch = Finishings(8, "saddle-stitch")
        @JvmField val edgeStitch = Finishings(9, "edge-stitch")
        @JvmField val stapleTopLeft = Finishings(20, "staple-top-left")
        @JvmField val stapleBottomLeft = Finishings(21, "staple-bottom-left")
        @JvmField val stapleTopRight = Finishings(22, "staple-top-right")
        @JvmField val stapleBottomRight = Finishings(23, "staple-bottom-right")
        @JvmField val edgeStitchLeft = Finishings(24, "edge-stitch-left")
        @JvmField val edgeStitchTop = Finishings(25, "edge-stitch-top")
        @JvmField val edgeStitchRight = Finishings(26, "edge-stitch-right")
        @JvmField val edgeStitchBottom = Finishings(27, "edge-stitch-bottom")
        @JvmField val stapleDualLeft = Finishings(28, "staple-dual-left")
        @JvmField val stapleDualTop = Finishings(29, "staple-dual-top")
        @JvmField val stapleDualRight = Finishings(30, "staple-dual-right")
        @JvmField val stapleDualBottom = Finishings(31, "staple-dual-bottom")

        @JvmField val Encoder = EnumType.Encoder(Finishings::class.java) { code, name ->
            Finishings(code, name)
        }
    }
}
