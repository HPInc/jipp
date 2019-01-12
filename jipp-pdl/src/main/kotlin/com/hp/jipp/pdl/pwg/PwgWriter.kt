package com.hp.jipp.pdl.pwg

import com.hp.jipp.pdl.ColorSpace
import com.hp.jipp.pdl.RenderableDocument
import com.hp.jipp.pdl.RenderablePage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.OutputStream

@SuppressWarnings("MagicNumber")
class PwgWriter(
    outputStream: OutputStream,
    private val caps: PwgCapabilities
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

    fun write(doc: RenderableDocument) {
        writeString("RaS2", 4)
        for (page in doc) {
            write(doc, page)
        }
    }

    /** Return a PWG-encodable value corresponding to this [Boolean]. */
    private fun Boolean.toInt() =
        if (this) 1 else 0

    @Suppress("LongMethod") // It's clearer to do it all here
    private fun write(doc: RenderableDocument, page: RenderablePage) {
        val colorSpace = if (caps.color) ColorSpace.RGB else ColorSpace.GRAYSCALE
        writeString("PwgRaster", 64)
        writeString("", 64) // mediaColor
        writeString("", 64) // mediaType
        writeString("", 64) // printContentOptimize
        writeBlank(12) // reserved
        writeInt(0) // cutMedia
        writeInt(caps.duplex.toInt()) // duplex
        writeInt(doc.dpi) // resolutionX
        writeInt(doc.dpi) // resolutionY
        writeBlank(16) // reserved
        writeInt(0) // insertSheet
        writeInt(0) // jog
        writeInt(0) // leadingEdge
        writeBlank(12) // reserved
        writeInt(0) // mediaPosition
        writeInt(0) // mediaWeight
        writeBlank(8) // reserved
        writeInt(0) // numCopies
        writeInt(0) // orientation
        writeBlank(4) // reserved
        writeInt(page.widthPixels * POINTS_PER_INCH / doc.dpi) // pageSizeX
        writeInt(page.heightPixels * POINTS_PER_INCH / doc.dpi) // pageSizeY
        writeBlank(8) // reserved
        writeInt(if (caps.duplex) caps.tumble.toInt() else 0) // tumble but only if duplex
        writeInt(page.widthPixels) // width (pixels)
        writeInt(page.heightPixels) // height (pixels)
        writeBlank(4) // reserved
        writeInt(8) // bitsPerColor
        writeInt(8 * colorSpace.bytesPerPixel) // bitsPerPixel
        writeInt(page.widthPixels * colorSpace.bytesPerPixel) // bytesPerLine
        writeInt(0) // colorOrder
        writeInt(colorSpace.colorSpaceEnum) // colorSpace (RGB or Grayscale)
        writeBlank(16) // reserved
        writeInt(colorSpace.bytesPerPixel) // numColors
        writeBlank(28) // reserved
        writeInt(0) // totalPageCount
        writeInt(0) // crossFeedTransform
        writeInt(0) // feedTransform
        writeInt(0) // imageBoxLeft
        writeInt(0) // imageBoxTop
        writeInt(0) // imageBoxRight
        writeInt(0) // imageBoxBottom
        writeInt(0xFFFFFF) // alternatePrimary (white pixel)
        writeInt(0) // printQuality
        writeBlank(20)
        writeInt(0) // vendorIdentifier
        writeInt(0) // vendorLength
        writeBlank(1088) // vendor
        writeBlank(64) // reserved
        writeString("", 64) // renderingIntent
        writeString("", 64) // pageSizeName

        var yOffset = 0
        val packer = PackBits(bytesPerPixel = colorSpace.bytesPerPixel, pixelsPerLine = page.widthPixels)
        var size = 0
        var byteArray: ByteArray? = null
        while (yOffset < page.heightPixels) {
            val height = Math.min(64, page.heightPixels - yOffset)
            val renderSize = page.renderSize(height, colorSpace)
            if (byteArray?.size != renderSize) {
                byteArray = ByteArray(renderSize)
            }
            page.render(yOffset, height, colorSpace, byteArray)
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

        /** Return a ColorSpaceEnum value corresponding to the supplied [ColorSpace]. */
        private val ColorSpace.colorSpaceEnum: Int
            get() = when (this) {
                ColorSpace.GRAYSCALE -> 18
                else -> 19
            }
    }
}
