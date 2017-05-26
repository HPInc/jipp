package com.hp.jipp.encoding

import java.io.DataInputStream
import java.io.IOException

/** An encoder for simple values */
abstract class SimpleEncoder<T>(override val type: String) : Encoder<T>() {
    /** Read a single value from the input stream, making use of the set of encoders */
    @Throws(IOException::class)
    abstract fun readValue(input: DataInputStream, valueTag: Tag): T

    @Throws(IOException::class)
    override fun readValue(input: DataInputStream, finder: Finder, valueTag: Tag): T {
        return readValue(input, valueTag)
    }
}
