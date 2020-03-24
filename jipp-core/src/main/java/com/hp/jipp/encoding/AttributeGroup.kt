// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.util.PrettyPrintable
import com.hp.jipp.util.PrettyPrinter
import java.io.IOException

/**
 * A tagged list of attributes. Only one attribute of a given type may appear in the group, although each attribute
 * may contain 0 or more values.
 */
interface AttributeGroup : PrettyPrintable, List<Attribute<*>> {
    val tag: Tag

    /** Return the attribute corresponding to the specified [name]. */
    operator fun get(name: String): Attribute<*>?

    /** Return the attribute as conforming to the supplied attribute type. */
    operator fun <T : Any> get(type: AttributeType<T>): Attribute<T>?

    /** Return all values found having this attribute type. */
    fun <T : Any> getValues(type: AttributeType<T>): List<T> =
        get(type) ?: listOf()

    /** Return the first value of an attribute matching [type]. */
    fun <T : Any> getValue(type: AttributeType<T>): T? =
        get(type)?.firstOrNull()

    /** Return the string form of any values present for this attribute [type]. */
    fun <T : Any> getStrings(type: AttributeType<T>): List<String> =
        get(type)?.strings() ?: listOf()

    /** Return the string form (or null) of the first value present for this attribute [type]. */
    fun <T : Any> getString(type: AttributeType<T>): String? =
        get(type)?.strings()?.firstOrNull()

    override fun print(printer: PrettyPrinter) {
        printer.open(PrettyPrinter.OBJECT, tag.toString())
        printer.addAll(this)
        printer.close()
    }

    /** Return a copy of this attribute group in mutable form. */
    fun toMutable(): MutableAttributeGroup =
        mutableGroupOf(tag, this)

    @Suppress("TooManyFunctions")
    companion object {
        /** Return a fixed group of attributes. */
        @JvmStatic
        fun groupOf(tag: Tag, attributes: List<Attribute<*>>): AttributeGroup =
            AttributeGroupImpl(tag, attributes)

        /** Return a fixed group of attributes. */
        @JvmStatic
        fun groupOf(tag: Tag, vararg attributes: Attribute<*>): AttributeGroup =
            groupOf(tag, attributes.toList())

        /** Return a mutable group of attributes. */
        @JvmStatic
        fun mutableGroupOf(tag: Tag, attributes: List<Attribute<*>>): MutableAttributeGroup =
            MutableAttributeGroup(tag, attributes)

        /** Return a mutable group of attributes. */
        @JvmStatic
        fun mutableGroupOf(tag: Tag, vararg attributes: Attribute<*>): MutableAttributeGroup =
            mutableGroupOf(tag, attributes.toList())

        /**
         * Read an entire attribute group if available in the input stream.
         */
        @JvmStatic
        @Throws(IOException::class)
        @Deprecated("Use IppInputStream.read()",
            ReplaceWith("readAttributeGroup()", "com.hp.jipp.encoding.IppInputStream"))
        fun read(input: IppInputStream, groupTag: Tag) =
            input.readAttributeGroup(groupTag)
    }
}
