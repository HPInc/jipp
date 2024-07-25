// Copyright 2018 - 2021 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.pdl.pwg

import com.hp.jipp.pdl.RenderableDocument
import com.hp.jipp.pdl.RenderablePage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.OutputStream
import kotlin.math.min

/**
 * An [OutputStream] that can write a [RenderableDocument] in PWG-Raster format.
 */
class PwgWriter
@JvmOverloads constructor(
    outputStream: OutputStream,
    private val settings: PwgSettings = PwgSettings(),
    /** Supply a converter here if you would like to return a customized header. */
    val headerCustomizer: (RenderablePage, PwgHeader) -> PwgHeader = { _, header -> header }
) : DataOutputStream(outputStream) {

    /** Write a document to this [outputStream]. */
    fun write(doc: RenderableDocument) {
        write(MAGIC_NUMBER)
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
        }.handleSides(settings.output, settings.allowPadding).forEachIndexed { num, page ->
            val header = headerCustomizer(page, settings.buildHeader(doc, page, num))
            header.write(this)
            writePageContent(page, header)
        }
    }

    private fun writePageContent(page: RenderablePage, header: PwgHeader) {
        // Pack and write the content bytes
        var yOffset = 0
        var size = 0
        var byteArray: ByteArray? = null
        while (yOffset < page.heightPixels) {
            val height = min(MAX_SWATH_HEIGHT, page.heightPixels - yOffset)
            val renderSize = page.renderSize(height, settings.output.colorSpace)
            if (byteArray?.size != renderSize) {
                byteArray = ByteArray(renderSize)
            }
            page.render(yOffset, height, settings.output.colorSpace, byteArray)
            val encodedBytes = ByteArrayOutputStream()
            header.packBits.encode(ByteArrayInputStream(byteArray), encodedBytes)
            write(encodedBytes.toByteArray())
            size += encodedBytes.size()
            yOffset += height
        }
    }

    companion object {
        val MAGIC_NUMBER = "RaS2".toByteArray()

        // Pack and encode only this many lines at a time to conserve RAM
        const val MAX_SWATH_HEIGHT = 64
    }
}
