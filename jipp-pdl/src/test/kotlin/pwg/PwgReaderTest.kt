// © Copyright 2019 - 2021 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package pwg

import com.hp.jipp.model.PrintQuality
import com.hp.jipp.model.Sides
import com.hp.jipp.pdl.ColorSpace
import com.hp.jipp.pdl.OutputSettings
import com.hp.jipp.pdl.RenderableDocument
import com.hp.jipp.pdl.pwg.PackBits
import com.hp.jipp.pdl.pwg.PwgHeader
import com.hp.jipp.pdl.pwg.PwgReader
import com.hp.jipp.pdl.pwg.PwgSettings
import com.hp.jipp.pdl.pwg.PwgSettings.Companion.BITS_PER_BYTE
import com.hp.jipp.pdl.pwg.PwgWriter
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Ignore
import org.junit.Test
import util.KotlinTest
import util.PageUtil
import util.PageUtil.BLUE
import util.PageUtil.describe
import util.PageUtil.fakePage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Locale

class PwgReaderTest {

    @Ignore // To use this drop a document into "resources" and comment out this line
    @Test fun `read from disk`() {
        PwgReader(javaClass.getResourceAsStream("/document_001.pwg")!!).readDocument().forEach {
            println((it as PwgReader.PwgPage).header)
        }
    }

    @Test fun simple() {
        val doc = object : RenderableDocument() {
            override val dpi: Int = 1
            val pages = listOf(fakePage(BLUE, ColorSpace.Rgb))
            override fun iterator() = pages.iterator()
        }

        val output = ByteArrayOutputStream()
        PwgWriter(output).write(doc)

        val read = PwgReader(ByteArrayInputStream(output.toByteArray())).readDocument()
        assertEquals(doc.dpi, read.dpi)
        val page = read.first() as PwgReader.PwgPage
        describe(page, ColorSpace.Rgb).also {
            println(it)
            assertEquals("...B...........", it.split("\n")[3])
        }
    }

    @Test fun multiPage() {
        val doc = object : RenderableDocument() {
            override val dpi: Int = 1
            val pages = listOf(
                fakePage(BLUE, ColorSpace.Rgb),
                fakePage(byteArrayOf(PageUtil.BLACK_BYTE), ColorSpace.Grayscale)
            )
            override fun iterator() = pages.iterator()
        }

        val output = ByteArrayOutputStream()
        PwgWriter(output, settings = PwgSettings(output = OutputSettings())).write(doc)

        val read = PwgReader(ByteArrayInputStream(output.toByteArray())).readDocument()
        assertEquals(doc.dpi, read.dpi)
        val page = read.toList()[0] as PwgReader.PwgPage
        describe(page, ColorSpace.Rgb).also {
            println(it)
            assertEquals("...B...........", it.split("\n")[3])
        }
        val page2 = read.toList()[1] as PwgReader.PwgPage
        describe(page2, ColorSpace.Rgb).also {
            println(it)
            assertEquals("...K...........", it.split("\n")[3])
        }
    }

    @Test fun `allowPadding allows padding of two-sided job`() {
        val doc = object : RenderableDocument() {
            override val dpi: Int = 1
            val pages = listOf(fakePage(BLUE, ColorSpace.Rgb))
            override fun iterator() = pages.iterator()
        }

        val output = ByteArrayOutputStream()
        PwgWriter(output, settings = PwgSettings(output = OutputSettings(sides = Sides.twoSidedLongEdge), allowPadding = true)).write(doc)

        val read = PwgReader(ByteArrayInputStream(output.toByteArray())).readDocument()
        assertEquals(2, read.toList().size)
    }

    @Test fun colorToGray() {
        val doc = object : RenderableDocument() {
            override val dpi: Int = 1
            val pages = listOf(fakePage(BLUE, ColorSpace.Rgb))
            override fun iterator() = pages.iterator()
        }

        val output = ByteArrayOutputStream()
        PwgWriter(output).write(doc)

        val read = PwgReader(ByteArrayInputStream(output.toByteArray())).readDocument()
        assertEquals(doc.dpi, read.dpi)
        val page = read.first() as PwgReader.PwgPage
        describe(page, ColorSpace.Grayscale).also {
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
            val pages = listOf(fakePage(BLUE, ColorSpace.Rgb))
            override fun iterator() = pages.iterator()
        }

        val output = ByteArrayOutputStream()
        PwgWriter(output).write(doc)
        val read = PwgReader(ByteArrayInputStream(output.toByteArray())).readDocument()
        val page = read.first() as PwgReader.PwgPage
        KotlinTest.cover(page.header, page.header.copy(), page.header.copy(imageBoxBottom = 5))
        PwgHeader.MediaPosition.values().forEach { pos ->
            val ippName = (pos.name.first().lowercaseChar() + pos.name.drop(1)) // decapitalize first letter
                .split("(?=[A-Z0-9]+)".toRegex()).joinToString("-") // add dashes back
                .replace("[0-9][0-9-]+".toRegex()) { it.value.replace("-", "") } // fix tray-1-0
                .lowercase(Locale.getDefault())
            PwgSettings(output = OutputSettings(source = ippName))
        }

        PwgSettings(output = OutputSettings(quality = PrintQuality.draft))
        PwgSettings(output = OutputSettings(quality = PrintQuality.normal))
        PwgSettings(output = OutputSettings(quality = PrintQuality.high))

        val pwgHeader = PwgHeader(
            bitsPerColor = BITS_PER_BYTE,
            bitsPerPixel = BITS_PER_BYTE * ColorSpace.Rgb.bytesPerPixel, colorSpace = PwgHeader.ColorSpace.Srgb,
            width = 100, height = 100, hwResolutionY = 100, hwResolutionX = 100
        )
        KotlinTest.cover(pwgHeader, pwgHeader.copy(), pwgHeader.copy(alternatePrimary = 0))
    }
}
