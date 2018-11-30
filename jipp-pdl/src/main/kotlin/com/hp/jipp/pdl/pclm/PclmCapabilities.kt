// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.pdl.pclm

/** Capabilities of the target device that are important when rendering */
data class PclmCapabilities(
    /** Height of each strip to send via PCLM, in pixels */
    val stripHeight: Int,
    /** True if the target device supports color */
    val color: Boolean
)
