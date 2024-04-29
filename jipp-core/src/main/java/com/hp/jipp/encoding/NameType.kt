// Copyright 2017 - 2021 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/**
 * An [AttributeType] for a [Name] value.
 *
 * See [RFC8011 Section 5.1.3](https://tools.ietf.org/html/rfc8011#section-5.1.3).
 */
open class NameType(name: String) : AttributeTypeImpl<Name>(name, Name::class.java) {
    /** An [AttributeType] for multiple [Name] values. */
    class Set(name: String) : NameType(name), AttributeSetType<Name> {
        /** Return an [Attribute] containing [Name] values from values given (without language). */
        fun of(value: String, vararg values: String) = of((listOf(value) + values).map { Name(it) })

        /** Return an [Attribute] containing [Name] values from values given (without language). */
        fun ofStrings(values: Iterable<String>) = of(values.map { Name(it) })

        override fun toString() = "NameType.Set($name)"
    }

    /** Return an [Attribute] of this type. */
    fun of(value: String) = of(Name(value))

    companion object {
        val codec = Codec(
            { it == Tag.nameWithLanguage || it == Tag.nameWithoutLanguage },
            {
                if (it == Tag.nameWithLanguage) {
                    readShort()
                    val lang = readString() // Lang comes first
                    Name(readString(), lang)
                } else {
                    Name(readString())
                }
            },
            {
                if (it.tag == Tag.nameWithLanguage) {
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
