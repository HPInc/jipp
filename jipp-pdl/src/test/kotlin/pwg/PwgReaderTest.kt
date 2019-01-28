// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package pwg

import com.hp.jipp.model.PrintQuality
import com.hp.jipp.pdl.ColorSpace
import com.hp.jipp.pdl.RenderableDocument
import com.hp.jipp.pdl.pwg.PackBits
import com.hp.jipp.pdl.pwg.PwgHeader
import com.hp.jipp.pdl.pwg.PwgReader
import com.hp.jipp.pdl.pwg.PwgSettings
import com.hp.jipp.pdl.pwg.PwgWriter
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException

class PwgReaderTest {
    @Test fun simple() {
        // Let's create a one-page, small, grayscale PWG raster document.
        val doc = object : RenderableDocument() {
            override val dpi: Int = 1
            val pages = listOf(PageTest.fakePage(PageTest.BLUE, ColorSpace.Rgb))
            override fun iterator() = pages.iterator()
        }

        val output = ByteArrayOutputStream()
        PwgWriter(output).write(doc)

        val read = PwgReader(ByteArrayInputStream(output.toByteArray())).readDocument()
        assertEquals(doc.dpi, read.dpi)
        val page = read.first() as PwgReader.PwgPage
        PageTest.toString(page, ColorSpace.Rgb).also {
            println(it)
            assertEquals("...B...........", it.split("\n")[3])
        }
    }

    @Test fun multiPage() {
        // Let's create a one-page, small, grayscale PWG raster document.
        val doc = object : RenderableDocument() {
            override val dpi: Int = 1
            val pages = listOf(
                PageTest.fakePage(PageTest.BLUE, ColorSpace.Rgb),
                PageTest.fakePage(byteArrayOf(PageTest.BLACK_BYTE), ColorSpace.Grayscale))
            override fun iterator() = pages.iterator()
        }

        val output = ByteArrayOutputStream()
        PwgWriter(output).write(doc)

        val read = PwgReader(ByteArrayInputStream(output.toByteArray())).readDocument()
        assertEquals(doc.dpi, read.dpi)
        val page = read.toList()[0] as PwgReader.PwgPage
        PageTest.toString(page, ColorSpace.Rgb).also {
            println(it)
            assertEquals("...B...........", it.split("\n")[3])
        }
        val page2 = read.toList()[1] as PwgReader.PwgPage
        PageTest.toString(page2, ColorSpace.Rgb).also {
            println(it)
            assertEquals("...K...........", it.split("\n")[3])
        }
    }

    @Test fun shortLine() {
        // Ask PackBits to decode something which can't be decoded
        val bits = ByteArrayOutputStream()
        val pixels = ByteArrayInputStream(ByteArray(200))
        val packer = PackBits(2, 100)
        packer.encode(pixels, bits)
        val shortPacker = PackBits(2, 50)
        try {
            shortPacker.decode(ByteArrayInputStream(bits.toByteArray()), ByteArrayOutputStream(), 2)
            fail("Should have thrown")
        } catch (e: IOException) {
            // As anticipated
        }
    }

    @Test fun longLine() {
        // Ask PackBits to decode something which can't be decoded
        val bits = ByteArrayOutputStream()
        val pixels = ByteArrayInputStream(ByteArray(100))
        val packer = PackBits(2, 50)
        packer.encode(pixels, bits)
        val longPacker = PackBits(2, 100)
        try {
            longPacker.decode(ByteArrayInputStream(bits.toByteArray()), ByteArrayOutputStream(), 1)
            fail("Should have thrown")
        } catch (e: IOException) {
            // As anticipated
        }
    }

    @Test fun cover() {
        // Run typical operations to validate different classes defined for the PWG decoder
        val doc = object : RenderableDocument() {
            override val dpi: Int = 1
            val pages = listOf(PageTest.fakePage(PageTest.BLUE, ColorSpace.Rgb))
            override fun iterator() = pages.iterator()
        }

        val output = ByteArrayOutputStream()
        PwgWriter(output).write(doc)
        val read = PwgReader(ByteArrayInputStream(output.toByteArray())).readDocument()
        val page = read.first() as PwgReader.PwgPage
        KotlinTest.cover(page.header, page.header.copy(), page.header.copy(imageBoxBottom = 5))
        PwgHeader.MediaPosition.values().forEach { pos ->
            val ippName = (pos.name.first().toLowerCase() + pos.name.drop(1)) // decapitalize first letter
                .split("(?=[A-Z0-9]+)".toRegex()).joinToString("-") // add dashes back
                .replace("[0-9][0-9-]+".toRegex()) { it.value.replace("-", "") } // fix tray-1-0
                .toLowerCase()
            PwgSettings().copy(source = ippName)
        }

        PwgSettings().copy(quality = PrintQuality.draft)
        PwgSettings().copy(quality = PrintQuality.normal)
        PwgSettings().copy(quality = PrintQuality.high)
    }
}
