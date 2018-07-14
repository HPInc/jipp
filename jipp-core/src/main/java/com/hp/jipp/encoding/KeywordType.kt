// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/** An attribute type containing keyword values as [String]s. */
open class KeywordType(override val name: String) : AttributeType<String> {
    override fun coerce(value: Any) =
        value as? String

    companion object {
        val codec = AttributeGroup.codec(Tag.keyword, {
            readString()
        }, {
            writeString(it)
        })
    }
}
