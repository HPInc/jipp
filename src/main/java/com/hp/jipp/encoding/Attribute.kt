package com.hp.jipp.encoding

import com.google.common.base.Function
import com.google.common.base.Optional
import com.google.common.collect.ImmutableList
import com.google.common.collect.Lists
import com.google.common.io.BaseEncoding
import com.hp.jipp.util.BuildError
import com.hp.jipp.util.Hook
import com.hp.jipp.util.ParseError
import com.hp.jipp.util.Pretty
import com.hp.jipp.util.Util

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.URI

/**
 * An IPP attribute, composed of a one-byte "value tag" suggesting its type, a human-readable string name, and one or
 * more values according to its type.
 *
 * @param valueTag must be valid for the attribute type, according to the encoder.
 */
data class Attribute<T>(val valueTag: Tag, val name: String, val values: List<T>,
        val encoder: BaseEncoder<T>) : Pretty.Printable {

    init {
        if (!(encoder.valid(valueTag) || Hook.`is`(HOOK_ALLOW_BUILD_INVALID_TAGS))) {
            throw BuildError("Invalid " + valueTag.toString() + " for " + encoder.type)
        }
    }

    interface EncoderFinder {
        @Throws(IOException::class)
        fun find(valueTag: Tag, name: String): BaseEncoder<*>
    }

    abstract class SimpleEncoder<T>(override val type: String) : BaseEncoder<T>() {

        /** Read a single value from the input stream, making use of the set of encoders  */
        @Throws(IOException::class)
        abstract fun readValue(`in`: DataInputStream, valueTag: Tag): T

        @Throws(IOException::class)
        override fun readValue(`in`: DataInputStream, finder: Attribute.EncoderFinder, valueTag: Tag): T {
            return readValue(`in`, valueTag)
        }
    }

    /**
     * Reads/writes attributes to the attribute's type.
     */
    abstract class BaseEncoder<T> {

        /** Return a human-readable name describing this type  */
        abstract val type: String

        /** Read a single value from the input stream, making use of the set of encoders  */
        @Throws(IOException::class)
        abstract fun readValue(`in`: DataInputStream, finder: Attribute.EncoderFinder, valueTag: Tag): T

        /** Write a single value to the output stream  */
        @Throws(IOException::class)
        abstract fun writeValue(out: DataOutputStream, value: T)

        /** Return true if this tag can be handled by this encoder  */
        abstract fun valid(valueTag: Tag): Boolean

        /** Read an attribute and its values from the data stream  */
        @Throws(IOException::class)
        fun read(`in`: DataInputStream, finder: Attribute.EncoderFinder, valueTag: Tag, name: String): Attribute<T> {
            val all = mutableListOf(readValue(`in`, finder, valueTag))
            var next = readAdditionalValue(`in`, valueTag, finder)
            while (next != null) {
                all.add(next)
                next = readAdditionalValue(`in`, valueTag, finder)
            }
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

    /** Return the n'th value in this attribute, assuming it is present  */
    fun getValue(n: Int): T {
        return values[n]
    }

    /** Return a copy of this attribute with a different name  */
    fun withName(newName: String): Attribute<T> = copy(name = newName)

    /** Write this attribute (including all of its values) to the output stream  */
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

    /** Write value tag and name components of an attribute  */
    @Throws(IOException::class)
    private fun writeHeader(out: DataOutputStream, valueTag: Tag, name: String) {
        valueTag.write(out)
        out.writeShort(name.length)
        out.write(name.toByteArray(charset(Util.UTF8)))
    }

    override fun print(printer: Pretty.Printer) {
        val prefix = (if (name == "") "" else name) + "(" + valueTag + "):"
        if (values.size == 1) {
            printer.open(Pretty.KEY_VALUE, prefix)
        } else {
            printer.open(Pretty.ARRAY, prefix)
        }

        for (value in values) {
            if (value is String) {
                printer.add("\"" + value + "\"")
            } else if (value is ByteArray) {
                printer.add("x" + BaseEncoding.base16().encode(value))
            } else if (value is Pretty.Printable) {
                value.print(printer)
            } else {
                printer.add(value.toString())
            }
        }
        printer.close()
    }

    override fun toString(): String {
        val values = Lists.transform(values, Function<T, String> { input ->
            if (input is String || input is URI) {
                return@Function "\"" + input + "\""
            } else if (input is ByteArray) {
                return@Function "x" + BaseEncoding.base16().encode((input as ByteArray?)!!)
            }
            input!!.toString()
        })

        val valueString: String
        if (values.size == 1) {
            valueString = values[0]
        } else {
            valueString = values.toString()
        }
        return (if (name == "") "" else name) +
                "(" + valueTag + ")" +
                ": " + valueString
    }

    companion object {

        /** Set to false in [Hook] to disable builders that accept invalid tags.  */
        val HOOK_ALLOW_BUILD_INVALID_TAGS = Attribute::class.java.name + ".HOOK_ALLOW_BUILD_INVALID_TAGS"

        /**
         * Read an attribute from an input stream, based on its tag
         */
        @Throws(IOException::class)
        @JvmStatic
        fun read(`in`: DataInputStream, finder: EncoderFinder, valueTag: Tag): Attribute<*> {
            val name = String(readValueBytes(`in`))
            return finder.find(valueTag, name).read(`in`, finder, valueTag, name)
        }

        /** Write a length-value tuple  */
        // TODO: Consider moving these to AttributeType
        @Throws(IOException::class)
        @JvmStatic
        fun writeValueBytes(out: DataOutputStream, bytes: ByteArray) {
            out.writeShort(bytes.size)
            out.write(bytes)
        }

        /** Skip (discard) a length-value pair  */
        @Throws(IOException::class)
        @JvmStatic
        fun skipValueBytes(`in`: DataInputStream) {
            val valueLength = `in`.readShort().toInt()
            if (valueLength.toLong() != `in`.skip(valueLength.toLong())) throw ParseError("Value too short")
        }

        /** Read and return value bytes from a length-value pair  */
        @Throws(IOException::class)
        @JvmStatic
        fun readValueBytes(`in`: DataInputStream): ByteArray {
            val valueLength = `in`.readShort().toInt()
            val valueBytes = ByteArray(valueLength)
            if (valueLength > 0) {
                val actual = `in`.read(valueBytes)
                if (valueLength > actual) {
                    throw ParseError("Value too short: expected " + valueBytes.size + " but got " + actual)
                }
            }
            return valueBytes
        }

        /** Reads a two-byte length field, asserting that it is of a specific length  */
        @Throws(IOException::class)
        @JvmStatic
        fun expectLength(`in`: DataInputStream, length: Int) {
            val readLength = `in`.readShort().toInt()
            if (readLength != length) {
                throw ParseError("Bad attribute length: expected $length, got $readLength")
            }
        }
    }
}
