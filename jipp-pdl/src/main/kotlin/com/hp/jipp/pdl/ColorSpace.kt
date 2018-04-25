package com.hp.jipp.pdl

private const val THREE_BYTES: Int = 3

/** Identifies a color space which describes how each pixel of image data is encoded */
enum class ColorSpace(val bytesPerPixel: Int) {
    /** Three bytes per pixel: Red, Green, Blue */
    RGB(THREE_BYTES),

    /** One byte per pixel, between 0x00=Black and 0xFF=White */
    GRAYSCALE(1);
}
