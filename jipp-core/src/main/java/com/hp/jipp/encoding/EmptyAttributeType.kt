// Copyright 2020 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/** Type for attributes with no values. */
open class EmptyAttributeType<T : Any>(
    override val name: String,
    val tag: OutOfBandTag
) : AttributeType<T> {
    override fun coerce(value: Any): T? = null
}
