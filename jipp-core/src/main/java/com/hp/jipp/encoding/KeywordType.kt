// Copyright 2017 - 2021 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/** An [AttributeType] for a keyword value, represented as a [String]. */
open class KeywordType(name: String) : AttributeTypeImpl<String>(name, String::class.java) {
    /** An [AttributeType] for keywords, represented as [String]s. */
    class Set(name: String) : KeywordType(name), AttributeSetType<String> {
        override fun toString() = "KeywordType.Set($name)"
    }

    override fun toString() = "KeywordType($name)"

    companion object {
        val codec = Codec(
            Tag.keyword,
            {
                readString()
            },
            {
                writeStringValue(it)
            }
        )
    }
}
