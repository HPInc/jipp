// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.pdl

import java.io.InputStream
import java.io.OutputStream
import kotlin.math.roundToInt

/** Identifies a color space which describes how each pixel of image data is encoded */
@Suppress("MagicNumber")
enum class ColorSpace(val bytesPerPixel: Int) {
    /** Four bytes per pixel: Blue, Green, Red, Alpha (little-endian ARGB). */
    Bgra(4),

    /** Three bytes per pixel: Red, Green, Blue */
    Rgb(3),

    /** One byte per pixel, between 0x00=Black and 0xFF=White */
    Grayscale(1);

    /** Return a converter lambda that will copy bytes from this color space to another. */
    fun converter(outputColor: ColorSpace): (ByteArray, Int, OutputStream) -> Unit =
        when (this) {
            Grayscale -> grayscaleConverter(outputColor)
            Rgb -> rgbConverter(outputColor)
            Bgra -> bgraConverter(outputColor)
        }

    private fun grayscaleConverter(outputColor: ColorSpace): (ByteArray, Int, OutputStream) -> Unit =
        when (outputColor) {
            Grayscale -> { input, inputOffset, output ->
                output.write(input[inputOffset].toInt())
            }
            Rgb -> { input, inputOffset, output ->
                val byte = input[inputOffset].toInt()
                output.write(byte)
                output.write(byte)
                output.write(byte)
            }
            Bgra -> { input, inputOffset, output ->
                val byte = input[inputOffset].toInt()
                output.write(ALPHA_FULL)
                output.write(byte)
                output.write(byte)
                output.write(byte)
            }
        }

    private fun rgbConverter(outputColor: ColorSpace): (ByteArray, Int, OutputStream) -> Unit =
        when (outputColor) {
            Grayscale -> { input, inputOffset, output ->
                output.write(((LUM_R * input[inputOffset]) +
                    (LUM_G * input[inputOffset + 1]) +
                    (LUM_B * input[inputOffset + 2])).roundToInt())
            }
            Rgb -> { input, inputOffset, output ->
                output.write(input, inputOffset, bytesPerPixel)
            }
            Bgra -> { input, inputOffset, output ->
                output.write(input[inputOffset + 2].toInt())
                output.write(input[inputOffset + 1].toInt())
                output.write(input[inputOffset].toInt())
                output.write(ALPHA_FULL)
            }
        }

    private fun bgraConverter(outputColor: ColorSpace): (ByteArray, Int, OutputStream) -> Unit =
        when (outputColor) {
            Grayscale -> { input, inputOffset, output ->
                output.write((((LUM_R * input[inputOffset + 2]) +
                    (LUM_G * input[inputOffset + 1]) +
                    (LUM_B * input[inputOffset + 0])) * input[3] / ALPHA_FULL
                    ).roundToInt())
            }
            Rgb -> { input, inputOffset, output ->
                when (input[3]) {
                    0x00.toByte() -> {
                        output.write(ALPHA_FULL)
                        output.write(ALPHA_FULL)
                        output.write(ALPHA_FULL)
                    }
                    0xFF.toByte() -> {
                        output.write(input[inputOffset + 2].toInt())
                        output.write(input[inputOffset + 1].toInt())
                        output.write(input[inputOffset + 0].toInt())
                    }
                    else -> {
                        val ratio = input[inputOffset].toDouble() / ALPHA_FULL
                        output.write(((ALPHA_FULL - input[inputOffset + 2]) * ratio).roundToInt())
                        output.write(((ALPHA_FULL - input[inputOffset + 1]) * ratio).roundToInt())
                        output.write(((ALPHA_FULL - input[inputOffset + 0]) * ratio).roundToInt())
                    }
                }
            }
            Bgra -> { input, inputOffset, output ->
                output.write(input, inputOffset, bytesPerPixel)
            }
        }

    /** Write [input] pixels encoded in this [ColorSpace] to [output] in [outputColor]. */
    fun convert(input: InputStream, output: OutputStream, outputColor: ColorSpace) {
        val inputPixel = ByteArray(bytesPerPixel)
        val converter = converter(outputColor)
        while (input.read(inputPixel) != -1) {
            converter(inputPixel, 0, output)
        }
    }

    companion object {
        private const val ALPHA_FULL = 0xFF
        private const val LUM_R = 0.2126
        private const val LUM_G = 0.7152
        private const val LUM_B = 0.0722
    }
}
