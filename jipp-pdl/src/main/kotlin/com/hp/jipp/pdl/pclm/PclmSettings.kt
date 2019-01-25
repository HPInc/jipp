// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.pdl.pclm

import com.hp.jipp.model.MediaSource
import com.hp.jipp.model.OutputBin
import com.hp.jipp.model.PclmRasterBackSide
import com.hp.jipp.model.PrintQuality
import com.hp.jipp.model.Sides
import com.hp.jipp.pdl.ColorSpace
import com.hp.jipp.pdl.OutputSettings
import com.hp.jipp.pdl.PrinterOutputTray

/** Capabilities of the target device that are important when rendering */
data class PclmSettings(
    /** Color space. */
    override val colorSpace: ColorSpace = ColorSpace.Rgb,

    /** Two-sided printing selection, a keyword from [Sides]. */
    override val sides: String = Sides.oneSided,

    /** The media source to use, a keyword from [MediaSource]. */
    override val source: String = MediaSource.auto,

    /** The level of print quality to use, or null for default. */
    override val quality: PrintQuality? = null,

    /** Output Bin setting, either [OutputBin.faceDown] or [OutputBin.faceUp]. */
    override val outputBin: String = OutputBin.faceUp,

    /** Stacking order, a keyword from [PrinterOutputTray.StackingOrder]. */
    override val stackingOrder: String = PrinterOutputTray.StackingOrder.firstToLast,

    /** Height of each strip to send via PCLM, in pixels. */
    val stripHeight: Int,

    /** Backside requirement, must be a [PclmRasterBackSide] value. */
    val backSide: String = PclmRasterBackSide.normal
) : OutputSettings
