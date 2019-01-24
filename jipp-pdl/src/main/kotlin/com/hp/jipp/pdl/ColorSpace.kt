// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.pdl

/** Identifies a color space which describes how each pixel of image data is encoded */
enum class ColorSpace(val bytesPerPixel: Int) {
    /** Three bytes per pixel: Red, Green, Blue */
    RGB(3),

    /** One byte per pixel, between 0x00=Black and 0xFF=White */
    GRAYSCALE(1);
}
