// © Copyright 2018 - 2020 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.pdl

import com.hp.jipp.model.MediaSource
import com.hp.jipp.model.PrintQuality
import com.hp.jipp.model.Sides
import com.hp.jipp.model.Types
import com.hp.jipp.pdl.pclm.PclmWriter

/** Generic output settings which may be required for any Page Description Language. */
data class OutputSettings @JvmOverloads constructor(
    /** Color space. */
    val colorSpace: ColorSpace = ColorSpace.Rgb,

    /** Two-sided printing selection, a keyword from [Sides]. */
    val sides: String = Sides.oneSided,

    /** The media source to use, a keyword from [MediaSource]. */
    val source: String = MediaSource.auto,

    /** The level of print quality to use, or null for default. */
    val quality: PrintQuality? = null,

    /** True if page order should be reversed (due to face-up output). */
    val reversed: Boolean = false,

    /**
     * Set the number of copies required.
     *
     * Note: if this is set for an IPP job, substitute [Types.copies] to 1 and [Types.jobPagesPerSet] to an
     * appropriate value (see [PclmWriter.calculateJobPagesPerSet] for PCLM and document size for PWG raster).
     */
    val copies: Int = 1
)
