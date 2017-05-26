package com.hp.jipp.encoding

import com.hp.jipp.util.BuildError
import com.hp.jipp.util.Hook
import com.hp.jipp.util.Pretty

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

import com.hp.jipp.util.HexStrings

/**
 * An IPP attribute, composed of a one-byte "value tag" suggesting its type, a human-readable string name, and one or
 * more values according to its type.
 *
 * @param valueTag must be valid for the attribute type, according to the encoder.
 */
data class Attribute<T>(val valueTag: Tag, val name: String, val values: List<T>, val encoder: BaseEncoder<T>) :
        Pretty.Printable, HexStrings {

    init {
        if (!(encoder.valid(valueTag) || Hook.`is`(HOOK_ALLOW_BUILD_INVALID_TAGS))) {
            throw BuildError("Invalid $valueTag for ${encoder.type}")
        }
    }

    interface EncoderFinder {
        @Throws(IOException::class)
        fun find(valueTag: Tag, name: String): BaseEncoder<*>
    }

    abstract class SimpleEncoder<T>(override val type: String) : BaseEncoder<T>() {
        /** Read a single value from the input stream, making use of the set of encoders */
        @Throws(IOException::class)
        abstract fun readValue(input: DataInputStream, valueTag: Tag): T

        @Throws(IOException::class)
        override fun readValue(input: DataInputStream, finder: Attribute.EncoderFinder, valueTag: Tag): T {
            return readValue(input, valueTag)
        }
    }

    /**
     * Reads/writes attributes to the attribute's type.
     */
    abstract class BaseEncoder<T> {

        /** Return a human-readable name describing this type */
        abstract val type: String

        /** Read a single value from the input stream, making use of the set of encoders */
        @Throws(IOException::class)
        abstract fun readValue(input: DataInputStream, finder: Attribute.EncoderFinder, valueTag: Tag): T

        /** Write a single value to the output stream */
        @Throws(IOException::class)
        abstract fun writeValue(out: DataOutputStream, value: T)

        /** Return true if this tag can be handled by this encoder */
        abstract fun valid(valueTag: Tag): Boolean

        /** Read an attribute and its values from the data stream */
        @Throws(IOException::class)
        fun read(input: DataInputStream, finder: Attribute.EncoderFinder, valueTag: Tag, name: String): Attribute<T> {
            val all = listOf(readValue(input, finder, valueTag)) +
                    generateList { readAdditionalValue(input, valueTag, finder) }
            return Attribute(valueTag, name, all, this)
        }

        /** Read a single additional value if possible  */
        @Throws(IOException::class)
        private fun readAdditionalValue(input: DataInputStream, valueTag: Tag, finder: Attribute.EncoderFinder): T? {
            if (input.available() < 3) return null
            input.mark(3)
            if (Tag.read(input) === valueTag) {
                val nameLength = input.readShort().toInt()
                if (nameLength == 0) {
                    return readValue(input, finder, valueTag)
                }
            }
            // Failed to parse an additional value so back up and quit.
            input.reset()
            return null
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

    override fun print(printer: Pretty.Printer) {
        val prefix = "$name($valueTag)"
        if (values.size == 1) {
            printer.open(Pretty.KEY_VALUE, prefix)
        } else {
            printer.open(Pretty.ARRAY, prefix)
        }

        for (value in values) {
            when(value) {
                is String -> printer.add("\"" + value + "\"")
                is ByteArray -> printer.add("x" + value.toHexString())
                is Pretty.Printable -> value.print(printer)
                else -> printer.add(value.toString())
            }
        }
        printer.close()
    }

    override fun toString(): String {
        val stringValues = values.map {
            when (it) {
                is String -> "\"$it\""
                is ByteArray -> "x" + it.toHexString()
                else -> it.toString()
            }
        }

        val valueString = if (stringValues.size == 1) stringValues[0] else stringValues.toString()
        return "$name($valueTag): $valueString"
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
        fun read(input: DataInputStream, finder: EncoderFinder, valueTag: Tag): Attribute<*> {
            val name = String(input.readValueBytes())
            return finder.find(valueTag, name).read(input, finder, valueTag, name)
        }

        // TODO: remove when no longer needed from Java code
        @JvmStatic fun readValueBytes2(input: DataInputStream) = input.readValueBytes()
    }
}
