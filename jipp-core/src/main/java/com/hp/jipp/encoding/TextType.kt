// Copyright 2017 - 2021 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/**
 * An [AttributeType] for a [Text] value.
 *
 * See [RFC8011 Section 5.1.2](https://tools.ietf.org/html/rfc8011#section-5.1.2).
 */
open class TextType(override val name: String) : AttributeTypeImpl<Text>(name, Text::class.java) {
    /** An [AttributeType] for multiple [Text] values. */
    class Set(name: String) : TextType(name), AttributeSetType<Text> {
        /** Return an [Attribute] containing [Text] of values given (without language). */
        fun of(value: String, vararg values: String) = of((listOf(value) + values).map { Text(it) })

        /** Return an [Attribute] containing [Text] of values given (without language). */
        fun ofStrings(values: Iterable<String>) = of(values.map { Text(it) })

        override fun toString() = "TextType.Set($name)"
    }

    /** Return an [Attribute] of a [Text] from [value] (without language). */
    fun of(value: String) = of(Text(value))

    companion object {
        val codec = Codec(
            { it == Tag.textWithLanguage || it == Tag.textWithoutLanguage },
            {
                if (it == Tag.textWithLanguage) {
                    readShort()
                    val lang = readString() // Lang comes first
                    Text(readString(), lang)
                } else {
                    Text(readString())
                }
            },
            {
                if (it.tag == Tag.textWithLanguage) {
                    writeShort(IppStreams.stringLength(it.lang!!) + IppStreams.stringLength(it.value))
                    writeStringValue(it.lang)
                    writeStringValue(it.value)
                } else {
                    writeStringValue(it.value)
                }
            }
        )
    }
}
