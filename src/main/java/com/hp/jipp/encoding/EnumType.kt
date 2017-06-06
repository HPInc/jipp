package com.hp.jipp.encoding

import com.hp.jipp.util.Reflect

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

/** An IPP enum type */
open class EnumType<T : Enum>(val enumEncoder: EnumType.Encoder<T>, name: String) :
        AttributeType<T>(enumEncoder, Tag.EnumValue, name) {

    /** An encoder for Enums. */
    data class Encoder<T : Enum>(override val type: String, val map: Map<Int, T>,
                                 val factory: (name: String, code: Int) -> T) : com.hp.jipp.encoding.Encoder<T>() {

        constructor(name: String, enums: Collection<T>, factory: (name: String, code: Int) -> T):
                this(name, Enum.toCodeMap(enums), factory)

        constructor(cls: Class<T>, factory: (name: String, code: Int) -> T):
                this(cls.simpleName, Reflect.getStaticObjects(cls)
                        .filter { cls.isAssignableFrom(it.javaClass)}
                        .map {
                            @Suppress("UNCHECKED_CAST")
                            it as T
                        }, factory)

        constructor(cls: Class<T>, factory: com.hp.jipp.encoding.Enum.Factory<T>):
                this(cls, { name: String, code: Int -> factory.of(name, code) })

        /** Returns a known enum, or creates a new instance if not found  */
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

        override fun valid(valueTag: Tag): Boolean = valueTag == Tag.EnumValue
    }

    override fun of(attribute: Attribute<*>): Attribute<T>? =
        if (attribute.valueTag !== Tag.EnumValue) null
        else of(attribute.values.filter { it is Int }
                .map { enumEncoder[it as Int] } )
}
