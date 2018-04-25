package com.hp.jipp.doc.pclm

import com.hp.jipp.doc.ColorSpace
import com.hp.jipp.doc.RenderableDocument
import com.hp.jipp.doc.RenderablePage
import java.io.ByteArrayOutputStream
import java.io.CharArrayWriter
import java.io.IOException
import java.io.OutputStream
import java.io.Writer
import java.util.zip.DeflaterOutputStream
import sun.nio.cs.StreamEncoder
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
@Suppress("LargeClass") // Clearer for now to have it all in one place
class PclmWriter(
    /** Destination for encoded PCLM stream */
    private val outputStream: OutputStream,
    /** Capabilities of target device */
    private val caps: PclmCapabilities,
    /** Resolution of rendered content */
    private val dpi: Int
) : Writer() {
    private val encoder = StreamEncoder.forOutputStreamWriter(outputStream, this, null as String?)

    /** Tracks the number of bytes written to the output stream so far */
    private var written: Int = 0

    /** Object number of each page object added to the stream */
    private val pageObjectNumbers = ArrayList<Int>()

    /** A list of offsets to each sequentially-numbered object in the stream */
    private val crossReferences = ArrayList<Int>()

    override fun flush() {
        encoder.flush()
        outputStream.flush()
    }

    override fun close() {
        encoder.close()
        outputStream.close()
    }

    override fun write(chars: CharArray?, offset: Int, length: Int) {
        written += length
        encoder.write(chars, offset, length)
    }

    /** Write a complete document in PCLm format */
    fun write(document: RenderableDocument) {
        crossReferences.add(-1) // Placeholder so that objects begin with 1
        crossReferences.add(-1) // Placeholder for Catalog
        crossReferences.add(-1) // Placeholder for Page Tree
        startDoc()

        // Print last page first so that the user doesn't have to shuffle. Printer should request this?
        for (page in document.reversed()) writePage(page)

        endDoc()
        flush()
    }

    /** Write the header to start a PCLM */
    private fun startDoc() {
        write("%PDF-1.7\n")
        write("%PCLm 1.0\n") // Spec shows PCLm-1.0 but this doesn't work
    }

    private data class Swath(val objectNumber: Int, val imageNumber: Int, val height: Int, val yOffset: Int)

    /** Write a rendered page */
    private fun writePage(page: RenderablePage) {
        val heightPixels = RenderablePage.pointsToPixels(dpi = dpi, points = page.heightPoints)
        val widthPixels = RenderablePage.pointsToPixels(dpi = dpi, points = page.widthPoints)

        // Number of strips (rounding up so we get all strips including the last)
        val stripCount = Math.ceil(heightPixels / caps.stripHeight.toDouble()).roundToInt()

        // Find the top of the page (PDF puts y=0 at bottom, opposite of most computer images)
        var yOffset = 0

        // Build out swath definitions. Note: duplex will run backwards
        val swaths = (0 until stripCount).map { index ->
            val height = if (index == stripCount - 1) {
                heightPixels - (stripCount - 1) * caps.stripHeight
            } else caps.stripHeight

            // Predict the object number assuming page object, page content stream object, and interleaved transforms
            Swath(objectNumber = crossReferences.size + 2 + (index * 2),
                    imageNumber = index,
                    height = height,
                    yOffset = yOffset.also { yOffset += height })
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
            write("/MediaBox [ 0 0 ${page.widthPoints.toInt()} ${page.heightPoints.toInt()} ]\n")
            write("/Contents [ ${objectNumber + 1} 0 R ]\n")
        }

        pageObjectNumbers += pageObjectNumber

        // Write the page content stream object
        pdObject {
            val contentStream = CharArrayWriter()
            contentStream.write("${POINTS_PER_INCH / dpi} 0 0 ${POINTS_PER_INCH / dpi} 0 0 cm\n")
            contentStream.write("/P <</MCID 0>> BDC q\n")

            for (swath in swaths) {
                // Transfer swath image offset back to page coordinates
                val pageYOffset = heightPixels - swath.yOffset - swath.height
                contentStream.write("$widthPixels 0 0 ${swath.height} 0 $pageYOffset cm\n")
                contentStream.write("/Image${swath.imageNumber} Do Q\n")
                contentStream.write("/P <</MCID 0>> BDC q\n")
            }

            charStream = contentStream.toCharArray()

            write("/Length ${contentStream.size()}\n")
        }
        writeSwaths(page, swaths, widthPixels)
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
                if (caps.color) {
                    write("/ColorSpace /DeviceRGB\n")
                } else {
                    write("/ColorSpace /DeviceGray\n")
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
        val colorSpace = if (caps.color) ColorSpace.RGB else ColorSpace.GRAYSCALE
        val size = swath.height * widthPixels * colorSpace.bytesPerPixel
        if (renderBytes?.size != size) {
            renderBytes = ByteArray(size)
        }

        page.render(dpi = dpi, yOffset = swath.yOffset, swathHeight = swath.height,
            colorSpace = colorSpace, byteArray = renderBytes)

        if (forceNonBlank) return renderBytes

        // Check to see if blank
        val blank: Byte = 0xFF.toByte()
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
        private const val POINTS_PER_INCH = 72f
        private const val CATALOG_OBJECT_NUMBER = 1
        private const val PAGE_TREE_OBJECT_NUMBER = CATALOG_OBJECT_NUMBER + 1
    }
}
