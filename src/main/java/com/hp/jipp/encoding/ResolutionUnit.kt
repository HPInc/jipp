package com.hp.jipp.encoding

/** A unit of measurement used to describe resolution */
data class ResolutionUnit(override val name: String, override val code: Int) : Enum() {

    override fun toString() = name

    companion object {
        @JvmField
        val dotsPerInch = ResolutionUnit("dpi", 3)

        @JvmField
        val dotsPerCentimeter = ResolutionUnit("dpcm", 4)

        /** The encoder for converting integers to Operation objects  */
        @JvmField
        val ENCODER = encoderOf(ResolutionUnit::class.java, { name, code -> ResolutionUnit(name, code) })
    }
}
