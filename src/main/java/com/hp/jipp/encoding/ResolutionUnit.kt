package com.hp.jipp.encoding

/** A unit of measurement used to describe resolution */
data class ResolutionUnit(override val name: String, override val code: Int) : NameCode() {
    companion object {
        @JvmField val DotsPerInch = ResolutionUnit("dpi", 3)
        @JvmField val DotsPerCentimeter = ResolutionUnit("dpcm", 4)

        /** The encoder for converting integers to Operation objects  */
        @JvmField
        val ENCODER: NameCodeType.Encoder<ResolutionUnit> = NameCodeType.Encoder.of(
                ResolutionUnit::class.java, object : Factory<ResolutionUnit> {
            override fun of(name: String, code: Int): ResolutionUnit {
                return ResolutionUnit(name, code)
            }
        })
    }
}
