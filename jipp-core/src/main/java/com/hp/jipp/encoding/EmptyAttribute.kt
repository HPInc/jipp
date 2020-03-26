// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/** An attribute consisting only of an out-of-band tag and no values. */
class EmptyAttribute(name: String, tag: OutOfBandTag) : BaseAttribute<Nothing>(name, emptyType, tag) {
    companion object {
        val emptyType: AttributeType<Nothing> = object : AttributeType<Nothing> {
            override fun coerce(value: Any): Nothing? = null
            override val name: String
                get() = "empty"
        }
    }
}
