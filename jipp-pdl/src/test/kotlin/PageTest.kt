// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

import com.hp.jipp.pdl.ColorSpace
import com.hp.jipp.pdl.RenderableDocument
import com.hp.jipp.pdl.RenderablePage
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.min

class PageTest {
    val doc = object : RenderableDocument {
        val pages = listOf(fakePage(1), fakePage(2), fakePage(3))
        override fun iterator() = pages.iterator()
        override val dpi = 1
    }

    /**
     * Return a page that looks like  "\", with num in each color for each pixel drawn in a 45 degree line, and
     * all other pixels perfectly white (0xFF).
     */
    fun fakePage(num: Int): RenderablePage {
        val fillByte = num.toString().first().toByte()
        return object : RenderablePage(15, 19) {
            override fun render(yOffset: Int, swathHeight: Int, colorSpace: ColorSpace, byteArray: ByteArray) {
                byteArray.fill(WHITE)
                for (i in 0 until swathHeight) {
                    val x = yOffset + i
                    if (x < widthPixels) {
                        val pos = (x + (yOffset + x * widthPixels)) * colorSpace.bytesPerPixel
                        byteArray.fill(fillByte, pos, pos + colorSpace.bytesPerPixel)
                    }
                }
            }
        }
    }

    @Test
    fun generate() {
        fakePage(1).toDisplay(ColorSpace.Rgb).also { output ->
            println("Page 1, no transform:\n$output")
            assertEquals("...1...........", output.split("\n")[3])
        }
    }

    @Test
    fun rotate() {
        fakePage(2).rotated().toDisplay(ColorSpace.Grayscale).also { output ->
            println("Page 2, rotated 180:\n$output")
            assertEquals(".....2.........", output.split("\n")[9]) // Center line
            assertEquals("............2..", output.split("\n")[16])
        }
    }

    @Test
    fun flipX() {
        fakePage(3).flipX().toDisplay(ColorSpace.Grayscale).also { output ->
            println("Page 3, flipped on X axis:\n$output")
            assertEquals("...........3...", output.split("\n")[3])
        }
    }

    @Test
    fun flipY() {
        fakePage(4).flipY().toDisplay(ColorSpace.Rgb).also { output ->
            println("Page 4, flipped on Y axis:\n$output")
            assertEquals("...........4...", output.split("\n")[7])
        }
    }

    companion object {
        const val WHITE = 0xFF.toByte()

        fun RenderablePage.toDisplay(colorSpace: ColorSpace): String {
            val maxShown = 80
            val swathHeight = min(maxShown, heightPixels)
            val bytes = ByteArray(renderSize(swathHeight, colorSpace))
            render(0, swathHeight, colorSpace, bytes)
            val buffer = StringBuffer()
            for (y in 0 until min(maxShown, heightPixels)) {
                for (x in 0 until min(maxShown, widthPixels)) {
                    val offset = (y * widthPixels + x) * colorSpace.bytesPerPixel
                    val pixel = bytes.sliceArray(offset until (offset + colorSpace.bytesPerPixel))
                    buffer.append(if (pixel.any { it != WHITE }) pixel.first().toChar() else '.')
                }
                buffer.append("\n")
            }
            return buffer.toString()
        }
    }
}
