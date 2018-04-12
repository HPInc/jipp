// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.util.BuildError
import com.hp.jipp.util.PrettyPrintable
import com.hp.jipp.util.PrettyPrinter
import com.hp.jipp.util.toHexString

import java.io.DataOutputStream
import java.io.IOException

/**
 * An IPP attribute, composed of a one-byte "value tag" suggesting its type, a human-readable string name, and one or
 * more values according to its type.
 *
 * @param valueTag must be valid for the attribute type, according to the encoder.
 */
data class Attribute<T>(val valueTag: Tag, val name: String, val values: List<T>, val encoder: Encoder<T>) :
        PrettyPrintable {

    init {
        if (!(encoder.valid(valueTag))) {
            throw BuildError("Invalid $valueTag for ${encoder.type}")
        }
    }

    /** Return the n'th value in this attribute, assuming it is present */
    fun getValue(n: Int): T {
        return values[n]
    }

    /** Return a copy of this attribute with a different name */
    fun withName(newName: String): Attribute<T> = copy(name = newName)

    override fun print(printer: PrettyPrinter) {
        val prefix = "$name($valueTag)"
        if (values.size == 1) {
            printer.open(PrettyPrinter.KEY_VALUE, prefix)
        } else {
            printer.open(PrettyPrinter.ARRAY, prefix)
        }

        values.forEach {
            when (it) {
                is PrettyPrintable -> it.print(printer)
                else -> printer.add(toPrintable(it))
            }
        }
        printer.close()
    }

    override fun toString(): String {
        val stringValues = values.map { toPrintable(it) }
        val valueString = if (stringValues.size == 1) stringValues[0] else stringValues.toString()
        return "$name($valueTag): $valueString"
    }

    private fun toPrintable(value: T): String = when (value) {
        is String -> "\"$value\""
        is ByteArray -> "x" + value.toHexString()
        else -> value.toString()
    }
}

/** Write this attribute (including all of its values) to the output stream */
@Throws(IOException::class)
fun <T> DataOutputStream.writeAttribute(attribute: Attribute<T>) {
    writeHeader(attribute)
    if (attribute.values.isEmpty()) {
        writeShort(0)
    } else {
        writeValue(attribute.encoder, attribute.values[0])
    }

    attribute.values.drop(1).forEach {
        writeHeader(attribute, name = "")
        writeValue(attribute.encoder, it)
    }
}

// Write ONLY the value tag + name components of an attribute
@Throws(IOException::class)
private fun <T> DataOutputStream.writeHeader(attribute: Attribute<T>, name: String = attribute.name) {
    writeTag(attribute.valueTag)
    writeString(name)
}
