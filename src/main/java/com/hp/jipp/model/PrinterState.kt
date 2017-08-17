package com.hp.jipp.model

import com.hp.jipp.encoding.Enum
import com.hp.jipp.encoding.EnumType
import com.hp.jipp.encoding.encoderOf

/** An enumeration of possible printer states  */
data class PrinterState(override val name: String, override val code: Int) : Enum() {

    /** An attribute type for [PrinterState] objects */
    class Type(name: String) : EnumType<PrinterState>(ENCODER, name)

    companion object {

        @JvmField val idle = PrinterState("idle", 3)
        @JvmField val processing = PrinterState("processing", 4)
        @JvmField val stopped = PrinterState("stopped", 5)

        @JvmField val ENCODER = encoderOf(PrinterState::class.java, { name, code -> PrinterState(name, code) })
    }
}
