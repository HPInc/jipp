// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.pdl

/** A page whose contents can be rendered to an [IntArray] of pixels. */
abstract class RenderablePage(
    /** Width of the entire page, in pixels. */
    val widthPixels: Int,
    /** Height of the entire page, in pixels. */
    val heightPixels: Int
) {

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
     * is calculated according to the following formula:
     *
     * [widthPixels] * [colorSpace] . [ColorSpace.bytesPerPixel] * [swathHeight]
     */
    fun renderSize(swathHeight: Int, colorSpace: ColorSpace): Int =
        widthPixels * colorSpace.bytesPerPixel * swathHeight

    /** Return a version of this page, but rotated 180 degrees. */
    fun rotated() = let { parent ->
        object : RenderablePage(widthPixels, heightPixels) {
            override fun render(yOffset: Int, swathHeight: Int, colorSpace: ColorSpace, byteArray: ByteArray) {
                parent.render(heightPixels - yOffset - swathHeight, swathHeight, colorSpace, byteArray)
                byteArray.rotate180(widthPixels, colorSpace.bytesPerPixel)
            }
        }
    }

    /** Return a version of this page, but flipped top-to-bottom. */
    fun flipY() = let { parent ->
        object : RenderablePage(widthPixels, heightPixels) {
            override fun render(yOffset: Int, swathHeight: Int, colorSpace: ColorSpace, byteArray: ByteArray) {
                parent.render(heightPixels - yOffset - swathHeight, swathHeight, colorSpace, byteArray)
                byteArray.flipY(widthPixels, colorSpace.bytesPerPixel)
            }
        }
    }

    /** Return a version of this page, but flipped left-to-right. */
    fun flipX() = let { parent ->
        object : RenderablePage(widthPixels, heightPixels) {
            override fun render(yOffset: Int, swathHeight: Int, colorSpace: ColorSpace, byteArray: ByteArray) {
                parent.render(yOffset, swathHeight, colorSpace, byteArray)
                byteArray.flipX(widthPixels, colorSpace.bytesPerPixel)
            }
        }
    }

    /** Return a blank version of this page (same width/height but all pixels white). */
    fun blank() =
        object : RenderablePage(heightPixels, widthPixels) {
            override fun render(yOffset: Int, swathHeight: Int, colorSpace: ColorSpace, byteArray: ByteArray) {
                byteArray.fill(WHITE_BYTE)
            }
        }

    companion object {
        const val WHITE_BYTE = 0xFF.toByte()

        /** Rotate the pixels in this byte array 180 degrees. */
        private fun ByteArray.rotate180(width: Int, bytesPerPixel: Int) {
            val height = size / (width * bytesPerPixel)
            for (y in 0 until (height / 2)) {
                for (x in 0 until width) {
                    swap((y * width + x) * bytesPerPixel,
                        (((height - y) * width) - x - 1) * bytesPerPixel,
                        bytesPerPixel)
                }
            }
            // If height is odd then we have to also flip the middle line end-to-end
            val middle = height / 2
            if (middle % 2 != 0) {
                for (x in 0 until width / 2) {
                    swap((middle * width + x) * bytesPerPixel,
                        ((middle * width + width - x - 1) * bytesPerPixel),
                        bytesPerPixel)
                }
            }
        }

        /** Rotate the pixels in this even-height byte array 180 degrees. */
        private fun ByteArray.flipX(width: Int, bytesPerPixel: Int) {
            val height = size / (width * bytesPerPixel)
            for (y in 0 until height) {
                for (x in 0 until width / 2) {
                    swap((y * width + x) * bytesPerPixel,
                        ((y * width + width - x - 1) * bytesPerPixel),
                        bytesPerPixel)
                }
            }
        }

        /** Rotate the pixels in this even-height byte array 180 degrees. */
        private fun ByteArray.flipY(width: Int, bytesPerPixel: Int) {
            val height = size / (width * bytesPerPixel)
            for (y in 0 until height / 2) {
                swap(y * width * bytesPerPixel,
                    (height - y - 1) * width * bytesPerPixel,
                    width * bytesPerPixel)
            }
        }

        /** Swap two chunks in a byte array based on their absolute locations. */
        private fun ByteArray.swap(pos1: Int, pos2: Int, size: Int) {
            if (size == 1) {
                val swapped = get(pos1)
                set(pos1, get(pos2))
                set(pos2, swapped)
            } else {
                val array = copyOfRange(pos1, pos1 + size)
                System.arraycopy(this, pos2, this, pos1, size)
                System.arraycopy(array, 0, this, pos2, size)
            }
        }
    }
}
