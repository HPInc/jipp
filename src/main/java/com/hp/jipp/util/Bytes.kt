package com.hp.jipp.util

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object Bytes {
    private val BUFFER_SIZE = 12 * 1024

    @JvmStatic @Throws(IOException::class)
    fun copy(input: InputStream, output: OutputStream): Long {
        val buffer = ByteArray(BUFFER_SIZE)
        var size: Long = 0
        while (true) {
            val readSize = input.read(buffer)
            if (readSize == -1) break
            output.write(buffer, 0, readSize)
            size += readSize.toLong()
        }
        return size
    }

    @JvmStatic @Throws(IOException::class)
    fun read(file: File): ByteArray {
        val out = ByteArrayOutputStream()
        Bytes.copy(FileInputStream(file), out)
        return out.toByteArray()
    }

    /** Return a byte in hex form */
    @JvmStatic
    fun Byte.toHexString(): String {
        val hexChars = arrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')
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
