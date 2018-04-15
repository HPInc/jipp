// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.model

import com.hp.jipp.encoding.Enum
import com.hp.jipp.encoding.EnumType

/** A superset of Status and Operation  */
abstract class Code : Enum() {

    companion object {
        // MUST be lazy because Status and Operation are subclasses.
        val Encoder by lazy {
            EnumType.Encoder("Code", Status.Encoder.map.values + Operation.Encoder.map.values) {
                code: Int, name: String -> Status(code, name)
            }
        }
    }
}
