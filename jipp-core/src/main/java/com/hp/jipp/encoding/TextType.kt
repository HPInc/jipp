// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/**
 * An attribute type for `text` types, which can be encoded with either [Tag.textWithoutLanguage] or
 * [Tag.textWithLanguage].
 *
 * See [RFC8011 Section 5.1.2](https://tools.ietf.org/html/rfc8011#section-5.1.2).
 */
open class TextType(override val name: String) : AttributeType<Text> {
    override fun coerce(value: Any) =
        value as? Text

    /** Return an attribute containing values as text strings (without language) */
    fun of(vararg values: String) = of(values.map { Text(it) })

    /** Return an attribute containing values as text strings (without language) */
    fun ofStrings(values: Iterable<String>) = of(values.map { Text(it) })

    override fun toString() = "TextType($name)"

    companion object {
        val codec = Codec({ it == Tag.textWithLanguage || it == Tag.textWithoutLanguage }, {
            if (it == Tag.textWithLanguage) {
                readShort()
                val lang = readString() // Lang comes first
                Text(readString(), lang)
            } else {
                Text(readString())
            }
        }, {
            if (it.tag == Tag.textWithLanguage) {
                writeShort(stringLength(it.lang!!) + stringLength(it.value))
                writeString(it.lang)
                writeString(it.value)
            } else {
                writeString(it.value)
            }
        })
    }
}
