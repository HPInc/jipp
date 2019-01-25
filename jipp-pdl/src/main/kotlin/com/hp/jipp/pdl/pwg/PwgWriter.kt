// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.pdl.pwg

import com.hp.jipp.pdl.RenderableDocument
import com.hp.jipp.pdl.RenderablePage
import com.hp.jipp.pdl.handleSides
import com.hp.jipp.pdl.mapPages
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.OutputStream

class PwgWriter
@JvmOverloads constructor(
    outputStream: OutputStream,
    private val settings: PwgSettings = PwgSettings()
) : DataOutputStream(outputStream) {

    /** Write a document to this [outputStream]. */
    fun write(doc: RenderableDocument) {
        write("RaS2".toByteArray())
        doc.mapPages {
            it.mapIndexed { num, page ->
                val header = settings.buildHeader(doc, page, num)
                when (header.feedTransform to header.crossFeedTransform) {
                    -1 to -1 -> page.rotated()
                    1 to -1 -> page.flipX()
                    -1 to 1 -> page.flipY()
                    else -> page
                }
            }
        }.handleSides(settings)

        doc.forEachIndexed { num, page ->
            val header = settings.buildHeader(doc, page, num)
            header.write(this)
            writePageContent(page, header)
        }
    }

    private fun writePageContent(page: RenderablePage, header: PwgHeader) {
        // Pack and write the content bytes
        var yOffset = 0
        val bytesPerPixel = header.bitsPerPixel / PwgSettings.BITS_PER_BYTE
        val packer = PackBits(bytesPerPixel = bytesPerPixel, pixelsPerLine = header.bytesPerLine / bytesPerPixel)
        var size = 0
        var byteArray: ByteArray? = null
        while (yOffset < page.heightPixels) {
            val height = Math.min(MAX_SWATH_HEIGHT, page.heightPixels - yOffset)
            val renderSize = page.renderSize(height, settings.colorSpace)
            if (byteArray?.size != renderSize) {
                byteArray = ByteArray(renderSize)
            }
            page.render(yOffset, height, settings.colorSpace, byteArray)
            val encodedBytes = ByteArrayOutputStream()
            packer.encode(ByteArrayInputStream(byteArray), encodedBytes)
            write(encodedBytes.toByteArray())
            size += encodedBytes.size()
            yOffset += height
        }
    }

    companion object {
        // Pack and encode only this many lines at a time to conserve RAM
        const val MAX_SWATH_HEIGHT = 64
    }
}
