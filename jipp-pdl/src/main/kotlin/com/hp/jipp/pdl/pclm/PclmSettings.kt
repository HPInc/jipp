// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.pdl.pclm

import com.hp.jipp.model.PclmRasterBackSide
import com.hp.jipp.pdl.OutputSettings

/** Capabilities of the target device that are important when rendering */
data class PclmSettings(
    /** Ordinary output settings. */
    val output: OutputSettings = OutputSettings(),

    /** Height of each strip to send via PCLM, in pixels. */
    val stripHeight: Int,

    /** Backside requirement, must be a [PclmRasterBackSide] value. */
    val backSide: String = PclmRasterBackSide.normal
)
