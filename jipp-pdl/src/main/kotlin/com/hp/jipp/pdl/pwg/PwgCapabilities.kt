// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.pdl.pwg

/**
 * Define bits that are to be set in pages sent over PWG-Raster format.
 */
data class PwgCapabilities(
    val color: Boolean = true,
    /** True if using double-sided printing. */
    val duplex: Boolean = false,
    /** True if double-sided printing will use "tumble" (short-edge) mode. */
    val tumble: Boolean = true
)
