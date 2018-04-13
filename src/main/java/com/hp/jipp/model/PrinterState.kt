// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.model

import com.hp.jipp.encoding.Enum
import com.hp.jipp.encoding.EnumType

/** An enumeration of possible printer states  */
data class PrinterState(override val code: Int, override val name: String) : Enum() {

    /** An attribute type for [PrinterState] objects */
    class Type(name: String) : EnumType<PrinterState>(Encoder, name)

    companion object {
        @JvmField val idle = PrinterState(3, "idle")
        @JvmField val processing = PrinterState(4, "processing")
        @JvmField val stopped = PrinterState(5, "stopped")

        @JvmField val Encoder = EnumType.Encoder(PrinterState::class.java) { code, name ->
            PrinterState(code, name)
        }
    }
}
