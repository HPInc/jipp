// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.pdl

/**
 * Values from section 5.6.36.1 of
 * https://ftp.pwg.org/pub/pwg/candidates/cs-ippjobprinterext3v10-20120727-5100.13.pdf
 *
 * Note: a parser for this type is planned for the jipp model
 */
object PrinterOutputTray {
    object StackingOrder {
        val firstToLast = "firstToLast"
        val lastToFirst = "lastToFirst"
    }
}
