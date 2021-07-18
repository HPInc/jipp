package util

import com.hp.jipp.pdl.ColorSpace
import com.hp.jipp.pdl.RenderablePage
import java.io.ByteArrayOutputStream
import kotlin.math.min

object PageUtil {
    const val WHITE_BYTE = 0xFF.toByte()
    const val BLACK_BYTE = 0x00.toByte()
    val RED = byteArrayOf(WHITE_BYTE, BLACK_BYTE, BLACK_BYTE)
    val GREEN = byteArrayOf(BLACK_BYTE, WHITE_BYTE, BLACK_BYTE)
    val BLUE = byteArrayOf(BLACK_BYTE, BLACK_BYTE, WHITE_BYTE)
    private val colorToText = mapOf(
        listOf(true, true, true) to ".",
        listOf(true, true, false) to 'Y',
        listOf(true, false, true) to 'P',
        listOf(true, false, false) to 'R',
        listOf(false, true, true) to 'C',
        listOf(false, true, false) to 'G',
        listOf(false, false, true) to 'B',
        listOf(false, false, false) to 'K'
    )
    private const val MAX_STRING_DIMENSION = 80

    fun describe(
        page: RenderablePage,
        colorSpace: ColorSpace = ColorSpace.Rgb
    ): String {
        val swathHeight: Int = min(MAX_STRING_DIMENSION, page.heightPixels)
        val bytes = ByteArray(page.renderSize(swathHeight, colorSpace))
        page.render(0, swathHeight, colorSpace, bytes)
        val buffer = StringBuffer()
        for (y in 0 until min(MAX_STRING_DIMENSION, page.heightPixels)) {
            for (x in 0 until min(MAX_STRING_DIMENSION, page.widthPixels)) {
                val offset = (y * page.widthPixels + x) * colorSpace.bytesPerPixel
                val pixel = bytes.sliceArray(offset until (offset + colorSpace.bytesPerPixel))
                buffer.append(
                    when (colorSpace) {
                        ColorSpace.Grayscale -> if (pixel[0] == WHITE_BYTE) "." else "K"
                        ColorSpace.Rgb ->
                            colorToText[
                                listOf(
                                    pixel[0] == WHITE_BYTE, pixel[1] == WHITE_BYTE,
                                    pixel[2] == WHITE_BYTE
                                )
                            ]
                        ColorSpace.Rgba ->
                            colorToText[
                                listOf(
                                    pixel[1] == WHITE_BYTE, pixel[2] == WHITE_BYTE,
                                    pixel[3] == WHITE_BYTE
                                )
                            ]
                    }
                )
            }
            buffer.append("\n")
        }
        return buffer.toString()
    }

    /**
     * Return a page that looks like  "\", with num in each color for each pixel drawn in a 45 degree line, and
     * all other pixels perfectly white (0xFF).
     */
    fun fakePage(pixel: ByteArray, pixelColorSpace: ColorSpace, width: Int = 15, height: Int = 19): RenderablePage {
        return object : RenderablePage(width, height) {
            override fun render(yOffset: Int, swathHeight: Int, colorSpace: ColorSpace, byteArray: ByteArray) {
                val outputStream = ByteArrayOutputStream()
                pixelColorSpace.converter(colorSpace).invoke(pixel, 0, outputStream)
                byteArray.fill(WHITE_BYTE)
                for (i in 0 until swathHeight) {
                    val x = yOffset + i
                    if (x < widthPixels) {
                        val pos = (x + (yOffset + x * widthPixels)) * colorSpace.bytesPerPixel
                        outputStream.toByteArray().copyInto(byteArray, pos)
                    }
                }
            }
        }
    }
}
