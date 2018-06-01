// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.model

import com.hp.jipp.encoding.Enum
import com.hp.jipp.encoding.EnumType

/** An enumeration of possible printer states  */
data class PrinterState(override val code: Int, override val name: String) : Enum() {
    override fun toString() = super.toString()

    /** An attribute type for [PrinterState] objects */
    class Type(name: String) : EnumType<PrinterState>(Encoder, name)

    object Code {
        const val idle = 3
        const val processing = 4
        const val stopped = 5
    }

    companion object {
        @JvmField val idle = PrinterState(Code.idle, "idle")
        @JvmField val processing = PrinterState(Code.processing, "processing")
        @JvmField val stopped = PrinterState(Code.stopped, "stopped")

        @JvmField val Encoder = EnumType.Encoder(PrinterState::class.java) { code, name ->
            PrinterState(code, name)
        }
    }
}
