// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/**
 * An attribute type for `name` types, which can be encoded with either [Tag.nameWithoutLanguage] or
 * [Tag.nameWithLanguage].
 *
 * See [RFC8011 Section 5.1.3](https://tools.ietf.org/html/rfc8011#section-5.1.3).
 */
open class NameType(override val name: String) : AttributeType<Name> {
    override fun coerce(value: Any) =
        value as? Name

    /** Return an attribute containing values as text strings (without language) */
    fun of(vararg values: String) = of(values.map { Name(it) })

    /** Return an attribute containing values as text strings (without language) */
    fun ofStrings(values: List<String>) = of(values.map { Name(it) })

    companion object {
        val codec = AttributeGroup.codec({ it == Tag.nameWithLanguage || it == Tag.nameWithoutLanguage }, {
            if (it == Tag.nameWithLanguage) {
                readShort()
                val lang = readString() // Lang comes first
                Name(readString(), lang)
            } else {
                Name(readString())
            }
        }, {
            if (it.tag == Tag.nameWithLanguage) {
                writeShort(stringLength(it.lang!!) + stringLength(it.value))
                writeString(it.lang)
                writeString(it.value)
            } else {
                writeString(it.value)
            }
        })
    }
}
