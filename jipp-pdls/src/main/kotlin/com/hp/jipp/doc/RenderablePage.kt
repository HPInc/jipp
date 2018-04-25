package com.hp.jipp.doc

import kotlin.math.roundToInt

/** A page whose contents can be rendered to an [IntArray] of pixels */
abstract class RenderablePage {

    /** Width of the page in points */
    abstract val widthPoints: Double

    /** Height of the page in points */
    abstract val heightPoints: Double

    /**
     * Render a full-width swath of the page into an array of bytes at the given DPI.
     */
    abstract fun render(
        /** Dots per inch during render */
        dpi: Int,
        /** yOffset from top of page, in pixels */
        yOffset: Int,
        /** height of swath to be rendered, in pixels */
        swathHeight: Int,
        /** Color Space defining how each pixel is to be encoded */
        colorSpace: ColorSpace,
        /** A [ByteArray] having the correct size for this swath (see [renderSize]) */
        byteArray: ByteArray
    )

    /**
     * Return the number of bytes which must be present in the byteArray supplied to [render]. The value
     * is calulated according to the following formula:
     *
     * [pointsToPixels] ([dpi], [widthPoints]) * [colorSpace] . [ColorSpace.bytesPerPixel] * [swathHeight]
     *
     */
    fun renderSize(dpi: Int, swathHeight: Int, colorSpace: ColorSpace): Int =
        pointsToPixels(dpi, widthPoints) * colorSpace.bytesPerPixel * swathHeight

    companion object {
        /** Points per inch (72) */
        @JvmField
        val POINTS_PER_INCH = 72.0

        /** Utility method used to convert points to pixels at a certain dpi */
        @JvmStatic
        fun pointsToPixels(dpi: Int, points: Double): Int =
            Math.ceil(points * dpi / POINTS_PER_INCH).roundToInt()

        /** Utility method used to convert pixels at a certain dpi to points */
        @JvmStatic
        fun pixelsToPoints(dpi: Int, pixels: Int): Double =
            POINTS_PER_INCH * pixels / dpi
    }
}
