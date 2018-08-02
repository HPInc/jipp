// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/**
 * Describes a name object, which may or may not have a language string.
 */
data class Name(override val value: String, val lang: String?) : TaggedValue(), Stringable {
    constructor(value: String) : this(value, null)

    override val tag: Tag = if (lang == null) Tag.nameWithoutLanguage else Tag.nameWithLanguage

    override fun asString() = value

    override fun toString() =
        if (lang == null) {
            "\"$value\" (name)"
        } else {
            "\"$value\" ($lang name)"
        }
}
