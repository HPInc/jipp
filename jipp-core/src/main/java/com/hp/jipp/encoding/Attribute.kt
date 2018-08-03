// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.util.BuildError
import com.hp.jipp.util.PrettyPrintable
import com.hp.jipp.util.PrettyPrinter
import com.hp.jipp.util.toHexString
import java.text.SimpleDateFormat
import java.util.* // ktlint-disable

/** Any kind of attribute, having any number of any type of values */
interface Attribute<T : Any> : PrettyPrintable, List<T> {
    /** The name of the attribute. */
    val name: String

    /** The attributes out-of-band tag (only when content is empty). */
    val tag: Tag?

    /** Attribute type used to encode the attribute if known */
    val type: AttributeType<T>

    /** Return values in string form. */
    fun strings(): List<String> = map { if (it is Stringable) it.asString() else it.toString() }

    /** Return a copy of this attribute having a new name */
    fun withName(newName: String): Attribute<T> = object : Attribute<T> by this {
        override val name = newName
    }

    /** True if the tag for this attribute is [Tag.unknown] */
    fun isUnknown() = tag == Tag.unknown

    /** True if the tag for this attribute is [Tag.noValue] */
    fun isNoValue() = tag == Tag.noValue

    /** True if the tag for this attribute is [Tag.unsupported] */
    fun isUnsupported() = tag == Tag.unsupported

    /** Returns the first value in the attribute if present. */
    fun getValue(): T?

    /** Convert all attribute values to their most basic string-like form */
    private fun toStrings() =
        map {
            if (it is Stringable) {
                it.asString()
            } else {
                it.toString()
            }
        }

    override fun print(printer: PrettyPrinter) {
        when (size) {
            0 -> printer.open(PrettyPrinter.SILENT, name)
            1 -> printer.open(PrettyPrinter.KEY_VALUE, "$name =")
            else -> printer.open(PrettyPrinter.ARRAY, "$name =")
        }

        forEach {
            when (it) {
                is PrettyPrintable -> it.print(printer)
                else -> printer.add(toPrintable(it))
            }
        }
        printer.close()
    }

    fun toPrintable(value: T): String = when (value) {
        // Present Calendar as an ISO6801 date string
        is Calendar -> SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").let { format ->
            format.timeZone = value.timeZone
            format.format(value.time)
        }
        is ByteArray -> "0x" + value.toHexString()
        else -> value.toString()
    }

    class Type(override val name: String) : AttributeType<Any> {
        override fun coerce(value: Any): Any? = value
    }
}


open class BaseAttribute<T: Any>(
    override val name: String,
    override val type: AttributeType<T>,
    final override val tag: Tag?,
    val values: List<T>
) : Attribute<T>, List<T> by values {

    constructor(name: String, type: AttributeType<T>, values: List<T>) : this(name, type, null, values)

    constructor(name: String, type: AttributeType<T>, tag: Tag) : this(name, type, tag, emptyList())

    init {
        if (values.isEmpty() && (tag == null || (!tag.isOutOfBand && !tag.isCollection))) {
            throw BuildError("Attribute must have values or an out-of-band tag")
        }
    }

    override fun getValue(): T? = if (values.isEmpty()) null else values[0]

    override fun toString() = if (tag == null) {
        "$name = $values"
    } else {
        "$name($tag)"
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is Attribute<*>) return false
        // Attribute equality only occurs with respect to its name, tag, and value content
        return other.name == name && other.tag == tag && values == other
    }

    override fun hashCode(): Int {
        var code = values.hashCode()
        code = 31 * code + name.hashCode()
        code = 31 * code + (tag?.hashCode() ?: 0)
        return code
    }
}