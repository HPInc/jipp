// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.util.getStaticObjects
import java.io.IOException

/** An IPP enum type */
open class EnumType<T : Enum>(val enumEncoder: EnumType.Encoder<T>, override val name: String) :
        AttributeType<T>(enumEncoder, Tag.enumValue) {

    /**
     * An [Encoder] for [Enum] values
     * @param typeName Human-readable type of the [Enum]
     * @param map predefined [Enum] instances to reuse when decoding
     * @param factory a way to create new [Enum] instances of the correct type when decoding an undefined value
     */
    data class Encoder<T : Enum>(
        override val typeName: String,
        val map: Map<Int, T>,
        val factory: (code: Int, name: String) -> T
    ) : com.hp.jipp.encoding.Encoder<T>() {

        constructor(name: String, enums: Collection<T>, factory: (code: Int, name: String) -> T):
                this(name, Enum.toCodeMap(enums), factory)

        /**
         * Construct an [EnumType] encoder for a subclass of Enum. All public static instances of the class
         * will be included as potential values for decoding purposes.
         */
        constructor(cls: Class<T>, factory: (code: Int, name: String) -> T):
            this(cls.simpleName,
                cls.getStaticObjects().filter { cls.isAssignableFrom(it.javaClass) }.map {
                    @Suppress("UNCHECKED_CAST")
                    it as T
                }, factory)

        /** Returns a known [Enum], or creates a new instance from factory if not found  */
        operator fun get(code: Int): T =
            map[code] ?: factory(code, "$typeName(x${Integer.toHexString(code)})")

        @Throws(IOException::class)
        override fun readValue(input: IppInputStream, finder: Finder, valueTag: Tag): T {
            return get(IntegerType.Encoder.readValue(input, valueTag))
        }

        @Throws(IOException::class)
        override fun writeValue(out: IppOutputStream, value: T) {
            IntegerType.Encoder.writeValue(out, value.code)
        }

        override fun valid(valueTag: Tag): Boolean = valueTag == Tag.enumValue
    }

    override fun convert(attribute: Attribute<*>): Attribute<T>? =
        if (attribute.valueTag !== Tag.enumValue) null
        else of(attribute.values
                .filter { it is Int }
                .map { enumEncoder[it as Int] })
}
