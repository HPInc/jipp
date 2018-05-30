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
    private val caps: PwgCapabilities,
    private val dpi: Int
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
        for (page in doc.reversed()) {
            write(page)
        }
    }

    @Suppress("LongMethod") // It's clearer to do it all here
    private fun write(page: RenderablePage) {
        val pixelWidth = RenderablePage.pointsToPixels(dpi, page.widthPoints)
        val pixelHeight = RenderablePage.pointsToPixels(dpi, page.heightPoints)
        val colorSpace = if (caps.color) ColorSpace.RGB else ColorSpace.GRAYSCALE
        writeString("PwgRaster", 64)
        writeString("", 64) // mediaColor
        writeString("", 64) // mediaType
        writeString("", 64) // printContentOptimize
        writeBlank(12) // reserved
        writeInt(0) // cutMedia
        writeInt(0) // duplex
        writeInt(dpi) // resolutionX
        writeInt(dpi) // resolutionY
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
        writeInt(page.widthPoints.toInt()) // pageSizeX
        writeInt(page.heightPoints.toInt()) // pageSizeY
        writeBlank(8) // reserved
        writeInt(0) // tumble
        writeInt(pixelWidth) // width (pixels)
        writeInt(pixelHeight) // height (pixels)
        writeBlank(4) // reserved
        writeInt(8) // bitsPerColor
        writeInt(24) // bitsPerPixel
        writeInt(pixelWidth * colorSpace.bytesPerPixel) // bytesPerLine
        writeInt(0) // colorOrder
        writeInt(19) // colorSpace (RGB)
        writeBlank(16) // reserved
        writeInt(3) // numColors
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
        val packer = PackBits(bytesPerPixel = colorSpace.bytesPerPixel, pixelsPerLine = pixelWidth)
        var size = 0
        var byteArray: ByteArray? = null
        while (yOffset < pixelHeight) {
            val height = Math.min(64, pixelHeight - yOffset)
            val renderSize = page.renderSize(dpi, height, colorSpace)
            if (byteArray?.size != renderSize) {
                byteArray = ByteArray(renderSize)
            }
            page.render(dpi, yOffset, height, colorSpace, byteArray)
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
}
