// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.pdl.pwg

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.* // ktlint-disable no-wildcard-imports

/**
 * Encoder/Decoder for the PackBits algorithm described in the Wi-Fi Peer-to-Peer Services Print Technical
 * Specification v1.1
 */
class PackBits(
    /** Number of bytes per pixel (1 for grayscale, 3 for RGB) */
    private val bytesPerPixel: Int,
    /** Total number of pixels on each horizontal line */
    private val pixelsPerLine: Int
) {

    /** Reads [inputPixels] until there are no more, writing encoded bytes to [outputBytes] */
    fun encode(inputPixels: InputStream, outputBytes: OutputStream) {
        EncodeContext(inputPixels, outputBytes, bytesPerPixel, pixelsPerLine).encode()
    }

    /** Manage the mutable context during encoding */
    private class EncodeContext(
        private val pixelsIn: InputStream,
        private val bytesOut: OutputStream,
        private val bytesPerPixel: Int,
        pixelsPerLine: Int
    ) {
        private val bytesPerLine = bytesPerPixel * pixelsPerLine
        private var lineArrayValid = false
        private var lineArray = ByteArray(bytesPerLine)
        private var nextLineArrayValid = false
        private var nextLineArray = ByteArray(bytesPerLine)
        private var lineRepeatCount = 0
        private var bytePos: Int = 0
        private var pixelCount: Int = 0

        /** Consume the next event from the current encoding context */
        fun encode() {
            while (readNextLine()) {
                bytesOut.write(lineRepeatCount - 1)
                encodePixelGroups()
            }
        }

        private fun encodePixelGroups() {
            bytePos = 0
            while (bytePos < lineArray.size) {
                if (bytePos + bytesPerPixel == lineArray.size) {
                    // Exactly one pixel left so encode it as repeating of 1
                    pixelCount = 1
                    encodeRepeatingPixelGroup()
                } else if (lineArray.equals(bytePos, bytesPerPixel, lineArray, bytePos + bytesPerPixel)) {
                    pixelCount = 2
                    seekNonMatchingPixel()
                    encodeRepeatingPixelGroup()
                } else {
                    // Non-repeating pixels, seek the first two matching pixels at end
                    pixelCount = 2
                    seekMatchingPixels()
                    if (pixelCount == 1) {
                        encodeRepeatingPixelGroup()
                    } else {
                        encodeNonRepeatingPixelGroup()
                    }
                }
            }
        }

        private fun seekNonMatchingPixel() {
            // Multiple repeating pixels, seek EOL or non-matching pixel
            var nextPixelIndex = bytePos + pixelCount * bytesPerPixel
            while (pixelCount < MAX_GROUP &&
                    nextPixelIndex < lineArray.size &&
                    lineArray.equals(bytePos, bytesPerPixel, lineArray, nextPixelIndex)) {
                pixelCount++
                nextPixelIndex += bytesPerPixel
            }
        }

        private fun seekMatchingPixels() {
            var nextPixelIndex = bytePos + pixelCount * bytesPerPixel
            while (nextPixelIndex < lineArray.size && pixelCount < MAX_GROUP) {
                if (lineArray.equals(nextPixelIndex - bytesPerPixel, bytesPerPixel,
                                lineArray, nextPixelIndex)) {
                    // We found two matching pixels so back up
                    pixelCount--
                    break
                } else {
                    pixelCount++
                    nextPixelIndex += bytesPerPixel
                }
            }
        }

        private fun encodeRepeatingPixelGroup() {
            bytesOut.write(pixelCount - 1)
            bytesOut.write(lineArray, bytePos, bytesPerPixel)
            bytePos += pixelCount * bytesPerPixel
        }

        private fun encodeNonRepeatingPixelGroup() {
            bytesOut.write(NON_REPEAT_SUBTRACT_FROM - pixelCount)
            bytesOut.write(lineArray, bytePos, bytesPerPixel * pixelCount)
            bytePos += pixelCount * bytesPerPixel
        }

        /** Compare a section of this ByteArray with a section of the same length in another byte array */
        private fun ByteArray.equals(offset: Int, length: Int, other: ByteArray, otherOffset: Int): Boolean {
            for (index in 0 until length) {
                if (this[offset + index] != other[otherOffset + index]) return false
            }
            return true
        }

        private fun readNextLine(): Boolean {
            lineArrayValid = false

            // Take the next line if we can
            if (nextLineArrayValid) {
                val swap = lineArray
                lineArray = nextLineArray
                nextLineArray = swap
                nextLineArrayValid = false
                lineArrayValid = true
            }

            // Read a new line if we need it
            if (!lineArrayValid) {
                if (!readLine(lineArray)) return false
            }

            // Now read additional lines beyond lineIn to see if there are repeats
            lineRepeatCount = 1
            while (lineRepeatCount <= MAX_LINE_REPEAT) {
                if (readLine(nextLineArray)) {
                    if (Arrays.equals(lineArray, nextLineArray)) {
                        lineRepeatCount++
                    } else {
                        // We found a different line so hold for later
                        nextLineArrayValid = true
                        break
                    }
                } else {
                    // No more lines to read
                    nextLineArrayValid = false
                    break
                }
            }
            return true
        }

        private fun readLine(into: ByteArray): Boolean {
            return when (val bytesRead = pixelsIn.read(into)) {
                -1 -> false
                into.size -> true
                else -> throw IOException("Could not read whole line ($bytesRead bytes instead of ${lineArray.size}")
            }
        }
    }

    /**
     * Read PackBits-encoded [inputBytes] into pixels in the OutputStream until [lines] have been reached.
     */
    fun decode(inputBytes: InputStream, outputPixels: OutputStream, lines: Int) {
        var linesWritten = 0
        while (linesWritten < lines) {
            val lineRepeat = inputBytes.read()
            if (lineRepeat == -1) throw IOException("Too few lines (read $linesWritten, expected $lines)")
            val line: ByteArray = decodeLine(inputBytes, pixelsPerLine)
            for (i in 0 until (lineRepeat + 1)) {
                linesWritten++
                outputPixels.write(line)
            }
        }
        if (linesWritten > lines) throw IOException("Too many lines (read $linesWritten, expected $lines)")
    }

    private fun decodeLine(bytes: InputStream, pixelsPerLine: Int): ByteArray {
        val pixels = ByteArrayOutputStream()
        val pixel = ByteArray(bytesPerPixel)
        while (pixels.size() < pixelsPerLine * bytesPerPixel) {
            val control = bytes.read()
            if (control == -1) throw IOException("EOF before EOL")
            if (control < MAX_GROUP) {
                bytes.read(pixel)
                for (i in 0 until control + 1) {
                    pixels.write(pixel)
                }
            } else {
                // 257 - control = count
                for (i in 0 until (NON_REPEAT_SUBTRACT_FROM - control)) {
                    bytes.read(pixel)
                    pixels.write(pixel)
                }
            }
        }
        if (pixels.size() > pixelsPerLine * bytesPerPixel) {
            throw IOException("Line too long; ${pixels.size()} with max ${pixelsPerLine * bytesPerPixel}")
        }
        return pixels.toByteArray()
    }

    companion object {
        private const val MAX_GROUP = 128
        private const val MAX_LINE_REPEAT = 256
        private const val NON_REPEAT_SUBTRACT_FROM = 257
    }
}
