package pclm

import util.ByteWindow
import java.math.BigDecimal

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
