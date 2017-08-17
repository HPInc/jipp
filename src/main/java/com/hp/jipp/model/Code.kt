package com.hp.jipp.model

import com.hp.jipp.encoding.Enum
import com.hp.jipp.encoding.EnumType

/** A superset of Status and Operation  */
abstract class Code : Enum() {

    companion object {
        // MUST be lazy because Status and Operation are subclasses.
        val ENCODER by lazy {
            EnumType.Encoder("Code", Status.ENCODER.map.values + Operation.ENCODER.map.values,
                    { name: String, code: Int -> Status(name, code) })
        }
    }
}
