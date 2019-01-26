// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package pwg

import com.hp.jipp.pdl.pwg.PackBits
import com.hp.jipp.pdl.pwg.PwgHeader
import com.hp.jipp.pdl.pwg.PwgWriter
import com.hp.jipp.util.toWrappedHexString
import org.junit.Assert.assertEquals
import org.junit.Test
import util.ByteWindow
import util.RandomDocument
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

class PwgWriterTest {
    @Test
    fun validateGeneratedPwg() {
        val name = "validateGeneratedPwg"
        val outPwg = File("build/$name.pwg")
        // Create and load a sample PDF
        val doc = RandomDocument(123L, 2, 72.0, 72.0, 300)

        // Create and write as PWG
        PwgWriter(outPwg.outputStream()).use {
            it.write(doc)
        }

        // Load and validate
        outPwg.inputStream().use {
            PwgValidator.validate(it)
        }
    }

    @Test
    fun cycleDefaultHeader() {
        val header = PwgHeader(bitsPerColor = 8, bitsPerPixel = 24,
            colorSpace = PwgHeader.ColorSpace.Srgb,
            hwResolutionX = 300,
            hwResolutionY = 300,
            height = 1000,
            width = 2000)
        val output = ByteArrayOutputStream()
        header.write(output)
        val read = PwgHeader.read(ByteArrayInputStream(output.toByteArray()))
        assertEquals(header.toString(), read.toString())
    }

    @Test
    fun cycleHeader() {
        val output = ByteArrayOutputStream()
        val doc = RandomDocument(123L, 2, 72.0, 72.0, 300)
        PwgWriter(output).use {
            it.write(doc)
        }
        val originalHeader = output.toByteArray().sliceArray(4 until PwgHeader.HEADER_SIZE + 4)
        val header = PwgHeader.read(ByteArrayInputStream(originalHeader))
        val headerOutput = ByteArrayOutputStream().also { header.write(it) }
        assertEquals(PwgHeader.HEADER_SIZE, headerOutput.size())
        assertEquals(originalHeader.toWrappedHexString(), headerOutput.toByteArray().toWrappedHexString())
    }

    @Test
    fun packRepeatAndNon() {
        cyclePackBits(60, 1, 1, "AAAAAAAAAAAACBAACCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC")
    }

    @Test
    fun packRepeatAndNonOnce() {
        cyclePackBits(40, 1, 1, "AAAAAAAAAAAAABAAAAAAAAAAAAAAAAAAAAAAAAAA")
    }

    @Test
    fun packExtendedRepeat() {
        cyclePackBits(130, 1, 1, original =
        "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBCCCAAA")
    }

    @Test
    fun packNonRepeat() {
        cyclePackBits(lineLength = 130, lines = 1, bytesPerPixel = 2, original = "ab".repeat(130))
    }

    @Test
    fun packMultiNonRepeatLine() {
        cyclePackBits(lineLength = 130, lines = 2, bytesPerPixel = 2, original = "ab".repeat(130).repeat(2))
    }

    @Test
    fun repeatingLines() {
        cyclePackBits(lineLength = 20, lines = 6, bytesPerPixel = 2,
                original = "ab".repeat(20).repeat(2) + "cd".repeat(20).repeat(2) + "ab".repeat(20).repeat(2))
    }

    @Test
    fun packRandom1ByteBuffer() {
        val lineLength = 20
        val lines = 10
        val original = randomBuffer("ABC", 1, lineLength * lines)
        cyclePackBits(lineLength, bytesPerPixel = 1, lines = lines, original = original)
    }

    @Test
    fun packRandom3ByteBuffer() {
        val lineLength = 60
        val lines = 1
        val original = randomBuffer("AaABbBCcCDdDEeE", 3, lineLength * lines)
        cyclePackBits(lineLength, lines = lines, bytesPerPixel = 3, original = original)
    }

    @Test
    fun packLargeBuffer() {
        val outputBytes = ByteArrayOutputStream()
        val lineLength = 3000
        val bytesPerPixel = 1
        var lines = 0

        // Make sure we have a big repeating section somewhere
        randomBuffer("abc", bytesPerPixel, lineLength).also {
            for (i in 0 until 300) outputBytes.write(it)
        }
        lines += 300

        // Now write random lines
        outputBytes.write(randomBuffer("abc", bytesPerPixel, lineLength * (3000 - lines)))

        val outputByteArray = outputBytes.toByteArray()
        val encodedOutput = ByteArrayOutputStream()

        PackBits(bytesPerPixel, lineLength).encode(ByteArrayInputStream(outputByteArray), encodedOutput)

        val decodedStream = ByteArrayOutputStream()
        PackBits(bytesPerPixel, lineLength).decode(ByteArrayInputStream(encodedOutput.toByteArray()), decodedStream, lines)
    }

    private fun randomBuffer(palette: String, bytesPerPixel: Int, totalPixels: Int): ByteArray {
        val bytesOut = ByteArrayOutputStream()
        val pixels = palette.chunked(bytesPerPixel).map { it.toByteArray() }

        while (bytesOut.size() < totalPixels * bytesPerPixel) {
            val length = Math.min(totalPixels - bytesOut.size() / bytesPerPixel,
                when ((Math.random() * 6).toInt()) {
                    0 -> (Math.random() * 130).toInt()
                    1 -> 2
                    else -> 1
                })
            when ((Math.random() * 2).toInt()) {
                0 -> {
                    // Repeat
                    val pixel = pixels.choose()
                    for (i in 0 until length) bytesOut.write(pixel)
                }
                else -> {
                    for (i in 0 until length) bytesOut.write(pixels.choose())
                }
            }
        }
        return bytesOut.toByteArray()
    }

    /** Make a random choice of things in an array, returning the choice */
    private fun <T> List<T>.choose(): T = get((Math.random() * size).toInt())

    private fun cyclePackBits(lineLength: Int, lines: Int, bytesPerPixel: Int, original: String) {
        println("Original:\n" + original.chunked(lineLength * bytesPerPixel).joinToString("\n"))
        cyclePackBits(lineLength, lines, bytesPerPixel, original.toByteArray())
    }

    private fun cyclePackBits(lineLength: Int, lines: Int, bytesPerPixel: Int, original: ByteArray) {
        val out = ByteArrayOutputStream()
        PackBits(bytesPerPixel, lineLength).encode(ByteArrayInputStream(original), out)
        println("Packed ${original.size} into ${out.size()}:")
        println(ByteWindow(out.toByteArray()).toString(out.size()))

        val restored = ByteArrayOutputStream()
        PackBits(bytesPerPixel, lineLength).decode(ByteArrayInputStream(out.toByteArray()), restored, lines = lines)
        println("Restored to ${restored.size()}:")
        val restoredString = String(restored.toByteArray())
        println(restoredString.chunked(lineLength * bytesPerPixel).joinToString("\n"))
        assertEquals(String(original), restoredString)
    }
}
