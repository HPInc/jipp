package com.hp.jipp.encoding

import com.hp.jipp.util.BuildError
import com.hp.jipp.util.Bytes
import com.hp.jipp.util.Hook
import com.hp.jipp.util.PrettyPrinter

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

/**
 * An IPP attribute, composed of a one-byte "value tag" suggesting its type, a human-readable string name, and one or
 * more values according to its type.
 *
 * @param valueTag must be valid for the attribute type, according to the encoder.
 */
data class Attribute<T>(val valueTag: Tag, val name: String, val values: List<T>, val encoder: Encoder<T>) :
        PrettyPrinter.Printable {

    init {
        if (!(encoder.valid(valueTag) || Hook.`is`(HOOK_ALLOW_BUILD_INVALID_TAGS))) {
            throw BuildError("Invalid $valueTag for ${encoder.type}")
        }
    }

    /** Return the n'th value in this attribute, assuming it is present */
    fun getValue(n: Int): T {
        return values[n]
    }

    /** Return a copy of this attribute with a different name */
    fun withName(newName: String): Attribute<T> = copy(name = newName)

    /** Write this attribute (including all of its values) to the output stream */
    @Throws(IOException::class)
    fun write(out: DataOutputStream) {
        writeHeader(out, valueTag, name)
        if (values.isEmpty()) {
            out.writeShort(0)
            return
        }

        encoder.writeValue(out, getValue(0))
        for (i in 1..values.size - 1) {
            writeHeader(out, valueTag, "")
            encoder.writeValue(out, values[i])
        }
    }

    /** Write value tag and name components of an attribute */
    @Throws(IOException::class)
    private fun writeHeader(out: DataOutputStream, valueTag: Tag, name: String) {
        valueTag.write(out)
        out.writeString(name)
    }

    override fun print(printer: PrettyPrinter) {
        val prefix = "$name($valueTag)"
        if (values.size == 1) {
            printer.open(PrettyPrinter.KEY_VALUE, prefix)
        } else {
            printer.open(PrettyPrinter.ARRAY, prefix)
        }

        values.forEach {
            when(it) {
                is PrettyPrinter.Printable -> it.print(printer)
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
        is ByteArray -> "x" + Bytes.toHexString(value)
        else -> value.toString()
    }

    companion object {
        /** Return a list from all returns from a generator until null */
        fun <T> generateList(generator: () -> T?): List<T> {
            val items = ArrayList<T>()
            var value: T? = generator()
            while (value != null) {
                items.add(value)
                value = generator()
            }
            return items
        }

        /** Set to false in [Hook] to disable builders that accept invalid tags.  */
        val HOOK_ALLOW_BUILD_INVALID_TAGS = Attribute::class.java.name + ".HOOK_ALLOW_BUILD_INVALID_TAGS"

        /**
         * Read an attribute from an input stream, based on its tag
         */
        @Throws(IOException::class)
        @JvmStatic
        fun read(input: DataInputStream, finder: Encoder.Finder, valueTag: Tag): Attribute<*> {
            val name = String(input.readValueBytes())
            return finder.find(valueTag, name).read(input, finder, valueTag, name)
        }
    }
}
