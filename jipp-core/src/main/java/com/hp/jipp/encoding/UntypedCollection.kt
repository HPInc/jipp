// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/** An attribute collection in its raw, untyped form */
data class UntypedCollection(override val attributes: List<Attribute<*>>) : AttributeCollection {
    constructor(vararg attributes: Attribute<*>) : this(attributes.toList())

    class Type(override val name: String) : AttributeType<UntypedCollection> {
        override fun coerce(value: Any) =
            value as? UntypedCollection
    }
}
