package com.hp.jipp.model

import com.hp.jipp.encoding.NameCode
import com.hp.jipp.encoding.NameCodeType

/** An enumeration of possible printer states  */
class PrinterState(override val name: String, override val code: Int) : NameCode() {

    override fun toString() = name

    companion object {

        @JvmField val Idle = PrinterState("idle", 3)
        @JvmField val Processing = PrinterState("processing", 4)
        @JvmField val Stopped = PrinterState("stopped", 5)

        @JvmField val ENCODER: NameCodeType.Encoder<PrinterState> = NameCodeType.Encoder.of(
                PrinterState::class.java, object : NameCode.Factory<PrinterState> {
            override fun of(name: String, code: Int) = PrinterState(name, code)
        })
    }
}
