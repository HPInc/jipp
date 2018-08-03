// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/** An attribute consisting only of an out-of-band tag and no values. */
class EmptyAttribute(name: String, tag: Tag) : BaseAttribute<Any>(name, emptyType, tag) {
    companion object {
        val emptyType: AttributeType<Any> = object : AttributeType<Any> {
            override fun coerce(value: Any): Any? = value
            override val name: String
                get() = "empty"
        }
    }
}
