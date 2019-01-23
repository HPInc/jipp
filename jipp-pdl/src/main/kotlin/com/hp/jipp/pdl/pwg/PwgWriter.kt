// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.pdl.pwg

import com.hp.jipp.pdl.ColorSpace
import com.hp.jipp.pdl.RenderableDocument
import com.hp.jipp.pdl.RenderablePage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.OutputStream

typealias PwgHeaderBuilder = (RenderableDocument, RenderablePage) -> PwgHeader

@SuppressWarnings("MagicNumber")
class PwgWriter
@JvmOverloads constructor(
    outputStream: OutputStream,
    /**
     * A header builder, used to construct an appropriate PwgHeader for each page of a document.
     * [rgbHeaderBuilder], [grayscaleHeaderBuilder], or a custom builder may be used.
     */
    private val headerBuilder: PwgHeaderBuilder
) : DataOutputStream(outputStream) {

    /** Write a null-terminated [string] including up to [width] bytes*/
    private fun writeString(string: String, width: Int) {
        val bytes = string.toByteArray()
        write(bytes, 0, Math.min(width, bytes.size))
        val remaining = width - bytes.size
        if (remaining > 0) {
            writeBlank(remaining)
        }
    }

    /** Write a document to the writer's [outputStream]. */
    fun write(doc: RenderableDocument) {
        writeString("RaS2", 4)
        doc.forEach { page ->
            write(doc, page, headerBuilder(doc, page))
        }
    }

    /** Return a PWG-encodable value corresponding to this [Boolean]. */
    private fun Boolean.toInt() =
        if (this) 1 else 0

    @Suppress("LongMethod") // It's clearer to do it all here
    private fun write(doc: RenderableDocument, page: RenderablePage, header: PwgHeader) {

        header.write(this)

        // TODO: The manipulation of ColorSpace feels cheap here. Should PwgHeader.ColorSpace implement jipp.ColorSpace?

        var yOffset = 0
        val bytesPerPixel = header.bitsPerPixel / 8
        val packer = PackBits(bytesPerPixel = bytesPerPixel, pixelsPerLine = header.bytesPerLine / bytesPerPixel)
        var size = 0
        var byteArray: ByteArray? = null
        while (yOffset < page.heightPixels) {
            val height = Math.min(64, page.heightPixels - yOffset)
            val renderSize = page.renderSize(bytesPerPixel * page.widthPixels * height)
            if (byteArray?.size != renderSize) {
                byteArray = ByteArray(renderSize)
            }
            page.render(yOffset, height, header.colorSpace.toJippColorSpace(), byteArray)
            val encodedBytes = ByteArrayOutputStream()
            packer.encode(ByteArrayInputStream(byteArray), encodedBytes)
            write(encodedBytes.toByteArray())
            size += encodedBytes.size()
            yOffset += height
        }
    }

    private fun writeBlank(bytes: Int) {
        write(ByteArray(bytes))
    }

    companion object {
        const val POINTS_PER_INCH = 72
        const val BITS_PER_BYTE = 8
        val rgbHeaderBuilder = { doc: RenderableDocument, page: RenderablePage ->
            PwgHeader(
                hwResolutionX = doc.dpi, hwResolutionY = doc.dpi,
                pageSizeX = page.widthPixels * POINTS_PER_INCH / doc.dpi,
                pageSizeY = page.heightPixels * POINTS_PER_INCH / doc.dpi,
                width = page.widthPixels, height = page.heightPixels,
                bitsPerColor = BITS_PER_BYTE,
                bitsPerPixel = ColorSpace.RGB.bytesPerPixel * BITS_PER_BYTE,
                colorSpace = PwgHeader.ColorSpace.Srgb,
                numColors = ColorSpace.RGB.bytesPerPixel
            )
        }

        val grayscaleHeaderBuilder = { doc: RenderableDocument, page: RenderablePage ->
            PwgHeader(
                hwResolutionX = doc.dpi, hwResolutionY = doc.dpi,
                pageSizeX = page.widthPixels * POINTS_PER_INCH / doc.dpi,
                pageSizeY = page.heightPixels * POINTS_PER_INCH / doc.dpi,
                width = page.widthPixels, height = page.heightPixels,
                bitsPerColor = BITS_PER_BYTE,
                bitsPerPixel = ColorSpace.GRAYSCALE.bytesPerPixel * BITS_PER_BYTE,
                colorSpace = PwgHeader.ColorSpace.Sgray,
                numColors = ColorSpace.GRAYSCALE.bytesPerPixel
            )
        }

        /** Return a ColorSpaceEnum value corresponding to the supplied [ColorSpace]. */
        private val ColorSpace.colorSpaceEnum: Int
            get() = when (this) {
                ColorSpace.GRAYSCALE -> 18
                else -> 19
            }

        private fun PwgHeader.ColorSpace.toJippColorSpace(): ColorSpace {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }
}
