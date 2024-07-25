// Copyright 2017 - 2020 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/**
 * An otherwise unrecognized attribute, including at least one value, containing values of any legal type that
 * can be expressed in IPP form.
 */
class UnknownAttribute(
    name: String,
    values: List<Any>
) : AttributeImpl<Any>(name, Type(name), values) {

    /** Provide values as varargs. */
    constructor(name: String, value: Any, vararg values: Any) : this(name, listOf(value) + values)

    /** An unknown [AttributeType]. */
    class Type(override val name: String) : AttributeType<Any> {
        override fun coerce(value: Any): Any? = value
    }

    /** An unknown [AttributeType] carrying multiple values. */
    class SetType(override val name: String) : AttributeSetType<Any> {
        override fun coerce(value: Any): Any? = value
    }
}
