// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.pdl.pwg

import com.hp.jipp.model.MediaSource
import com.hp.jipp.model.PrintQuality
import com.hp.jipp.model.PwgRasterDocumentSheetBack
import com.hp.jipp.model.Sides
import com.hp.jipp.pdl.OutputSettings
import com.hp.jipp.pdl.RenderableDocument
import com.hp.jipp.pdl.RenderablePage
import com.hp.jipp.pdl.isEven

/**
 * Provide settings for PWG-Raster output.
 */
data class PwgSettings(
    /** Ordinary output settings. */
    val output: OutputSettings = OutputSettings(),

    /** The coordinate system requested for the back side of two-sided sheets, from [PwgRasterDocumentSheetBack]. */
    val sheetBack: String = PwgRasterDocumentSheetBack.normal
) {
    /** The calculated [PwgHeader.MediaPosition] for these settings. */
    val pwgMediaPosition = output.source.toPwgMediaPosition()

    /** The calculated [PwgHeader.ColorSpace] for these settings. */
    val pwgColorSpace = PwgHeader.ColorSpace.from(output.colorSpace)

    /** The calculated [PwgHeader.PrintQuality] for these settings. */
    val pwgPrintQuality = output.quality?.toPwgPrintQuality() ?: PwgHeader.PrintQuality.Default

    /**
     * Build a [PwgHeader] from current settings.
     */
    fun buildHeader(
        doc: RenderableDocument,
        page: RenderablePage,
        /** 0-based page number. */

        pageNumber: Int
    ): PwgHeader {
        val transform = PwgFeedTransform.lookup(pageNumber, output.sides, sheetBack)
        return PwgHeader(
            hwResolutionX = doc.dpi,
            hwResolutionY = doc.dpi,
            pageSizeX = page.widthPixels * POINTS_PER_INCH / doc.dpi,
            pageSizeY = page.heightPixels * POINTS_PER_INCH / doc.dpi,
            width = page.widthPixels,
            height = page.heightPixels,
            bitsPerColor = BITS_PER_BYTE,
            bitsPerPixel = output.colorSpace.bytesPerPixel * BITS_PER_BYTE,
            colorSpace = pwgColorSpace,
            duplex = output.sides != Sides.oneSided,
            tumble = output.sides == Sides.twoSidedShortEdge,
            mediaPosition = pwgMediaPosition,
            printQuality = pwgPrintQuality,
            feedTransform = transform.feedTransform,
            crossFeedTransform = transform.crossFeedTransform
        )
    }

    /** Used to look up the correct feed transformation */
    @SuppressWarnings("MagicNumber")
    private class PwgFeedTransform(val crossFeedTransform: Int, val feedTransform: Int) {
        companion object {
            // Taken from Table 20 in Wi-Fi Peer-to-Peer Services Print Technical Specification v1.1
            private val transforms = mapOf(
                (Sides.twoSidedLongEdge to PwgRasterDocumentSheetBack.flipped) to PwgFeedTransform(1, -1),
                (Sides.twoSidedLongEdge to PwgRasterDocumentSheetBack.rotated) to PwgFeedTransform(-1, -1),
                (Sides.twoSidedShortEdge to PwgRasterDocumentSheetBack.flipped) to PwgFeedTransform(-1, 1),
                (Sides.twoSidedShortEdge to PwgRasterDocumentSheetBack.manualTumble) to PwgFeedTransform(-1, -1))
            val default = PwgFeedTransform(1, 1)

            /** Return the correct transform given a 0-based page number, sides mode, and sheet-back requirements. */
            fun lookup(pageNumber: Int, sides: String, sheetBack: String) =
                if (pageNumber.isEven) default else transforms.getOrDefault(sides to sheetBack, default)
        }
    }

    companion object {
        private const val POINTS_PER_INCH = 72
        const val BITS_PER_BYTE = 8

        private fun String.toPwgMediaPosition() =
            when (this) {
                MediaSource.alternate -> PwgHeader.MediaPosition.Alternate
                MediaSource.alternateRoll -> PwgHeader.MediaPosition.AlternateRoll
                MediaSource.auto -> PwgHeader.MediaPosition.Auto
                MediaSource.bottom -> PwgHeader.MediaPosition.Bottom
                MediaSource.byPassTray -> PwgHeader.MediaPosition.ByPassTray
                MediaSource.center -> PwgHeader.MediaPosition.Center
                MediaSource.disc -> PwgHeader.MediaPosition.Disc
                MediaSource.envelope -> PwgHeader.MediaPosition.Envelope
                MediaSource.hagaki -> PwgHeader.MediaPosition.Hagaki
                MediaSource.largeCapacity -> PwgHeader.MediaPosition.LargeCapacity
                MediaSource.left -> PwgHeader.MediaPosition.Left
                MediaSource.main -> PwgHeader.MediaPosition.Main
                MediaSource.mainRoll -> PwgHeader.MediaPosition.MainRoll
                MediaSource.manual -> PwgHeader.MediaPosition.Manual
                MediaSource.middle -> PwgHeader.MediaPosition.Middle
                MediaSource.photo -> PwgHeader.MediaPosition.Photo
                MediaSource.rear -> PwgHeader.MediaPosition.Rear
                MediaSource.right -> PwgHeader.MediaPosition.Right
                MediaSource.roll1 -> PwgHeader.MediaPosition.Roll1
                MediaSource.roll10 -> PwgHeader.MediaPosition.Roll10
                MediaSource.roll2 -> PwgHeader.MediaPosition.Roll2
                MediaSource.roll3 -> PwgHeader.MediaPosition.Roll3
                MediaSource.roll4 -> PwgHeader.MediaPosition.Roll4
                MediaSource.roll5 -> PwgHeader.MediaPosition.Roll5
                MediaSource.roll6 -> PwgHeader.MediaPosition.Roll6
                MediaSource.roll7 -> PwgHeader.MediaPosition.Roll7
                MediaSource.roll8 -> PwgHeader.MediaPosition.Roll8
                MediaSource.roll9 -> PwgHeader.MediaPosition.Roll9
                MediaSource.side -> PwgHeader.MediaPosition.Side
                MediaSource.top -> PwgHeader.MediaPosition.Top
                MediaSource.tray1 -> PwgHeader.MediaPosition.Tray1
                MediaSource.tray10 -> PwgHeader.MediaPosition.Tray10
                MediaSource.tray11 -> PwgHeader.MediaPosition.Tray11
                MediaSource.tray12 -> PwgHeader.MediaPosition.Tray12
                MediaSource.tray13 -> PwgHeader.MediaPosition.Tray13
                MediaSource.tray14 -> PwgHeader.MediaPosition.Tray14
                MediaSource.tray15 -> PwgHeader.MediaPosition.Tray15
                MediaSource.tray16 -> PwgHeader.MediaPosition.Tray16
                MediaSource.tray17 -> PwgHeader.MediaPosition.Tray17
                MediaSource.tray18 -> PwgHeader.MediaPosition.Tray18
                MediaSource.tray19 -> PwgHeader.MediaPosition.Tray19
                MediaSource.tray2 -> PwgHeader.MediaPosition.Tray2
                MediaSource.tray20 -> PwgHeader.MediaPosition.Tray20
                MediaSource.tray3 -> PwgHeader.MediaPosition.Tray3
                MediaSource.tray4 -> PwgHeader.MediaPosition.Tray4
                MediaSource.tray5 -> PwgHeader.MediaPosition.Tray5
                MediaSource.tray6 -> PwgHeader.MediaPosition.Tray6
                MediaSource.tray7 -> PwgHeader.MediaPosition.Tray7
                MediaSource.tray8 -> PwgHeader.MediaPosition.Tray8
                MediaSource.tray9 -> PwgHeader.MediaPosition.Tray9
                else -> throw IllegalArgumentException("$this is not a recognized media source type")
            }

        private fun PrintQuality.toPwgPrintQuality() =
            when (this) {
                PrintQuality.draft -> PwgHeader.PrintQuality.Draft
                PrintQuality.normal -> PwgHeader.PrintQuality.Normal
                PrintQuality.high -> PwgHeader.PrintQuality.High
                else -> null
            }
    }
}
