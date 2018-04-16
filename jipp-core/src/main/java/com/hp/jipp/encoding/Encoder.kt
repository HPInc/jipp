// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.util.ParseError
import java.io.IOException

/** Reads/writes attribute values */
abstract class Encoder<T> {

    /** Return a human-readable name describing this type */
    abstract val typeName: String

    /** Read a single value from the input stream, making use of the set of encoders */
    @Throws(IOException::class)
    abstract fun readValue(input: IppInputStream, finder: Finder, valueTag: Tag): T

    /** Write a single value to the output stream */
    @Throws(IOException::class)
    abstract fun writeValue(out: IppOutputStream, value: T)

    /** Return true if this tag can be handled by this encoder */
    abstract fun valid(valueTag: Tag): Boolean

    /** An object that can look up the appropriate encoder based on a tag/name pair */
    interface Finder {
        /** For a given tag and attribute name, return the correct [Encoder] */
        @Throws(IOException::class)
        fun find(valueTag: Tag, name: String): Encoder<*>
    }

    companion object {
        /** Return a finder for the given attributeTypes and encoders */
        @JvmStatic
        fun finderOf(
                attributeTypes: Map<String, AttributeType<*>>,
                encoders: List<Encoder<*>>
        ): Encoder.Finder {
            return object : Encoder.Finder {
                @Throws(IOException::class)
                override fun find(valueTag: Tag, name: String): Encoder<*> {
                    // Check for a matching attribute type
                    val attributeType = attributeTypes[name]

                    return if (attributeType != null && attributeType.encoder.valid(valueTag)) {
                        attributeType.encoder
                    } else {
                        // If no valid match above then try with each default encoder
                        encoders.find { it.valid(valueTag) }
                                ?: throw ParseError("No encoder found for $name($valueTag)")
                    }
                }
            }
        }
    }
}
