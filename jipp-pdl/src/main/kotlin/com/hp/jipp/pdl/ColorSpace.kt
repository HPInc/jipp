// Copyright 2018 - 2021 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.pdl

import java.io.InputStream
import java.io.OutputStream
import kotlin.math.roundToInt

/** Identifies a color space which describes how each pixel of image data is encoded. */
@Suppress("MagicNumber")
enum class ColorSpace(val bytesPerPixel: Int) {
    /** Four bytes per pixel: Red, Green, Blue, Alpha. */
    Rgba(4),

    /** Three bytes per pixel: Red, Green, Blue. */
    Rgb(3),

    /** One byte per pixel, between 0x00=Black and 0xFF=White. */
    Grayscale(1);

    /** Return a converter lambda that will copy bytes from this color space to another. */
    fun converter(outputColor: ColorSpace): (ByteArray, Int, OutputStream) -> Unit =
        when (this) {
            Grayscale -> grayscaleConverter(outputColor)
            Rgb -> rgbConverter(outputColor)
            Rgba -> rgbaConverter(outputColor)
        }

    /** Return a function that converts a [Grayscale] input array to an [outputColor] output stream. */
    private fun grayscaleConverter(outputColor: ColorSpace): (ByteArray, Int, OutputStream) -> Unit =
        when (outputColor) {
            Grayscale -> passThrough
            Rgb -> { input, inputOffset, output ->
                val byte = input[inputOffset].toInt()
                output.write(byte)
                output.write(byte)
                output.write(byte)
            }
            Rgba -> { input, inputOffset, output ->
                val byte = input[inputOffset].toInt()
                output.write(byte)
                output.write(byte)
                output.write(byte)
                output.write(MAX_BYTE)
            }
        }

    /** Return a function that converts an [Rgb] input array to an [outputColor] output stream. */
    private fun rgbConverter(outputColor: ColorSpace): (ByteArray, Int, OutputStream) -> Unit =
        when (outputColor) {
            Grayscale -> { input, inputOffset, output ->
                output.write(input.rgbLuminosityAt(inputOffset).roundToInt())
            }
            Rgb -> passThrough
            Rgba -> { input, inputOffset, output ->
                output.write(input[inputOffset].toInt())
                output.write(input[inputOffset + 1].toInt())
                output.write(input[inputOffset + 2].toInt())
                output.write(MAX_BYTE)
            }
        }

    /** Return a function that converts an [Rgba] input array to an [outputColor] output stream. */
    private fun rgbaConverter(outputColor: ColorSpace): (ByteArray, Int, OutputStream) -> Unit =
        when (outputColor) {
            Grayscale -> { input, inputOffset, output ->
                output.write(
                    input.rgbLuminosityAt(inputOffset)
                        .applyAlpha(input[inputOffset + 3].uByteInt)
                )
            }
            Rgb -> { input, inputOffset, output ->
                when (val alpha = input[inputOffset + 3].uByteInt) {
                    0x00 -> {
                        output.write(MAX_BYTE)
                        output.write(MAX_BYTE)
                        output.write(MAX_BYTE)
                    }
                    0xFF -> output.write(input, inputOffset, 3)
                    else -> {
                        output.write(input[inputOffset].uByteInt.toDouble().applyAlpha(alpha))
                        output.write(input[inputOffset + 1].uByteInt.toDouble().applyAlpha(alpha))
                        output.write(input[inputOffset + 2].uByteInt.toDouble().applyAlpha(alpha))
                    }
                }
            }
            Rgba -> passThrough
        }

    /** A converter function that passes data through unmodified. */
    private val passThrough = { input: ByteArray, inputOffset: Int, output: OutputStream ->
        output.write(input, inputOffset, bytesPerPixel)
    }

    /**
     * Find the correct point between this intensity value (0x00-0xFF) and white based on alpha level (0x00-0xFF)
     * where alpha 0x00 is white and alpha 0xFF is the original intensity.
     */
    private fun Double.applyAlpha(alpha: Int): Int =
        (this * alpha / MAX_BYTE - alpha + MAX_BYTE).roundToInt()

    /** Return this unsigned byte as an integer. */
    private val Byte.uByteInt get() = toInt().and(MAX_BYTE)

    /** Measure the luminosity at the specified index. */
    private fun ByteArray.rgbLuminosityAt(index: Int) =
        LUM_R * get(index).uByteInt +
            LUM_G * get(index + 1).uByteInt +
            LUM_B * get(index + 2).uByteInt

    /** Write [input] pixels encoded in this [ColorSpace] to [output] in [outputColor]. */
    fun convert(input: InputStream, output: OutputStream, outputColor: ColorSpace) {
        val inputPixel = ByteArray(bytesPerPixel)
        val converter = converter(outputColor)
        while (input.read(inputPixel) != -1) {
            converter(inputPixel, 0, output)
        }
    }

    companion object {
        private const val MAX_BYTE = 0xFF

        /** Red pixel luminosity. */
        private const val LUM_R = 0.2126

        /** Green pixel luminosity. */
        private const val LUM_G = 0.7152

        /** Blue pixel luminosity. */
        private const val LUM_B = 0.0722
    }
}
