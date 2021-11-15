// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT
//
// DO NOT MODIFY. Code is auto-generated by genTypes.py. Content taken from registry at
// https://www.iana.org/assignments/ipp-registrations/ipp-registrations.xml, updated on 2021-10-14
@file:Suppress("MaxLineLength", "WildcardImport")

package com.hp.jipp.model

import com.hp.jipp.encoding.* // ktlint-disable
import com.hp.jipp.encoding.Enum // Override java Enum

/**
 * "print-quality" enum as defined in:
 * [RFC8011](http://www.iana.org/go/rfc8011).
 */
data class PrintQuality(override val code: Int, override val name: String) : Enum() {

    override fun toString() = super.toString()

    /** An [AttributeType] for a [PrintQuality] attribute. */
    class Type(name: String) : EnumType<PrintQuality>(name, { get(it) })

    /** An [AttributeType] for multiple [PrintQuality] attributes. */
    class SetType(name: String) : EnumType.Set<PrintQuality>(name, { get(it) })

    object Code {
        const val draft = 3
        const val normal = 4
        const val high = 5
    }

    companion object {
        @JvmField val draft = PrintQuality(Code.draft, "draft")
        @JvmField val normal = PrintQuality(Code.normal, "normal")
        @JvmField val high = PrintQuality(Code.high, "high")

        @JvmField val all = listOf(
            draft,
            normal,
            high,
        ).map { it.code to it }.toMap()

        operator fun get(value: Int): PrintQuality =
            all[value] ?: PrintQuality(value, "???")
    }
}
