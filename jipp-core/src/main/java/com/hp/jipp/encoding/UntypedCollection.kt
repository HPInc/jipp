// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/**
 * An attribute collection (see [RFC3382](https://www.iana.org/go/rfc3382)) represented as a list of [Attribute]
 * objects.
 *
 * All received collection attributes are parsed into this type, but may be coerced into typed collections
 * when extracted from an [AttributeGroup].
 */
data class UntypedCollection(override val attributes: List<Attribute<*>>) : AttributeCollection {
    /** An [AttributeType] for [UntypedCollection] values */
    class Type(override val name: String) : AttributeType<UntypedCollection> {
        override fun coerce(value: Any) =
            value as? UntypedCollection
    }
}
