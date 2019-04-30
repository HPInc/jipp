// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

import com.hp.jipp.pdl.ColorSpace
import com.hp.jipp.pdl.RenderablePage
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.min

class PageTest {
    @Test fun generate() {
        val page = fakePage(RED, ColorSpace.Rgb)
        describe(page, ColorSpace.Rgb).also { output ->
            println("Page 1, no transform:\n$output")
            assertEquals("...R...........", output.split("\n")[3])
        }
    }

    @Test fun rotate() {
        val page = fakePage(byteArrayOf(BLACK_BYTE), ColorSpace.Grayscale)
            .rotated()
        describe(page, ColorSpace.Grayscale).also { output ->
            println("Page 2, rotated 180:\n$output")
            assertEquals(".....K.........", output.split("\n")[9]) // Center line
            assertEquals("............K..", output.split("\n")[16])
        }
    }

    @Test fun flipX() {
        val page = fakePage(byteArrayOf(BLACK_BYTE), ColorSpace.Grayscale)
            .flipX()
        describe(page, ColorSpace.Grayscale).also { output ->
            println("Page 3, flipped on X axis:\n$output")
            assertEquals("...........K...", output.split("\n")[3])
        }
    }

    @Test
    fun flipY() {
        val page = fakePage(BLUE, ColorSpace.Rgb)
            .flipY()
        describe(page, ColorSpace.Rgb).also { output ->
            println("Page 4, flipped on Y axis:\n$output")
            assertEquals("...........B...", output.split("\n")[7])
        }
    }

    @Test
    fun flipYEven() {
        val page = fakePage(BLUE, ColorSpace.Rgb, height = 18)
            .flipY()
        describe(page, ColorSpace.Rgb).also { output ->
            println("Page 4, flipped on Y axis:\n$output")
            assertEquals("..........B....", output.split("\n")[7])
        }
    }

    @Test
    fun blank() {
        val page = fakePage(BLUE, ColorSpace.Rgb)
            .blank()
        describe(page, ColorSpace.Rgb).also { output ->
            println("Blank page::\n$output")
            assertEquals("...............", output.split("\n")[7])
        }
    }

    companion object {
        const val WHITE_BYTE = 0xFF.toByte()
        const val BLACK_BYTE = 0x00.toByte()
        val RED = byteArrayOf(WHITE_BYTE, BLACK_BYTE, BLACK_BYTE)
        val GREEN = byteArrayOf(BLACK_BYTE, WHITE_BYTE, BLACK_BYTE)
        val BLUE = byteArrayOf(BLACK_BYTE, BLACK_BYTE, WHITE_BYTE)

        private val colorToText = mapOf(
            listOf(true, true, true) to ".",
            listOf(true, true, false) to 'Y',
            listOf(true, false, true) to 'P',
            listOf(true, false, false) to 'R',
            listOf(false, true, true) to 'C',
            listOf(false, true, false) to 'G',
            listOf(false, false, true) to 'B',
            listOf(false, false, false) to 'K'
            )
        private const val MAX_STRING_DIMENSION = 80

        fun describe(
            page: RenderablePage,
            colorSpace: ColorSpace = ColorSpace.Rgb
        ): String {
            val swathHeight: Int = min(MAX_STRING_DIMENSION, page.heightPixels)
            val bytes = ByteArray(page.renderSize(swathHeight, colorSpace))
            page.render(0, swathHeight, colorSpace, bytes)
            val buffer = StringBuffer()
            for (y in 0 until min(MAX_STRING_DIMENSION, page.heightPixels)) {
                for (x in 0 until min(MAX_STRING_DIMENSION, page.widthPixels)) {
                    val offset = (y * page.widthPixels + x) * colorSpace.bytesPerPixel
                    val pixel = bytes.sliceArray(offset until (offset + colorSpace.bytesPerPixel))
                    buffer.append(
                        when (colorSpace) {
                            ColorSpace.Grayscale -> if (pixel[0] == WHITE_BYTE) "." else "K"
                            ColorSpace.Rgb ->
                                colorToText[listOf(pixel[0] == WHITE_BYTE, pixel[1] == WHITE_BYTE,
                                    pixel[2] == WHITE_BYTE)]
                        }
                    )
                }
                buffer.append("\n")
            }
            return buffer.toString()
        }

        /**
         * Return a page that looks like  "\", with num in each color for each pixel drawn in a 45 degree line, and
         * all other pixels perfectly white (0xFF).
         */
        fun fakePage(pixel: ByteArray, pixelColorSpace: ColorSpace, width: Int = 15, height: Int = 19): RenderablePage {
            return object : RenderablePage(width, height) {
                override fun render(yOffset: Int, swathHeight: Int, colorSpace: ColorSpace, byteArray: ByteArray) {
                    val outputPixel = ByteArray(colorSpace.bytesPerPixel)
                    pixelColorSpace.converter(colorSpace).invoke(pixel, outputPixel)
                    byteArray.fill(WHITE_BYTE)
                    for (i in 0 until swathHeight) {
                        val x = yOffset + i
                        if (x < widthPixels) {
                            val pos = (x + (yOffset + x * widthPixels)) * colorSpace.bytesPerPixel
                            outputPixel.copyInto(byteArray, pos)
                        }
                    }
                }
            }
        }
    }
}
