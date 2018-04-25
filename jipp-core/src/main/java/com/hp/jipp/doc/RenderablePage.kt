package com.hp.jipp.doc

import kotlin.math.roundToInt

/** A page whose contents can be rendered to an [IntArray] of pixels */
abstract class RenderablePage {

    /** Width of the page in points */
    abstract val widthPoints: Double

    /** Height of the page in points */
    abstract val heightPoints: Double

    /**
     * Render a full-width swath of the page into an array of bytes at the given DPI. Returns the [ByteArray]
     * as supplied or allocated.
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
        /**
         * A [ByteArray] having the correct size for this swath, which is: [pointsToPixels] ([widthPoints]) *
         * [colorSpace] . [ColorSpace.bytesPerPixel] * [swathHeight]
         */
        byteArray: ByteArray
    )

    companion object {
        private const val POINTS_PER_INCH = 72.0

        /** Utility method used to convert points to pixels at a certain dpi */
        fun pointsToPixels(dpi: Int, points: Double): Int =
            Math.ceil(points * dpi / POINTS_PER_INCH).roundToInt()

        /** Utility method used to convert pixels at a certain dpi to points */
        fun pixelsToPoints(dpi: Int, pixels: Int): Double =
            POINTS_PER_INCH * pixels / dpi
    }
}
