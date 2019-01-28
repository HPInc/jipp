// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.pdl

import com.hp.jipp.model.MediaSource
import com.hp.jipp.model.OutputBin
import com.hp.jipp.model.PrintQuality
import com.hp.jipp.model.Sides

/** Generic output settings which may be required for any Page Description Language. */
data class OutputSettings(
    /** Color space. */
    val colorSpace: ColorSpace = ColorSpace.Rgb,

    /** Two-sided printing selection, a keyword from [Sides]. */
    val sides: String = Sides.oneSided,

    /** The media source to use, a keyword from [MediaSource]. */
    val source: String = MediaSource.auto,

    /** The level of print quality to use, or null for default. */
    val quality: PrintQuality? = null,

    /** Output Bin setting, either [OutputBin.faceDown] or [OutputBin.faceUp]. */
    val outputBin: String = OutputBin.faceUp,

    /** Stacking order, a keyword from [PrinterOutputTray.StackingOrder]. */
    val stackingOrder: String = PrinterOutputTray.StackingOrder.lastToFirst
)
