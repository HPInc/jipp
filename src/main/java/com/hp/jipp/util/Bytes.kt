package com.hp.jipp.util

// Used to convert bytes to hex
private val hexChars = "0123456789abcdef".toCharArray()

/** Return a byte in hex form */
fun Byte.toHexString(): String {
    val i = toInt()
    val char1 = hexChars[i shr 4 and 0x0f]
    val char2 = hexChars[i and 0x0f]
    return "$char1$char2"
}

/** Return a byte array in hex form */
fun ByteArray.toHexString(): String = joinToString(separator = "") { it.toHexString() }
