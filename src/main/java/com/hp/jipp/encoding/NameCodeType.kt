package com.hp.jipp.encoding

import com.google.auto.value.AutoValue
import com.google.common.base.Function
import com.google.common.base.Optional
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.google.common.collect.Lists
import com.hp.jipp.util.Util

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

class NameCodeType<T : NameCode>(val nameCodeEncoder: NameCodeType.Encoder<T>, name: String) :
        AttributeType<T>(nameCodeEncoder, Tag.EnumValue, name) {

    /** An encoder for NameCode enumerations.  */
    data class Encoder<T : NameCode>(override val type: String, val map: Map<Int, T>,
                                     val factory: NameCode.Factory<T>) : Attribute.BaseEncoder<T>() {

        /** Returns a known enum, or creates a new instance if not found  */
        operator fun get(code: Int): T {
            val e = Optional.fromNullable(map[code])
            if (e.isPresent) return e.get()
            return factory.of(type + "(x" + Integer.toHexString(code) + ")", code)
        }

        @Throws(IOException::class)
        override fun readValue(`in`: DataInputStream, finder: Attribute.EncoderFinder, valueTag: Tag): T {
            return get(IntegerType.ENCODER.readValue(`in`, valueTag))
        }

        @Throws(IOException::class)
        override fun writeValue(out: DataOutputStream, value: T) {
            IntegerType.ENCODER.writeValue(out, value.code)
        }

        override fun valid(valueTag: Tag): Boolean = valueTag == Tag.EnumValue

        companion object {
            /**
             * Return a new NameCode.Encoder including values from all static members defined in the class (from reflection)
             */
            @JvmStatic
            fun <T : NameCode> of(cls: Class<T>, factory: NameCode.Factory<T>): Encoder<T> =
                of(cls.simpleName, Util.getStaticObjects(cls)
                        .filter { cls.isAssignableFrom(it.javaClass)}
                        .map {
                            @Suppress("UNCHECKED_CAST")
                            it as T
                        }, factory)

            /** Return a new enumeration encoder  */
            @JvmStatic
            fun <T : NameCode> of(name: String, enums: Collection<T>, factory: NameCode.Factory<T>): Encoder<T> =
                Encoder(name, NameCode.toCodeMap(enums), factory)
        }
    }

    override fun of(attribute: Attribute<*>): Optional<Attribute<T>> {
        if (attribute.valueTag !== Tag.EnumValue) return Optional.absent<Attribute<T>>()
        return Optional.of(of(attribute.values.filter { it is Int }.map { nameCodeEncoder[it as Int] } ))
    }
}
