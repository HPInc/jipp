package com.hp.jipp.encoding

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

/**
 * Reads/writes attribute values
 */
abstract class Encoder<T> {

    /** Return a human-readable name describing this type */
    abstract val type: String

    /** Read a single value from the input stream, making use of the set of encoders */
    @Throws(IOException::class)
    abstract fun readValue(input: DataInputStream, finder: Finder, valueTag: Tag): T

    /** Write a single value to the output stream */
    @Throws(IOException::class)
    abstract fun writeValue(out: DataOutputStream, value: T)

    /** Return true if this tag can be handled by this encoder */
    abstract fun valid(valueTag: Tag): Boolean

    /** Read an attribute and its values from the data stream */
    @Throws(IOException::class)
    fun read(input: DataInputStream, finder: Finder, valueTag: Tag, name: String): Attribute<T> {
        val all = listOf(readValue(input, finder, valueTag)) +
                Attribute.generateList { readAdditionalValue(input, valueTag, finder) }
        return Attribute(valueTag, name, all, this)
    }

    /** Read a single additional value if possible  */
    @Throws(IOException::class)
    private fun readAdditionalValue(input: DataInputStream, valueTag: Tag, finder: Finder): T? {
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

    interface Finder {
        @Throws(IOException::class)
        fun find(valueTag: Tag, name: String): Encoder<*>
    }
}
