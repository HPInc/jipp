// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/**
 * An otherwise unrecognized attribute, including at least one value, containing values of any legal type that
 * can be expressed in IPP form.
 */
class UnknownAttribute(
    name: String,
    values: List<Any>
) : BaseAttribute<Any>(name, values) {

    /** Provide values as varargs. */
    constructor(name: String, value: Any, vararg values: Any) : this(name, listOf(value) + values)

    /** An unknown attribute type. */
    class Type(override val name: String) : AttributeType<Any> {
        override fun coerce(value: Any): Any? = value
    }
}
