// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.pdl.pwg

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * All elements of a PWG Raster header as described in section 4.3 of
 * [PWG-5102.4](https://ftp.pwg.org/pub/pwg/candidates/cs-ippraster10-20120420-5102.4.pdf).
 */
data class PwgHeader(
    val mediaColor: String = "",
    val mediaType: String = "",
    val printContentOptimize: String = "",
    val cutMedia: When = When.Never,
    /** True if printing two-sided. */
    val duplex: Boolean = false,
    val hwResolutionX: Int,
    val hwResolutionY: Int,
    val insertSheet: Boolean = false,
    val jog: When = When.Never,
    val leadingEdge: Edge = Edge.ShortEdgeFirst,
    val mediaPosition: MediaPosition = MediaPosition.Auto,
    val mediaWeightMetric: Int = 0,
    val numCopies: Int = 0,
    val orientation: Orientation = Orientation.Portrait,
    /** Media width in points. */
    val pageSizeX: Int = 0,
    /** Media height in points. */
    val pageSizeY: Int = 0,
    /** True if two-sided printing should be flipped along the short-edge. */
    val tumble: Boolean = false,
    /** Full-bleed page width in pixels. */
    val width: Int,
    /** Full-bleed page height in pixels. */
    val height: Int,
    val bitsPerColor: Int,
    val bitsPerPixel: Int,
    val colorOrder: ColorOrder = ColorOrder.Chunky,
    val colorSpace: ColorSpace,
    val numColors: Int,
    val totalPageCount: Int = 0,
    val crossFeedTransform: Int = 1,
    val feedTransform: Int = 1,
    /** Left position of non-blank area in pixels, if image box is known. */
    val imageBoxLeft: Int = 0,
    /** Top position of non-blank area in pixels, if image box is known. */
    val imageBoxTop: Int = 0,
    /** Right position of non-blank area in pixels, if image box is known. */
    val imageBoxRight: Int = 0,
    /** Bottom position of non-blank area in pixels, if image box is known. */
    val imageBoxBottom: Int = 0,
    /** An sRGB color field containing 24 bits of color data. Default: WHITE */
    val alternatePrimary: Int = WHITE,
    val printQuality: PrintQuality = PrintQuality.Default,
    /** USB vendor identification number or 0. */
    val vendorIdentifier: Int = 0,
    /** Octets containing 0-1088 bytes of vendor-specific data. */
    val vendorData: ByteArray = byteArrayOf(),
    val renderingIntent: String = "",
    val pageSizeName: String = ""
) {
    /** Number of bytes per line, always based on [width] and [bitsPerPixel]. */
    val bytesPerLine: Int = ((bitsPerPixel * width + 7) / 8)

    init {
        if (vendorData.size > MAX_VENDOR_DATA_SIZE) {
            throw IllegalArgumentException("vendorData.size of ${vendorData.size} must not be > $MAX_VENDOR_DATA_SIZE")
        }
    }

    /** Something that has an integer value. */
    interface HasValue {
        val value: Int
    }

    /** Converts from an integer value to a T. */
    interface ValueConverter<T : HasValue> {
        fun from(value: Int): T
    }

    /** Points during print when another operation should take place. */
    enum class When(override val value: Int) : HasValue {
        Never(0), AfterDocument(1), AfterJob(2), AfterSet(3), AfterPage(4);

        companion object : ValueConverter<When> {
            override fun from(value: Int) = When.values().firstOrNull { it.value == value } ?: Never
        }
    }

    /** Kinds of duplexing. */
    enum class Edge(override val value: Int) : HasValue {
        ShortEdgeFirst(0), LongEdgeFirst(1);

        companion object : ValueConverter<Edge> {
            override fun from(value: Int) = Edge.values().firstOrNull { it.value == value } ?: ShortEdgeFirst
        }
    }

    /** Output orientation of a page. */
    enum class Orientation(override val value: Int) : HasValue {
        Portrait(0), Landscape(1), ReversePortrait(2), ReverseLandscape(3);

        companion object : ValueConverter<Orientation> {
            override fun from(value: Int) = Orientation.values().firstOrNull { it.value == value } ?: Portrait
        }
    }

    enum class ColorOrder(override val value: Int) : HasValue {
        Chunky(0);

        companion object : ValueConverter<ColorOrder> {
            override fun from(value: Int) = ColorOrder.values().firstOrNull { it.value == value } ?: Chunky
        }
    }

    /** Meaning of color values provided for each pixel. */
    enum class ColorSpace(override val value: Int) : HasValue {
        Rgb(1), Black(2), Cmyk(6), Sgray(18), Srgb(19), AdobeRgb(20), Device1(48), Device2(49), Device3(50),
        Device4(51), Device5(52), Device6(53), Device7(54), Device8(55), Device9(56), Device10(57), Device11(58),
        Device12(59), Device13(60), Device14(61), Device15(62);

        companion object : ValueConverter<ColorSpace> {
            override fun from(value: Int) = ColorSpace.values().firstOrNull { it.value == value } ?: ColorSpace.Srgb
        }
    }

    /** Media input source. */
    enum class MediaPosition(override val value: Int) : HasValue {
        Auto(0), Main(1), Alternate(2), LargeCapacity(3), Manual(4), Envelope(5), Disc(6), Photo(7), Hagaki(8),
        MainRoll(9), AlternateRoll(10), Top(11), Middle(12), Bottom(13), Side(14), Left(15), Right(16), Center(17),
        Rear(18), ByPassTray(19), Tray1(20), Tray2(21), Tray3(22), Tray4(23), Tray5(24), Tray6(25), Tray7(26),
        Tray8(27), Tray9(28), Tray10(29), Tray11(30), Tray12(31), Tray13(32), Tray14(33), Tray15(34), Tray16(35),
        Tray17(36), Tray18(37), Tray19(38), Tray20(39), Roll1(40), Roll2(41), Roll3(42), Roll4(43), Roll5(44),
        Roll6(45), Roll7(46), Roll8(47), Roll9(48), Roll10(49);

        companion object : ValueConverter<MediaPosition> {
            override fun from(value: Int) = MediaPosition.values().firstOrNull { it.value == value } ?: Auto
        }
    }

    /** Requested output quality. */
    enum class PrintQuality(override val value: Int) : HasValue {
        Default(0), Draft(3), Normal(4), High(5);

        companion object : ValueConverter<PrintQuality> {
            override fun from(value: Int) = PrintQuality.values().firstOrNull { it.value == value } ?: Default
        }
    }

    /** Writes a PWG header containing exactly 1796 octets. */
    fun write(output: OutputStream) {
        ((output as? DataOutputStream) ?: DataOutputStream(output)).apply {
            writeCString(PWG_RASTER_NAME) // Always the same
            writeCString(mediaColor)
            writeCString(mediaType)
            writeCString(printContentOptimize)
            writeReserved(12)
            writeInt(cutMedia)
            writeInt(duplex)
            writeInt(hwResolutionX)
            writeInt(hwResolutionY)
            writeReserved(16)
            writeInt(insertSheet)
            writeInt(jog)
            writeInt(leadingEdge)
            writeReserved(12)
            writeInt(mediaPosition)
            writeInt(mediaWeightMetric)
            writeReserved(8)
            writeInt(numCopies)
            writeInt(orientation)
            writeReserved(4)
            writeInt(pageSizeX)
            writeInt(pageSizeY)
            writeReserved(8)
            writeInt(tumble)
            writeInt(width)
            writeInt(height)
            writeReserved(4)
            writeInt(bitsPerColor)
            writeInt(bitsPerPixel)
            writeInt(bytesPerLine)
            writeInt(colorOrder)
            writeInt(colorSpace)
            writeReserved(16)
            writeInt(numColors)
            writeReserved(28)
            writeInt(totalPageCount)
            writeInt(crossFeedTransform)
            writeInt(feedTransform)
            writeInt(imageBoxLeft)
            writeInt(imageBoxTop)
            writeInt(imageBoxRight)
            writeInt(imageBoxBottom)
            writeInt(alternatePrimary)
            writeInt(printQuality)
            writeReserved(20)
            writeInt(vendorIdentifier)
            writeInt(vendorData.size)
            write(vendorData, 0, vendorData.size)
            // Pad with 0
            writeReserved(MAX_VENDOR_DATA_SIZE - vendorData.size)
            writeReserved(64)
            writeCString(renderingIntent)
            writeCString(pageSizeName)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PwgHeader

        if (mediaColor != other.mediaColor) return false
        if (mediaType != other.mediaType) return false
        if (printContentOptimize != other.printContentOptimize) return false
        if (cutMedia != other.cutMedia) return false
        if (duplex != other.duplex) return false
        if (hwResolutionX != other.hwResolutionX) return false
        if (hwResolutionY != other.hwResolutionY) return false
        if (insertSheet != other.insertSheet) return false
        if (jog != other.jog) return false
        if (leadingEdge != other.leadingEdge) return false
        if (mediaPosition != other.mediaPosition) return false
        if (mediaWeightMetric != other.mediaWeightMetric) return false
        if (numCopies != other.numCopies) return false
        if (orientation != other.orientation) return false
        if (pageSizeX != other.pageSizeX) return false
        if (pageSizeY != other.pageSizeY) return false
        if (tumble != other.tumble) return false
        if (width != other.width) return false
        if (height != other.height) return false
        if (bitsPerColor != other.bitsPerColor) return false
        if (bitsPerPixel != other.bitsPerPixel) return false
        if (colorOrder != other.colorOrder) return false
        if (colorSpace != other.colorSpace) return false
        if (numColors != other.numColors) return false
        if (totalPageCount != other.totalPageCount) return false
        if (crossFeedTransform != other.crossFeedTransform) return false
        if (feedTransform != other.feedTransform) return false
        if (imageBoxLeft != other.imageBoxLeft) return false
        if (imageBoxTop != other.imageBoxTop) return false
        if (imageBoxRight != other.imageBoxRight) return false
        if (imageBoxBottom != other.imageBoxBottom) return false
        if (alternatePrimary != other.alternatePrimary) return false
        if (printQuality != other.printQuality) return false
        if (vendorIdentifier != other.vendorIdentifier) return false
        if (!vendorData.contentEquals(other.vendorData)) return false
        if (renderingIntent != other.renderingIntent) return false
        if (pageSizeName != other.pageSizeName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = mediaColor.hashCode()
        result = 31 * result + mediaType.hashCode()
        result = 31 * result + printContentOptimize.hashCode()
        result = 31 * result + cutMedia.hashCode()
        result = 31 * result + duplex.hashCode()
        result = 31 * result + hwResolutionX
        result = 31 * result + hwResolutionY
        result = 31 * result + insertSheet.hashCode()
        result = 31 * result + jog.hashCode()
        result = 31 * result + leadingEdge.hashCode()
        result = 31 * result + mediaPosition.hashCode()
        result = 31 * result + mediaWeightMetric
        result = 31 * result + numCopies
        result = 31 * result + orientation.hashCode()
        result = 31 * result + pageSizeX
        result = 31 * result + pageSizeY
        result = 31 * result + tumble.hashCode()
        result = 31 * result + width
        result = 31 * result + height
        result = 31 * result + bitsPerColor
        result = 31 * result + bitsPerPixel
        result = 31 * result + colorOrder.hashCode()
        result = 31 * result + colorSpace.hashCode()
        result = 31 * result + numColors
        result = 31 * result + totalPageCount
        result = 31 * result + crossFeedTransform
        result = 31 * result + feedTransform
        result = 31 * result + imageBoxLeft
        result = 31 * result + imageBoxTop
        result = 31 * result + imageBoxRight
        result = 31 * result + imageBoxBottom
        result = 31 * result + alternatePrimary
        result = 31 * result + printQuality.hashCode()
        result = 31 * result + vendorIdentifier
        result = 31 * result + vendorData.contentHashCode()
        result = 31 * result + renderingIntent.hashCode()
        result = 31 * result + pageSizeName.hashCode()
        return result
    }

    companion object {
        const val PWG_RASTER_NAME = "PwgRaster"
        const val MAX_VENDOR_DATA_SIZE = 1088
        const val HEADER_SIZE = 1796
        const val WHITE = 0xFFFFFF

        private const val CSTRING_LENGTH = 64

        /**
         * Read the input stream to construct a PwgHeader.
         *
         * The input stream MUST contain 1796 octets. It MAY contain invalid enums (which will be silently converted
         * to default values).
         */
        fun read(input: InputStream): PwgHeader =
            ((input as? DataInputStream) ?: DataInputStream(input)).run {
                // Discard the initial PwgRaster string
                readCString()

                // Read everything parameter-by-parameter
                PwgHeader(
                    mediaColor = readCString(),
                    mediaType = readCString(),
                    printContentOptimize = readCString().also { skip(12) },
                    cutMedia = readValue(When),
                    duplex = readInt().toBoolean(),
                    hwResolutionX = readInt(),
                    hwResolutionY = readInt().also { skip(16) },
                    insertSheet = readInt().toBoolean(),
                    jog = readValue(When),
                    leadingEdge = readValue(Edge).also { skip(12) },
                    mediaPosition = readValue(MediaPosition),
                    mediaWeightMetric = readInt().also { skip(8) },
                    numCopies = readInt(),
                    orientation = readValue(Orientation).also { skip(4) },
                    pageSizeX = readInt(),
                    pageSizeY = readInt().also { skip(8) },
                    tumble = readInt().toBoolean(),
                    width = readInt(),
                    height = readInt().also { skip(4) },
                    bitsPerColor = readInt(),
                    bitsPerPixel = readInt().also { skip(4) }, // Since bytesPerLine is calculated
                    colorOrder = readValue(ColorOrder),
                    colorSpace = readValue(ColorSpace).also { skip(16) },
                    numColors = readInt().also { skip(28) },
                    totalPageCount = readInt(),
                    crossFeedTransform = readInt(),
                    feedTransform = readInt(),
                    imageBoxLeft = readInt(),
                    imageBoxTop = readInt(),
                    imageBoxRight = readInt(),
                    imageBoxBottom = readInt(),
                    alternatePrimary = readInt(),
                    printQuality = readValue(PrintQuality).also { skip(20) },
                    vendorIdentifier = readInt(),
                    vendorData = ByteArray(readInt()).also {
                        read(it)
                        skip(64L + (1088 - it.size))
                    },
                    renderingIntent = readCString(),
                    pageSizeName = readCString())
            }

        /**
         * Write 0-bytes into the output string, [bytes] long.
         */
        private fun DataOutputStream.writeReserved(bytes: Int) {
            write(ByteArray(bytes))
        }

        /**
         * Write the specified string, up to [width] bytes, zero-padded to exactly [width].
         */
        private fun DataOutputStream.writeCString(string: String) {
            val bytes = string.toByteArray()
            write(bytes, 0, Math.min(CSTRING_LENGTH, bytes.size))
            writeReserved(CSTRING_LENGTH - bytes.size)
        }

        /**
         * Write an enum value object.
         */
        private fun DataOutputStream.writeInt(hasValue: HasValue) {
            writeInt(hasValue.value)
        }

        /**
         * Read a zero-padded fixed-width string
         */
        private fun DataInputStream.readCString() =
            ByteArray(CSTRING_LENGTH).let {
                read(it)
                String(it).split('\u0000')[0]
            }

        /**
         * Convert back to an enum value object.
         */
        private fun <T : HasValue> DataInputStream.readValue(converter: ValueConverter<T>): T =
            converter.from(readInt())

        /**
         * A regular writeBoolean only writes one byte. But PWG is very thorough and uses four bytes to encode a
         * single true/false bit.
         */
        private fun DataOutputStream.writeInt(value: Boolean) {
            writeInt(if (value) 1 else 0)
        }

        private fun Int.toBoolean() =
            this != 0
    }
}
