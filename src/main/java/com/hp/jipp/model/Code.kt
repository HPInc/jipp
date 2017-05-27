package com.hp.jipp.model

import com.hp.jipp.encoding.NameCode
import com.hp.jipp.encoding.NameCodeType

/** A superset of Status and Operation  */
abstract class Code : NameCode() {

    companion object {
        private val all: List<Code> by lazy {
            Status.ENCODER.map.values + Operation.ENCODER.map.values
        }

        val ENCODER: NameCodeType.Encoder<Code> by lazy {
            NameCodeType.Encoder.of("Code", all, object : NameCode.Factory<Code> {
                override fun of(name: String, code: Int): Code {
                    return Status(name, code)
                }
            })
        }
    }
}
