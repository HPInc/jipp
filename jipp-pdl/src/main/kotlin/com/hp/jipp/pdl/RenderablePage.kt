package com.hp.jipp.pdl

/** A page whose contents can be rendered to an [IntArray] of pixels. */
abstract class RenderablePage {

    /** Width of the page in pixels */
    abstract val widthPixels: Int

    /** Height of the page in pixels */
    abstract val heightPixels: Int

    /**
     * Render a full-width swath of the page into an array of bytes at the given DPI.
     */
    abstract fun render(
        /** yOffset from top of page, in pixels. */
        yOffset: Int,
        /** height of swath to be rendered, in pixels. */
        swathHeight: Int,
        /** Color Space defining how each pixel is to be encoded. */
        colorSpace: ColorSpace,
        /** A [ByteArray] having the correct size available for this swath (see [renderSize]). */
        byteArray: ByteArray
    )

    /**
     * Return the number of bytes which must be present in the byteArray supplied to [render]. The value
     * is generally calculated according to the following formula:
     *
     * [widthPixels] * [colorSpace] . [ColorSpace.bytesPerPixel] * [swathHeight]
     *
     */
    fun renderSize(swathHeight: Int, colorSpace: ColorSpace): Int =
        widthPixels * colorSpace.bytesPerPixel * swathHeight
}
