package com.hp.jipp.model

import com.hp.jipp.encoding.Enum
import com.hp.jipp.encoding.EnumType

/** An enumeration of possible printer states  */
class PrinterState(override val name: String, override val code: Int) : Enum() {

    override fun toString() = name

    class Type(name: String) : EnumType<PrinterState>(ENCODER, name)

    companion object {

        @JvmField val Idle = PrinterState("idle", 3)
        @JvmField val Processing = PrinterState("processing", 4)
        @JvmField val Stopped = PrinterState("stopped", 5)

        @JvmField val ENCODER = EnumType.Encoder(
                PrinterState::class.java, { name, code -> PrinterState(name, code) })
    }
}
