// Copyright 2017 - 2021 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/**
 * An [AttributeCollection] (see [RFC3382](https://www.iana.org/go/rfc3382)) represented as a list of [Attribute]
 * objects.
 *
 * All received collection attributes are parsed into this type, but may be coerced into typed collections
 * when extracted from an [AttributeGroup].
 */
data class UntypedCollection(override val attributes: List<Attribute<*>>) : AttributeCollection {
    /** An [AttributeType] for a single [UntypedCollection] value */
    class Type(name: String) : AttributeTypeImpl<UntypedCollection>(name, UntypedCollection::class.java)

    /** An [AttributeType] for multiple [UntypedCollection] values */
    class SetType(name: String) :
        AttributeTypeImpl<UntypedCollection>(name, UntypedCollection::class.java),
        AttributeSetType<UntypedCollection> {
        override fun toString() = "UntypedCollection.Set($name)"
    }
}
