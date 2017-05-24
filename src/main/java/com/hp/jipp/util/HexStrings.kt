package com.hp.jipp.util

interface HexStrings {

    /** Return a byte in hex form */
    fun Byte.toHexString(): String {
        val hexChars = arrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')
        val i = this.toInt()
        val char1 = hexChars[i shr 4 and 0x0f]
        val char2 = hexChars[i and 0x0f]
        return "$char1$char2"
    }

    /** Return a byte array in hex form */
    fun ByteArray.toHexString(): String {
        val builder = StringBuilder()
        for (b in this) {
            builder.append(b.toHexString())
        }
        return builder.toString()
    }
}