package com.hp.jipp.model

import com.hp.jipp.encoding.Enum
import com.hp.jipp.encoding.EnumType
import com.hp.jipp.encoding.encoderOf

/** Finishings as defined in [RFC8011 Section 5.2.6](https://tools.ietf.org/html/rfc8011#section-5.2.6). */
data class Finishings(override val name: String, override val code: Int) : Enum() {

    override fun toString() = name

    /** An attribute type for [Finishings] attributes */
    class Type(name: String) : EnumType<Finishings>(ENCODER, name)

    companion object {
        @JvmField val none = Finishings("none", 3)
        @JvmField val staple = Finishings("staple", 4)
        @JvmField val punch = Finishings("punch", 5)
        @JvmField val cover = Finishings("cover", 6)
        @JvmField val bind = Finishings("bind", 7)
        @JvmField val saddleStitch = Finishings("saddle-stitch", 8)
        @JvmField val edgeStitch = Finishings("edge-stitch", 9)
        @JvmField val stapleTopLeft = Finishings("staple-top-left", 20)
        @JvmField val stapleBottomLeft = Finishings("staple-bottom-left", 21)
        @JvmField val stapleTopRight = Finishings("staple-top-right", 22)
        @JvmField val stapleBottomRight = Finishings("staple-bottom-right", 23)
        @JvmField val edgeStitchLeft = Finishings("edge-stitch-left", 24)
        @JvmField val edgeStitchTop = Finishings("edge-stitch-top", 25)
        @JvmField val edgeStitchRight = Finishings("edge-stitch-right", 26)
        @JvmField val edgeStitchBottom = Finishings("edge-stitch-bottom", 27)
        @JvmField val stapleDualLeft = Finishings("staple-dual-left", 28)
        @JvmField val stapleDualTop = Finishings("staple-dual-top", 29)
        @JvmField val stapleDualRight = Finishings("staple-dual-right", 30)
        @JvmField val stapleDualBottom = Finishings("staple-dual-bottom", 31)

        @JvmField val ENCODER = encoderOf(Finishings::class.java, { name, code -> Finishings(name, code) })
    }
}
