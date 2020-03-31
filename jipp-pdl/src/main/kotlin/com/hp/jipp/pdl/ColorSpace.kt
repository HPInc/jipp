// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.pdl

import java.io.InputStream
import java.io.OutputStream

/** Identifies a color space which describes how each pixel of image data is encoded */
@Suppress("MagicNumber")
enum class ColorSpace(val bytesPerPixel: Int) {
    /** Three bytes per pixel: Red, Green, Blue */
    Rgb(3),

    /** One byte per pixel, between 0x00=Black and 0xFF=White */
    Grayscale(1);

    /** Return a converter lambda that will copy bytes from this color space to another. */
    fun converter(outputColor: ColorSpace): (ByteArray, ByteArray) -> Unit =
        when (this) {
            Grayscale ->
                when (outputColor) {
                    Grayscale -> { input, output -> output[0] = input[0] }
                    Rgb -> { input, output ->
                        output[0] = input[0]
                        output[1] = input[0]
                        output[2] = input[0]
                    }
                }
            Rgb ->
                when (outputColor) {
                    Grayscale -> { input, output ->
                        output[0] = ((LUM_R * input[0]) + (LUM_G * input[1]) + (LUM_B * input[2])).toByte()
                    }
                    Rgb -> { input, output -> input.copyInto(output) }
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
        private const val LUM_R = 0.2126
        private const val LUM_G = 0.7152
        private const val LUM_B = 0.0722
    }
}
