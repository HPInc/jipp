// Copyright 2018 - 2024 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package pwg

import com.hp.jipp.pdl.pwg.PackBits
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import util.ByteWindow
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.InputStream

object PwgValidator {
    private const val HEADER_SIZE = 1796

    fun validate(inputStream: InputStream) = inputStream.validatePwg()

    private fun InputStream.validatePwg() {
        val word = ByteArray(4)
        read(word)
        assertArrayEquals("RaS2".toByteArray(), word)

        // Until we run out of pages
        while (true) {
            val header = ByteArray(HEADER_SIZE)
            val bytesRead = read(header)
            if (bytesRead == -1) return
            assertEquals(HEADER_SIZE, bytesRead)
            val input = DataInputStream(ByteArrayInputStream(header))
            assertEquals("PwgRaster", input.readNullString(64))
            val mediaColor = input.readNullString(64)
            val mediaType = input.readNullString(64)
            val printContentOptimize = input.readNullString(64)
            input.skip(12)
            val cutMedia = input.readInt()
            println("mediaColor=$mediaColor, mediaType=$mediaType, optimize=$printContentOptimize, cutMedia=$cutMedia")
            val duplex = input.readInt()
            val resolutionX = input.readInt()
            val resolutionY = input.readInt()
            // Resolution in dpi
            println("duplex=$duplex, resolutionX=$resolutionX, resolutionY=$resolutionY")
            input.skip(16)
            val insertSheet = input.readInt()
            val jog = input.readInt()
            val leadingEdge = input.readInt()
            println("insertSheet=$insertSheet, jog=$jog, leadingEdge=$leadingEdge")
            input.skip(12)
            val mediaPosition = input.readInt()
            val mediaWeight = input.readInt()
            println("mediaPosition=$mediaPosition, mediaWeight=$mediaWeight")
            input.skip(8)
            val numCopies = input.readInt()
            val orientation = input.readInt()
            input.skip(4)
            // Sizes in points
            val pageSizeX = input.readInt()
            val pageSizeY = input.readInt()
            println("numCopies=$numCopies, orientation=$orientation, pageSizeX=$pageSizeX, pageSizeY=$pageSizeY")
            input.skip(8)
            val tumble = input.readInt()
            val width = input.readInt()
            val height = input.readInt()
            println("tumble=$tumble, width=$width, height=$height")
            input.skip(4)
            val bitsPerColor = input.readInt()
            val bitsPerPixel = input.readInt()
            val bytesPerLine = input.readInt()
            val colorOrder = input.readInt()
            // Color space: 19 = sRGB color, 18 is sRGB grayscale
            val colorSpace = input.readInt()
            println("bitsPerColor=$bitsPerColor, bitsPerPixel=$bitsPerPixel, bytesPerLine=$bytesPerLine, colorOrder=$colorOrder, colorSpace=$colorSpace")
            input.skip(16)
            val numColors = input.readInt()
            input.skip(28)
            val totalPageCount = input.readInt()
            val crossFeedTransform = input.readInt()
            val feedTransform = input.readInt()
            println("numColors=$numColors, totalPageCount=$totalPageCount, crossFeedTransform=$crossFeedTransform, feedTransform=$feedTransform")

            val imageBoxLeft = input.readInt()
            val imageBoxTop = input.readInt()
            val imageBoxRight = input.readInt()
            val imageBoxBottom = input.readInt()
            println("imageBoxLeft=$imageBoxLeft, imageBoxTop=$imageBoxTop, imageBoxRight=$imageBoxRight, imageBoxBottom=$imageBoxBottom")

            val alternatePrimary = input.readInt()
            val printQuality = input.readInt()
            input.skip(20)
            val vendorIdentifier = input.readInt()
            val vendorLength = input.readInt()
            println("alternatePrimary=$alternatePrimary, printQuality=$printQuality, vendorIdentifier=$vendorIdentifier, vendorLength=$vendorLength")

            input.skip(1088) // Vendor data
            input.skip(64) // Reserved
            val renderingIntent = input.readNullString(64)
            val pageSizeName = input.readNullString(64)

            println("renderingIntent=$renderingIntent, pageSizeName=$pageSizeName")

            val imageBytes = ByteArrayOutputStream()
            PackBits(bitsPerPixel = bitsPerPixel, pixelsPerLine = width)
                .decode(this, imageBytes, lines = height)
            println(ByteWindow(imageBytes.toByteArray()).toString(200))
            assertEquals(width * height, imageBytes.size() / (bitsPerPixel / 8))
            break
        }
    }

    private fun DataInputStream.readNullString(byteCount: Int): String {
        val bytes = ByteArray(byteCount)
        read(bytes)
        for (i in 0 until byteCount) {
            if (bytes[i] == 0.toByte()) {
                return String(bytes, 0, i)
            }
        }
        return String(bytes)
    }
}
