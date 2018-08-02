// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.util.BuildError

/** An attribute consisting only of an out-of-band tag and no values. */
data class EmptyAttribute(override val name: String, override val tag: Tag) : Attribute<Any> {
    init {
        if (!tag.isOutOfBand && !tag.isCollection) throw BuildError("$tag must be an out-of-band or collection tag")
    }
    override val values: List<Any> = listOf()
    override fun equals(other: Any?) =
        if (other is Attribute<*>) equalTo(other) else super.equals(other)

    override val type: AttributeType<Any> = object : AttributeType<Any> {
        override val name: String
            get() = "empty"

        override fun coerce(value: Any): Any? = value
    }
}
