package com.hp.jipp.model

import com.hp.jipp.encoding.Enum
import com.hp.jipp.encoding.EnumType

/** Finishings as defined in [RFC8011 Section 5.2.6](https://tools.ietf.org/html/rfc8011#section-5.2.6). */
data class Finishings(override val name: String, override val code: Int) : Enum() {

    override fun toString() = name

    class Type(name: String) : EnumType<Finishings>(ENCODER, name)

    companion object {
        @JvmField val None = Finishings("none", 3)
        @JvmField val Staple = Finishings("staple", 4)
        @JvmField val Punch = Finishings("punch", 5)
        @JvmField val Cover = Finishings("cover", 6)
        @JvmField val Bind = Finishings("bind", 7)
        @JvmField val SaddleStitch = Finishings("saddle-stitch", 8)
        @JvmField val EdgeStitch = Finishings("edge-stitch", 9)
        @JvmField val StapleTopLeft = Finishings("staple-top-left", 20)
        @JvmField val StapleBottomLeft = Finishings("staple-bottom-left", 21)
        @JvmField val StapleTopRight = Finishings("staple-top-right", 22)
        @JvmField val StapleBottomRight = Finishings("staple-bottom-right", 23)
        @JvmField val EdgeStitchLeft = Finishings("edge-stitch-left", 24)
        @JvmField val EdgeStitchTop = Finishings("edge-stitch-top", 25)
        @JvmField val EdgeStitchRight = Finishings("edge-stitch-right", 26)
        @JvmField val EdgeStitchBottom = Finishings("edge-stitch-bottom", 27)
        @JvmField val StapleDualLeft = Finishings("staple-dual-left", 28)
        @JvmField val StapleDualTop = Finishings("staple-dual-top", 29)
        @JvmField val StapleDualRight = Finishings("staple-dual-right", 30)
        @JvmField val StapleDualBottom = Finishings("staple-dual-bottom", 31)

        @JvmField val ENCODER = EnumType.Encoder(Finishings::class.java, { name, code -> Finishings(name, code) })
    }
}
