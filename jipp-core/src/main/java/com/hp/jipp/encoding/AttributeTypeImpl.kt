// © Copyright 2020 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/** A base implementation of [AttributeType]. */
open class AttributeTypeImpl<T : Any>(
    override val name: String,
    private val cls: Class<T>
) : AttributeType<T> {
    override fun coerce(value: Any): T? =
        @Suppress("UNCHECKED_CAST")
        if (cls.isInstance(value)) value as T else null

    override fun toString() = "${cls.simpleName}Type($name)"
}
