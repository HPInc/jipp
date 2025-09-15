// © Copyright 2017 - 2020 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/** An [Attribute] consisting only of an out-of-band tag and no values. */
class EmptyAttribute<T : Any>(
    name: String,
    val tag: OutOfBandTag
) : AttributeImpl<T>(name, EmptyAttributeType<T>(name, tag), listOf())
