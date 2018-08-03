// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/** An attribute consisting only of an out-of-band tag and no values. */
class EmptyAttribute(name: String, tag: Tag) : BaseAttribute<Any>(name, AnyAttribute.anyType, tag)
