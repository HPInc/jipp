// © Copyright 2018 - 2021 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.pdl.pclm

import com.hp.jipp.model.PclmRasterBackSide
import com.hp.jipp.model.Sides
import com.hp.jipp.model.Types
import com.hp.jipp.pdl.ColorSpace
import com.hp.jipp.pdl.RenderableDocument
import com.hp.jipp.pdl.RenderablePage
import com.hp.jipp.pdl.isEven
import com.hp.jipp.pdl.isOdd
import java.io.ByteArrayOutputStream
import java.io.CharArrayWriter
import java.io.IOException
import java.io.OutputStream
import java.io.Writer
import java.util.zip.DeflaterOutputStream
import kotlin.math.roundToInt

/**
 * Writes a document using PCLm page description language as specified in "Wi-Fi Peer-to-Peer
 * Services Print Technical Specification v1.1".
 *
 * This format is a strict subset of PDF, so a PCLm file can be opened and inspected by ordinary
 * PDF viewers.
 *
 * IMPORTANT: This implementation does NOT yet support Duplex use cases.
 *
 * See also: https://wwwimages2.adobe.com/content/dam/acom/en/devnet/pdf/PDF32000_2008.pdf
 */
@Suppress("LargeClass", "TooManyFunctions") // Clearer for now to have it all in one place
class PclmWriter(
    /** Destination for encoded PCLM stream */
    private val outputStream: OutputStream,
    /** PCLM Settings to use when writing. */
    private val settings: PclmSettings
) : Writer() {
    /** Tracks the number of bytes written to the output stream so far */
    private var written: Int = 0

    /** Object number of each page object added to the stream */
    private val pageObjectNumbers = ArrayList<Int>()

    /** A list of offsets to each sequentially-numbered object in the stream */
    private val crossReferences = ArrayList<Int>()

    override fun flush() {
        outputStream.flush()
    }

    override fun close() {
        outputStream.close()
    }

    override fun write(chars: CharArray, offset: Int, length: Int) {
        written += length
        outputStream.write(String(chars).toByteArray(), offset, length)
    }

    /** Return the correct value for the [Types.jobPagesPerSet] value. */
    fun calculateJobPagesPerSet(document: RenderableDocument): Int {
        val pageCount = document.count()
        return if (settings.output.sides != Sides.oneSided && pageCount.isOdd) {
            // In this case alone PCLM will add a padding page between copies
            pageCount + 1
        } else pageCount
    }

    /** Write a complete document in PCLm format */
    fun write(document: RenderableDocument) {
        crossReferences.add(-1) // Placeholder so that objects begin with 1
        crossReferences.add(-1) // Placeholder for Catalog
        crossReferences.add(-1) // Placeholder for Page Tree
        startDoc()
        document.mapPages { doc ->
            doc.mapIndexed { pageNumber, page -> page.transform(pageNumber) }
        }.handleSides(settings.output, allowPadding = true).forEach { page ->
            writePage(document, page)
        }
        endDoc()
        flush()
    }

    private fun RenderablePage.transform(number: Int) =
        when {
            number.isEven -> this
            settings.output.sides == Sides.twoSidedLongEdge && settings.backSide == PclmRasterBackSide.rotated ->
                rotated()
            settings.output.sides == Sides.twoSidedLongEdge && settings.backSide == PclmRasterBackSide.flipped ->
                flipY()
            settings.output.sides == Sides.twoSidedShortEdge && settings.backSide == PclmRasterBackSide.flipped ->
                flipX()
            else -> this
        }

    /** Write the header to start a PCLM */
    private fun startDoc() {
        write("%PDF-1.7\n")
        write("%PCLm 1.0\n") // Spec shows PCLm-1.0 but this doesn't work
    }

    private data class Swath(val objectNumber: Int, val imageNumber: Int, val height: Int, val yOffset: Int)

    /** Write a rendered page */
    private fun writePage(doc: RenderableDocument, page: RenderablePage) {
        // Number of strips (rounding up so we get all strips including the last)
        val stripCount = Math.ceil(page.heightPixels / settings.stripHeight.toDouble()).roundToInt()

        // Find the top of the page (PDF puts y=0 at bottom, opposite of most computer images)
        var yOffset = 0

        // Build out swath definitions. Note: duplex will run backwards
        val swaths = (0 until stripCount).map { index ->
            val height = if (index == stripCount - 1) {
                page.heightPixels - (stripCount - 1) * settings.stripHeight
            } else settings.stripHeight

            // Predict the object number assuming page object, page content stream object, and interleaved transforms
            Swath(
                objectNumber = crossReferences.size + 2 + (index * 2),
                imageNumber = index,
                height = height,
                yOffset = yOffset.also { yOffset += height }
            )
        }

        // Write the page object
        val pageObjectNumber = pdObject {
            write("/Type /Page\n")
            write("/Parent $PAGE_TREE_OBJECT_NUMBER 0 R\n")
            write("/Resources <<\n")
            write("/XObject <<\n")
            for (swath in swaths) {
                write("/Image${swath.imageNumber} ${swath.objectNumber} 0 R\n")
            }
            write(">>\n")
            write(">>\n")

            // Note: For landscape we would use a different media box
            // MediaBox MUST be integral for some printers
            val heightPoints: Int = page.heightPixels * POINTS_PER_INCH / doc.dpi
            val widthPoints: Int = page.widthPixels * POINTS_PER_INCH / doc.dpi
            write("/MediaBox [ 0 0 $widthPoints $heightPoints ]\n")
            write("/Contents [ ${objectNumber + 1} 0 R ]\n")
        }

        pageObjectNumbers += pageObjectNumber

        // Write the page content stream object
        pdObject {
            val contentStream = CharArrayWriter()
            contentStream.write(
                "${POINTS_PER_INCH.toDouble() / doc.dpi} 0 0 " +
                    "${POINTS_PER_INCH.toDouble() / doc.dpi} 0 0 cm\n"
            )
            contentStream.write("/P <</MCID 0>> BDC q\n")

            for (swath in swaths) {
                // Transfer swath image offset back to page coordinates
                val pageYOffset = page.heightPixels - swath.yOffset - swath.height
                contentStream.write("${page.widthPixels} 0 0 ${swath.height} 0 $pageYOffset cm\n")
                contentStream.write("/Image${swath.imageNumber} Do Q\n")
                contentStream.write("/P <</MCID 0>> BDC q\n")
            }

            charStream = contentStream.toCharArray()

            write("/Length ${contentStream.size()}\n")
        }
        writeSwaths(page, swaths, page.widthPixels)
    }

    private fun writeSwaths(page: RenderablePage, swaths: List<Swath>, widthPixels: Int) {
        var first = true
        for (swath in swaths) {
            var rawBytes: ByteArray? = null
            pdObject {
                val curBytes = getBytes(page, swath, first, widthPixels, rawBytes)
                rawBytes = curBytes // Maintain for possible re-use
                byteStream = if (curBytes.isNotEmpty()) {
                    // Note: Consider jpg instead of flate, but switching between the two could look weird.
                    val encoded = ByteArrayOutputStream()
                    DeflaterOutputStream(encoded).use { it.write(rawBytes) }
                    encoded.toByteArray()
                } else {
                    rawBytes
                }

                write("/Width $widthPixels\n")
                when (settings.output.colorSpace) {
                    ColorSpace.Rgb -> write("/ColorSpace /DeviceRGB\n")
                    ColorSpace.Grayscale -> write("/ColorSpace /DeviceGray\n")
                    else -> throw IOException("${settings.output.colorSpace} not supported")
                }

                write("/Height ${swath.height}\n")
                write("/Filter /FlateDecode\n")
                write("/Subtype /Image\n")
                write("/Length ${byteStream!!.size}\n")
                write("/Type /XObject\n")
                write("/BitsPerComponent 8\n")
                if (curBytes.isEmpty() && !first) {
                    write("/Name /WhiteStrip\n")
                } else {
                    write("/Name /ColorStrip\n")
                }
            }

            // Note: Transform first when duplex (not yet implemented)
            pdObject {
                byteStream = "q /Image Do Q\n".toByteArray()
                write("/Length ${byteStream!!.size}\n")
            }

            flush()
            first = false
        }
    }

    /** Obtain bytes from a renderable page */
    private fun getBytes(
        page: RenderablePage,
        swath: Swath,
        forceNonBlank: Boolean,
        widthPixels: Int,
        bytes: ByteArray?
    ): ByteArray {
        var renderBytes = bytes
        val size = swath.height * widthPixels * settings.output.colorSpace.bytesPerPixel
        if (renderBytes?.size != size) {
            renderBytes = ByteArray(size)
        }

        page.render(
            yOffset = swath.yOffset, swathHeight = swath.height, colorSpace = settings.output.colorSpace,
            byteArray = renderBytes
        )

        if (forceNonBlank) return renderBytes

        // Check to see if blank
        val blank: Byte = BLANK
        return if (renderBytes.firstOrNull { it != blank } == null) {
            // All bytes are blank so return an empty array
            ByteArray(0)
        } else {
            // At least one non-blank byte so return normal array
            renderBytes
        }
    }

    /** Write the components necessary to end the PCLM file */
    private fun endDoc() {
        // Write the Catalog
        pdObject(CATALOG_OBJECT_NUMBER) {
            write("/Type /Catalog\n")
            // The page tree is next
            write("/Pages ${objectNumber + 1} 0 R\n")
        }

        // Write the Page Tree
        pdObject(PAGE_TREE_OBJECT_NUMBER) {
            write("/Count ${pageObjectNumbers.size}\n")
            write("/Kids [ ")
            for (i in 0 until pageObjectNumbers.size) {
                write("${pageObjectNumbers[i]} 0 R ")
            }
            write("]\n")
            write("/Type /Pages\n")
        }

        // Write the cross-references (skip the first placeholder)
        val xrefOffset = written
        write("xref\n")
        write("1 ${crossReferences.size - 1}\n")
        for (offset in crossReferences.drop(1)) {
            write(String.format("%010d 00000 n \n", offset))
        }

        // Write the trailer
        write("trailer\n")
        write("<<\n")
        write("/Size ${crossReferences.size - 1}\n")
        write("/Root $CATALOG_OBJECT_NUMBER 0 R\n")
        write(">>\n")
        write("startxref\n")
        write("$xrefOffset\n")
        write("%%EOF\n")
    }

    /** Create an object in the output stream, returning its object number */
    private fun pdObject(objectNumber: Int = crossReferences.size, block: InObject.() -> Unit): Int {
        val context = InObject(objectNumber)
        when {
            objectNumber < crossReferences.size -> crossReferences[objectNumber] = written
            objectNumber == crossReferences.size -> crossReferences += written
            else -> throw IOException("Cannot create object beyond last position")
        }

        write("${context.objectNumber} 0 obj\n")
        write("<<\n")
        context.block()
        write(">>\n")
        if (context.byteStream != null) {
            write("stream\n")
            flush()
            outputStream.write(context.byteStream!!)
            written += context.byteStream!!.size
            write("\nendstream\n")
        } else if (context.charStream != null) {
            write("stream\n")
            flush()
            write(context.charStream!!)
            write("\nendstream\n")
        }
        write("endobj\n")
        return objectNumber
    }

    private class InObject(val objectNumber: Int) {
        /** An array of bytes to include with this object as a stream */
        var byteStream: ByteArray? = null
        /** An array of chars to encode UTF-8 and include with this object as a stream */
        var charStream: CharArray? = null
    }

    private companion object {
        private const val BLANK = 0xFF.toByte()
        private const val POINTS_PER_INCH: Int = 72
        private const val CATALOG_OBJECT_NUMBER = 1
        private const val PAGE_TREE_OBJECT_NUMBER = CATALOG_OBJECT_NUMBER + 1
    }
}
