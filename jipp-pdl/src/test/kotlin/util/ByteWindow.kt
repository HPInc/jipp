package util

import java.util.* // ktlint-disable no-wildcard-imports

/**
 * Represents a section of bytes in an array
 */
data class ByteWindow(val array: ByteArray, val offset: Int, val length: Int) {
    constructor(array: ByteArray) : this(array, 0, array.size)

    /**
     * Return a new [ByteWindow] excluding the specified number of bytes from the front
     */
    fun drop(toConsume: Int) = copy(offset = offset + toConsume, length = length - toConsume)

    /**
     * Return a printable string which approximately represents the contents of this object, showing up to the
     * first [max] bytes.
     */
    fun toString(max: Int): String {
        val bigger = length > max
        val bytes = (offset until (offset + Math.min(max, length))).joinToString(" ") {
            String.format("%02X", array[it])
        } + if (bigger) "..." else ""
        val ascii = String(array, offset, Math.min(max, length)).replace("[^\\x20-\\x7E]".toRegex(), ".")
        return "Bytes($offset, $length) \"$ascii\" [ $bytes ]"
    }

    /**
     * Represent all of the content in this object as a [String]
     */
    fun asString() = String(array, offset, length)

    override fun toString() = toString(16)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ByteWindow

        if (!Arrays.equals(array, other.array)) return false
        if (offset != other.offset) return false
        if (length != other.length) return false

        return true
    }

    override fun hashCode(): Int {
        var result = Arrays.hashCode(array)
        result = 31 * result + offset
        result = 31 * result + length
        return result
    }
}