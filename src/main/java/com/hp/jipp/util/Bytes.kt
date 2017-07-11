package com.hp.jipp.util

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/** Utilities for dealing with arrays and streams of bytes */
object Bytes {
    private val BUFFER_SIZE = 12 * 1024

    /**
     * Copies all data from the input stream into an output stream until the input stream has no more data, closing
     * both streams.
     */
    @JvmStatic @Throws(IOException::class)
    fun copy(input: InputStream, output: OutputStream): Long {
        try {
            val buffer = ByteArray(BUFFER_SIZE)
            var size: Long = 0
            while (true) {
                val readSize = input.read(buffer)
                if (readSize == -1) break
                output.write(buffer, 0, readSize)
                size += readSize.toLong()
            }
            return size
        } finally {
            try { input.close() } catch (ignored: IOException) { }
            try { output.close() } catch (ignored: IOException) { }
        }
    }

    /** Reads the entire content of a file into a single ByteArray */
    @JvmStatic @Throws(IOException::class)
    fun read(file: File): ByteArray {
        val out = ByteArrayOutputStream()
        copy(FileInputStream(file), out)
        return out.toByteArray()
    }

    /** Used to convert bytes to hex */
    private val hexChars = arrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

    /** Return a byte in hex form */
    @JvmStatic
    fun Byte.toHexString(): String {
        val i = this.toInt()
        val char1 = hexChars[i shr 4 and 0x0f]
        val char2 = hexChars[i and 0x0f]
        return "$char1$char2"
    }

    /** Return a byte array in hex form */
    @JvmStatic
    fun toHexString(bytes: ByteArray): String {
        val builder = StringBuilder()
        for (b in bytes) {
            builder.append(b.toHexString())
        }
        return builder.toString()
    }
}
