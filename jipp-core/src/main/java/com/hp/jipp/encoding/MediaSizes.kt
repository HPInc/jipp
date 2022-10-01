// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.model.MediaCol
import com.hp.jipp.model.MediaColDatabase
import java.util.regex.Pattern

object MediaSizes {
    private val widthHeightPattern = Pattern.compile(
        "_([0-9]+(\\.[0-9]+)?)?x([0-9]+(\\.[0-9]+)?)([a-z]+)?$"
    )
    private const val WIDTH_AT = 1
    private const val HEIGHT_AT = 3
    private const val WIDTH_HEIGHT_DIMENSION_COUNT = 4
    private const val WIDTH_HEIGHT_UNIT_AT = 5

    private const val MM_HUNDREDTHS_PER_INCH = 2540
    private const val MM_HUNDREDTHS_PER_MM = 100

    /** Convert a media name containing dimensions into a [MediaCol.MediaSize] object, if possible. */
    @JvmStatic
    fun parse(mediaName: String): MediaCol.MediaSize? {
        val matches = widthHeightPattern.matcher(mediaName)
        if (!matches.find() || matches.groupCount() < WIDTH_HEIGHT_DIMENSION_COUNT) {
            // No way to guess media size from name, not enough parts.
            return null
        }

        // Assume mm unless "in"
        val unitString = if (matches.groupCount() >= WIDTH_HEIGHT_UNIT_AT) {
            matches.group(WIDTH_HEIGHT_UNIT_AT)
        } else {
            null
        }
        val units = if (unitString == "in") MM_HUNDREDTHS_PER_INCH else MM_HUNDREDTHS_PER_MM
        val x = (matches.group(WIDTH_AT).toDouble() * units).toInt()
        val y = (matches.group(HEIGHT_AT).toDouble() * units).toInt()
        return MediaCol.MediaSize(x, y)
    }

    /** Convert a [MediaCol.MediaSize] object to [MediaColDatabase.MediaSize] object. */
    @JvmStatic
    fun MediaCol.MediaSize?.toMediaColDatabaseMediaSize(): MediaColDatabase.MediaSize? {
        return this?.let {
            MediaColDatabase.MediaSize(IntOrIntRange(xDimension!!), IntOrIntRange(yDimension!!))
        }
    }
}
