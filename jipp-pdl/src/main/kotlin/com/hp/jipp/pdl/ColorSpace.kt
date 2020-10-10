// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.pdl

import java.io.InputStream
import java.io.OutputStream
import kotlin.math.roundToInt

/** Identifies a color space which describes how each pixel of image data is encoded */
@Suppress("MagicNumber")
enum class ColorSpace(val bytesPerPixel: Int) {
    /** Four bytes per pixel: Alpha, Red, Green Blue. */
    Argb(4),

    /** Three bytes per pixel: Red, Green, Blue */
    Rgb(3),

    /** One byte per pixel, between 0x00=Black and 0xFF=White */
    Grayscale(1);

    /** Return a converter lambda that will copy bytes from this color space to another. */
    fun converter(outputColor: ColorSpace): (ByteArray, ByteArray) -> Unit =
        when (this) {
            Grayscale ->
                when (outputColor) {
                    Grayscale -> { input, output ->
                        output[0] = input[0]
                    }
                    Rgb -> { input, output ->
                        output[0] = input[0]
                        output[1] = input[0]
                        output[2] = input[0]
                    }
                    Argb -> { input, output ->
                        output[0] = ALPHA_FULL
                        output[1] = input[0]
                        output[2] = input[0]
                        output[3] = input[0]
                    }
                }
            Rgb ->
                when (outputColor) {
                    Grayscale -> { input, output ->
                        output[0] = ((LUM_R * input[0]) + (LUM_G * input[1]) + (LUM_B * input[2]))
                            .roundToInt().toByte()
                    }
                    Rgb -> { input, output ->
                        input.copyInto(output)
                    }
                    Argb -> { input, output ->
                        output[0] = ALPHA_FULL
                        output[1] = input[0]
                        output[2] = input[1]
                        output[3] = input[2]
                    }
                }
            Argb ->
                when (outputColor) {
                    Grayscale -> { input, output ->
                        output[0] = (((LUM_R * input[1]) + (LUM_G * input[2]) + (LUM_B * input[3])) * input[0]
                            / ALPHA_FULL).roundToInt().toByte()
                    }
                    Rgb -> { input, output ->
                        when (input[0]) {
                            ALPHA_EMPTY -> {
                                output[0] = 0
                                output[1] = 0
                                output[2] = 0
                            }
                            ALPHA_FULL -> {
                                output[0] = input[1]
                                output[1] = input[2]
                                output[2] = input[3]
                            }
                            else -> {
                                val ratio = input[0].toDouble() / ALPHA_FULL
                                output[0] = (ratio * input[1]).roundToInt().toByte()
                                output[1] = (ratio * input[2]).roundToInt().toByte()
                                output[2] = (ratio * input[3]).roundToInt().toByte()
                            }
                        }
                    }
                    Argb -> { input, output ->
                        input.copyInto(output)
                    }
                }
        }

    /** Convert pixels encoded in this colorspace into pixels in the output stream/color space. */
    fun convert(input: InputStream, output: OutputStream, outputColor: ColorSpace) {
        val inputPixel = ByteArray(bytesPerPixel)
        val outputPixel = ByteArray(outputColor.bytesPerPixel)
        val converter = converter(outputColor)
        while (input.read(inputPixel) != -1) {
            converter(inputPixel, outputPixel)
            output.write(outputPixel)
        }
    }

    companion object {
        private const val ALPHA_EMPTY = 0x00.toByte()
        private const val ALPHA_FULL = 0xFF.toByte()
        private const val LUM_R = 0.2126
        private const val LUM_G = 0.7152
        private const val LUM_B = 0.0722
    }
}
