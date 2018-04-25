package pclm

import java.math.BigDecimal
import java.util.Arrays

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
    private fun toString(max: Int): String {
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

/** Parent class for all PCLM data types */
interface PclmData {
    fun toArray() = this as PclmArray
    fun toRef() = this as PclmObjectRef
    fun toNumber() = this as PclmNumber
    fun toName() = this as PclmName
    fun toDictionary() = this as PclmDictionary
}

/** A mapping of names to PdfData objects */
data class PclmDictionary(val items: List<Pair<String, PclmData>>) : PclmData {
    /** Return the first item whose key matches the label, or throw */
    operator fun get(label: String) = items.first { it.first == label }.second

    override fun toString() = items.joinToString(", ") { "/${it.first} ${it.second}" }
}

/** DSL for defining a PCLm dictionary */
fun pclmDictionary(block: PclmDictionaryContext.() -> Unit) =
        PclmDictionaryContext().apply { block() }.build()

/** Context in which PCLM dictionary pairs are added */
open class PclmDictionaryContext {
    private val pairs = ArrayList<Pair<String, PclmData>>()
    fun build() = PclmDictionary(pairs.toList())

    /** Add a Name/Value pair to the dictionary */
    fun add(pair: Pair<String, PclmData>) = pairs.add(pair)
}

/** An array of [PclmData] objects */
data class PclmArray(val items: List<PclmData>) : PclmData

/** DSL for defining a PCLm array */
fun pclmArray(block: PclmArrayContext.() -> Unit) =
        PclmArrayContext().apply { block() }.build()

class PclmArrayContext {
    private val items = ArrayList<PclmData>()
    fun build() = PclmArray(items.toList())
    /** Add a Name/Data pair to the dictionary */
    infix fun add(data: PclmData) = items.add(data)
}

data class PclmName(val name: String) : PclmData {
    override fun toString() = "/$name"
}

data class PclmNumber(val value: BigDecimal) : PclmData {
    constructor(value: Int) : this(value.toBigDecimal())
    override fun toString() = "$value"
}

data class PclmObjectRef(val number: Int) : PclmData {
    override fun toString() = "$number 0 R"
}

data class PclmObject(
    val number: Int,
    private val dictionary: PclmDictionary,
    val stream: ByteWindow? = null
) : PclmData {
    /** Return the first dictionary item whose key matches the label, or throw */
    operator fun get(label: String) = dictionary[label]
}

/** The newline character (0x0A), also known as LF (line feed) */
const val NEWLINE = 0x0A.toByte()
