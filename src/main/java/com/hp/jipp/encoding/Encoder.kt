// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.util.toSequence
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

/** Reads/writes attribute values */
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

    /** An object that can look up the appropriate encoder based on a tag/name pair */
    interface Finder {
        /** For a given tag and attribute name, return the correct [Encoder] */
        @Throws(IOException::class)
        fun find(valueTag: Tag, name: String): Encoder<*>
    }

    companion object {
        internal const val LENGTH_LEN: Int = 2
        internal const val TAG_LEN: Int = 2
        internal const val INT_LEN = 4
    }
}

/** Write a value to this [DataOutputStream] using the supplied [Encoder] */
@Throws(IOException::class)
fun <T> DataOutputStream.writeValue(encoder: Encoder<T>, value: T) {
    encoder.writeValue(this, value)
}

/** Read an [Attribute] from a [DataInputStream] */
@Throws(IOException::class)
fun <T> DataInputStream.readAttribute(encoder: Encoder<T>, finder: Encoder.Finder, valueTag: Tag, name: String):
        Attribute<T> {
    val all = listOf(encoder.readValue(this, finder, valueTag)) +
            { readAdditionalValue(encoder, valueTag, finder) }.toSequence()
    return Attribute(valueTag, name, all, encoder)
}

// Read a single additional value, if possible
@Throws(IOException::class)
private fun <T> DataInputStream.readAdditionalValue(encoder: Encoder<T>, valueTag: Tag, finder: Encoder.Finder): T? {
    // We need to look ahead so mark maximum amount
    if (available() < Encoder.TAG_LEN + Encoder.LENGTH_LEN) return null
    mark(Encoder.TAG_LEN + Encoder.LENGTH_LEN)

    return if (Tag.read(this) == valueTag && readShort().toInt() == 0) {
        // Tag matches and no name, so this is an additional value
        encoder.readValue(this, finder, valueTag)
    } else {
        // NOT an additional value so reset stream and return null
        reset()
        null
    }
}
