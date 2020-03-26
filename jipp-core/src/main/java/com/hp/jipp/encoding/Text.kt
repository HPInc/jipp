// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/**
 * Describes a text object, which may or may not have a language string.
 */
data class Text(override val value: String, val lang: String?) : TaggedValue(), Stringable {
    constructor(value: String) : this(value, null)

    override val tag = if (lang == null) Tag.textWithoutLanguage else Tag.textWithLanguage

    override fun asString() = value

    override fun toString() =
        if (lang == null) {
            "\"$value\" (text)"
        } else {
            "\"$value\" ($lang text)"
        }
}
