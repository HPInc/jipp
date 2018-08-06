// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.util.PrettyPrintable
import com.hp.jipp.util.PrettyPrinter
import com.hp.jipp.util.toHexString
import java.text.SimpleDateFormat
import java.util.* // ktlint-disable

/**
 * An IPP attribute, which is a named list of 0 or more values. Values are parsed into the most natural Java type that
 * can encode the supplied data.
 */
interface Attribute<T : Any> : PrettyPrintable, List<T> {
    /** The name of the attribute. */
    val name: String

    /** An out-of-band tag, present only when there are no values. */
    val tag: Tag?

    /** Attribute type used to encode the attribute, if known. */
    val type: AttributeType<T>?

    /** Return values in string form. */
    fun strings(): List<String> = map { if (it is Stringable) it.asString() else it.toString() }

    /** True if the tag for this attribute is [Tag.unknown] */
    fun isUnknown() = tag == Tag.unknown

    /** True if the tag for this attribute is [Tag.noValue] */
    fun isNoValue() = tag == Tag.noValue

    /** True if the tag for this attribute is [Tag.unsupported] */
    fun isUnsupported() = tag == Tag.unsupported

    /** Returns the first value in the attribute if present. */
    fun getValue(): T?

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

    /** Convert a value to a printable object */
    private fun toPrintable(value: T): String = when (value) {
        // Present Calendar as an ISO6801 date string
        is Calendar -> SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").let { format ->
            format.timeZone = value.timeZone
            format.format(value.time)
        }
        is ByteArray -> "0x" + value.toHexString()
        else -> value.toString()
    }
}
