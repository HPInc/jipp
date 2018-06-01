// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.model

import com.hp.jipp.encoding.Enum
import com.hp.jipp.encoding.EnumType

/** Finishings as defined in [RFC8011 Section 5.2.6](https://tools.ietf.org/html/rfc8011#section-5.2.6). */
data class Finishings(override val code: Int, override val name: String) : Enum() {

    override fun toString() = super.toString()

    /** An attribute type for [Finishings] attributes */
    class Type(name: String) : EnumType<Finishings>(Encoder, name)

    object Code {
        const val none = 3
        const val staple = 4
        const val punch = 5
        const val cover = 6
        const val bind = 7
        const val saddleStitch = 8
        const val edgeStitch = 9
        const val stapleTopLeft = 20
        const val stapleBottomLeft = 21
        const val stapleTopRight = 22
        const val stapleBottomRight = 23
        const val edgeStitchLeft = 24
        const val edgeStitchTop = 25
        const val edgeStitchRight = 26
        const val edgeStitchBottom = 27
        const val stapleDualLeft = 28
        const val stapleDualTop = 29
        const val stapleDualRight = 30
        const val stapleDualBottom = 31
    }

    companion object {
        @JvmField val none = Finishings(Code.none, "none")
        @JvmField val staple = Finishings(Code.staple, "staple")
        @JvmField val punch = Finishings(Code.punch, "punch")
        @JvmField val cover = Finishings(Code.cover, "cover")
        @JvmField val bind = Finishings(Code.bind, "bind")
        @JvmField val saddleStitch = Finishings(Code.saddleStitch, "saddle-stitch")
        @JvmField val edgeStitch = Finishings(Code.edgeStitch, "edge-stitch")
        @JvmField val stapleTopLeft = Finishings(Code.stapleTopLeft, "staple-top-left")
        @JvmField val stapleBottomLeft = Finishings(Code.stapleBottomLeft, "staple-bottom-left")
        @JvmField val stapleTopRight = Finishings(Code.stapleTopRight, "staple-top-right")
        @JvmField val stapleBottomRight = Finishings(Code.stapleBottomRight, "staple-bottom-right")
        @JvmField val edgeStitchLeft = Finishings(Code.edgeStitchLeft, "edge-stitch-left")
        @JvmField val edgeStitchTop = Finishings(Code.edgeStitchTop, "edge-stitch-top")
        @JvmField val edgeStitchRight = Finishings(Code.edgeStitchRight, "edge-stitch-right")
        @JvmField val edgeStitchBottom = Finishings(Code.edgeStitchBottom, "edge-stitch-bottom")
        @JvmField val stapleDualLeft = Finishings(Code.stapleDualLeft, "staple-dual-left")
        @JvmField val stapleDualTop = Finishings(Code.stapleDualTop, "staple-dual-top")
        @JvmField val stapleDualRight = Finishings(Code.stapleDualRight, "staple-dual-right")
        @JvmField val stapleDualBottom = Finishings(Code.stapleDualBottom, "staple-dual-bottom")

        @JvmField val Encoder = EnumType.Encoder(Finishings::class.java) { code, name ->
            Finishings(code, name)
        }
    }
}
