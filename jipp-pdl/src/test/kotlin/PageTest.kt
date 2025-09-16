// © Copyright 2018 - 2021 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

import com.hp.jipp.pdl.ColorSpace
import org.junit.Assert.assertEquals
import org.junit.Test
import util.PageUtil.BLACK_BYTE
import util.PageUtil.BLUE
import util.PageUtil.RED
import util.PageUtil.describe
import util.PageUtil.fakePage

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
}
