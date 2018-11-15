// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/**
 * A type of attribute which is most clearly represented as a [String]. Note: may be used
 * to get a more convenient representation of types which may contain combinations of Name,
 * Keyword, Text, etc.
 */
open class StringType(val tag: Tag, override val name: String) : AttributeType<String> {
    override fun coerce(value: Any) =
        when (value) {
            is String -> value
            is OtherString -> value.value
            else -> null
        }
}
