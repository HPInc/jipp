// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/**
 * An attribute, including at least one value, containing values of any legal type that can be expressed in IPP form.
 */
class AnyAttribute(
    name: String,
    values: List<Any>
) : BaseAttribute<Any>(name, anyType, values) {

    /** Provide values as varargs. */
    constructor(name: String, value: Any, vararg values: Any) : this(name, listOf(value) + values)

    companion object {
        val anyType: AttributeType<Any> = object : AttributeType<Any> {
            override fun coerce(value: Any): Any? = value
            override val name: String
                get() = "any"
        }
    }
}
