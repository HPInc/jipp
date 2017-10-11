package com.hp.jipp.encoding

import com.hp.jipp.util.getStaticObjects
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

/** An IPP enum type */
open class EnumType<T : Enum>(val enumEncoder: EnumType.Encoder<T>, name: String) :
        AttributeType<T>(enumEncoder, Tag.enumValue, name) {

    /**
     * An [Encoder] for [Enum] values
     * @param type Human-readable type of the [Enum]
     * @param map predefined [Enum] instances to reuse when decoding
     * @param factory a way to create new [Enum] instances of the correct type when decoding an undefined value
     */
    data class Encoder<T : Enum>(override val type: String, val map: Map<Int, T>,
                                 val factory: (name: String, code: Int) -> T) : com.hp.jipp.encoding.Encoder<T>() {

        constructor(name: String, enums: Collection<T>, factory: (name: String, code: Int) -> T):
                this(name, Enum.toCodeMap(enums), factory)

        /** Returns a known [Enum], or creates a new instance from factory if not found  */
        operator fun get(code: Int): T =
            map[code] ?: factory("$type(x${Integer.toHexString(code)})", code)

        @Throws(IOException::class)
        override fun readValue(input: DataInputStream, finder: Finder, valueTag: Tag): T {
            return get(IntegerType.ENCODER.readValue(input, valueTag))
        }

        @Throws(IOException::class)
        override fun writeValue(out: DataOutputStream, value: T) {
            IntegerType.ENCODER.writeValue(out, value.code)
        }

        override fun valid(valueTag: Tag): Boolean = valueTag == Tag.enumValue
    }

    override fun of(attribute: Attribute<*>): Attribute<T>? =
        if (attribute.valueTag !== Tag.enumValue) null
        else of(attribute.values
                .filter { it is Int }
                .map { enumEncoder[it as Int] })
}

/**
 * Create an [EnumType] encoder for a subclass of Enum. All public static instances of the class
 * will be included as potential values for decoding purposes.
 */
fun <T : Enum> encoderOf(cls: Class<T>, factory: (name: String, code: Int) -> T) =
        cls.run {
            EnumType.Encoder(simpleName, getStaticObjects()
                    .filter { isAssignableFrom(it.javaClass) }
                    .map {
                        @Suppress("UNCHECKED_CAST")
                        it as T
                    }, factory)
        }

/**
 * Create an [EnumType] encoder for a subclass of Enum. All public static instances of the class
 * will be included as potential values for decoding purposes.
 */
fun <T : Enum> encoderOf(cls: Class<T>, factory: Enum.Factory<T>) =
        encoderOf(cls) { name, code -> factory.of(name, code) }
