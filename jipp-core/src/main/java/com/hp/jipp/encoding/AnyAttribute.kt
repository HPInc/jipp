// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.util.PrettyPrintable

/**
 * An attribute, including at least one value, containing values of any legal type that can be expressed in IPP form.
 */
data class AnyAttribute(
    override val name: String,
    override val values: List<Any>
) : Attribute<Any>, PrettyPrintable {

    /** Provide values as varargs. */
    constructor(name: String, value: Any, vararg values: Any) : this(name, listOf(value) + values)

    override val tag: Tag? = null

    override val type: AttributeType<Any> = object : AttributeType<Any> {
        override fun coerce(value: Any): Any? = value
        override val name: String
            get() = "any"
    }

    override fun equals(other: Any?) =
        if (other is Attribute<*>) equalTo(other) else super.equals(other)

    override fun hashCode() = hashOf()

    init {
        if (tag == null && values.isEmpty()) {
            throw IllegalArgumentException("tag cannot be null when values are empty")
        }
    }
}
