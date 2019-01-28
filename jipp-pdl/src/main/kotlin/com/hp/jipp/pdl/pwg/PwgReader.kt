// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.pdl.pwg

import com.hp.jipp.pdl.ColorSpace
import com.hp.jipp.pdl.RenderableDocument
import com.hp.jipp.pdl.RenderablePage
import com.hp.jipp.pdl.util.NullOutputStream
import com.hp.jipp.pdl.util.WrappedByteArrayOutputStream
import com.hp.jipp.util.toHexString
import com.sun.xml.internal.messaging.saaj.util.TeeInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.IOException
import java.io.InputStream

/**
 * An [InputStream] that can read a [RenderableDocument] from PWG-Raster input
 */
class PwgReader(inputStream: InputStream) : DataInputStream(inputStream) {

    /**
     * Return a [PwgDocument] from input, throwing IOException for unrecoverable parsing errors.
     */
    fun readDocument() = PwgDocument(this)

    /**
     * A document sourced from PWG-Raster input. [RenderablePage] objects produced by this document can
     * be assumed to be [PwgPage] objects.
     */
    class PwgDocument internal constructor(input: DataInputStream) : RenderableDocument() {
        private var _dpi = -1

        override val dpi: Int
            get() = _dpi

        private val pages = mutableListOf<PwgPage>()

        init {
            val magicNumbers = ByteArray(PwgWriter.MAGIC_NUMBER.size)
            input.read(magicNumbers)
            if (!magicNumbers.contentEquals(PwgWriter.MAGIC_NUMBER)) {
                throw IOException("Expected ${PwgWriter.MAGIC_NUMBER.toHexString()} but read " +
                    magicNumbers.toHexString())
            }

            val headerBytes = ByteArray(PwgHeader.HEADER_SIZE)
            while (input.read(headerBytes) != -1) {
                val header = readValidHeader(headerBytes)
                pages += readPwgPage(header, input)
            }
        }

        override fun iterator(): Iterator<PwgPage> = pages.iterator()

        private fun readValidHeader(headerBytes: ByteArray): PwgHeader {
            val header = PwgHeader.read(ByteArrayInputStream(headerBytes))
            if (header.hwResolutionX != header.hwResolutionY) {
                throw IOException("Non-square resolutions not supported")
            }
            if (dpi != -1 && dpi != header.hwResolutionX) {
                throw IOException("All pages must have the same resolution (found $dpi and ${header.hwResolutionX}")
            }
            _dpi = header.hwResolutionX
            return header
        }

        private fun readPwgPage(header: PwgHeader, input: InputStream): PwgPage {
            val pageBytes = ByteArrayOutputStream()
            // Decode and throw away input
            header.packBits.decode(TeeInputStream(input, pageBytes), NullOutputStream, header.height)
            return PwgPage(header, pageBytes.toByteArray())
        }
    }

    class PwgPage(
        val header: PwgHeader,
        private val pageBytes: ByteArray
    ) : RenderablePage(header.width, header.height) {

        override fun render(yOffset: Int, swathHeight: Int, colorSpace: ColorSpace, byteArray: ByteArray) {
            val input = ByteArrayInputStream(pageBytes)
            header.packBits.decode(input, NullOutputStream, yOffset)
            if (PwgHeader.ColorSpace.from(colorSpace) == header.colorSpace) {
                // Decode directly to the target byte array
                header.packBits.decode(input, WrappedByteArrayOutputStream(byteArray), swathHeight)
            } else {
                // Decode into an intermediate buffer, then convert
                val pixelOutput = ByteArrayOutputStream()
                header.packBits.decode(input, pixelOutput, swathHeight)
                header.colorSpace.toPdlColorSpace().convert(
                    ByteArrayInputStream(pixelOutput.toByteArray()),
                    WrappedByteArrayOutputStream(byteArray), colorSpace)
            }
        }
    }
}
