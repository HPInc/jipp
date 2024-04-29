// Copyright 2017 - 2020 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import java.lang.IllegalArgumentException

/**
 * Describes an object which may contain either a [Name] or a keyword ([String]).
 *
 * See [RFC8011 Section 5.1.3](https://tools.ietf.org/html/rfc8011#section-5.1.3).
 */
data class KeywordOrName constructor(val name: Name?, val keyword: String?) : TaggedValue, Stringable {

    /** Construct a [KeywordOrName] containing only a keyword. */
    constructor(keyword: String) : this(null, keyword)

    /** Construct a [KeywordOrName] containing only a name. */
    constructor(name: Name) : this(name, null)

    init {
        name ?: keyword ?: throw IllegalArgumentException("both .name and .keyword are null")
        if (name != null && keyword != null) throw IllegalArgumentException("both .name and .keyword are present")
    }

    override val tag: ValueTag = name?.tag ?: Tag.keyword

    override val value = name ?: keyword!!

    override fun asString() = name?.asString() ?: keyword!!

    override fun toString() =
        name?.toString() ?: keyword!!
}
