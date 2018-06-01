package com.hp.jipp.model

import com.hp.jipp.encoding.Enum
import com.hp.jipp.encoding.EnumType

/** Enums defining relative print quality (RFC8011, Section 5.2.13). */
data class PrintQuality(override val code: Int, override val name: String) : Enum() {

    override fun toString() = super.toString()

    /** An attribute type for attributes based on this enum */
    class Type(name: String) : EnumType<PrintQuality>(PrintQuality.Encoder, name)

    object Code {
        const val draft = 3
        const val normal = 4
        const val high = 5
    }

    companion object {
        @JvmField val draft = PrintQuality(Code.draft, "draft")
        @JvmField val normal = PrintQuality(Code.normal, "normal")
        @JvmField val high = PrintQuality(Code.high, "high")

        /** The encoder for converting integers to this Enum object */
        @JvmField
        val Encoder = EnumType.Encoder(PrintQuality::class.java) { code, name ->
            PrintQuality(code, name)
        }
    }
}
