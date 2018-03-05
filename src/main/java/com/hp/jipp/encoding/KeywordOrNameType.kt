package com.hp.jipp.encoding

import com.hp.jipp.util.getStaticObjects
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

/** Attribute type for attributes based on [Keyword] or a simple string name */
class KeywordOrNameType<T : Keyword>(encoder: KeywordOrNameType.Encoder<T>, override val name: String) :
        AttributeType<T>(encoder, Tag.keyword) {

    /** An encoder for keyword types  */
    class Encoder<T : Keyword>(private val factory: Keyword.Factory<T>, all: Collection<T>, name: String) :
            SimpleEncoder<T>(name) {

        private val map: Map<String, T> = all.map { it.name to it }.toMap()

        @Throws(IOException::class)
        override fun readValue(input: DataInputStream, valueTag: Tag): T {
            val key = StringType.ENCODER.readValue(input, valueTag)
            return map[key] ?: factory.of(key)
        }

        @Throws(IOException::class)
        override fun writeValue(out: DataOutputStream, value: T) {
            StringType.ENCODER.writeValue(out, value.name)
        }

        override fun valid(valueTag: Tag) = valueTag == Tag.keyword || valueTag == Tag.nameWithoutLanguage

        val all: Collection<T>
            get() = map.values
    }

    companion object {
        /** Return a new [Encoder] for a class internally defining static [Keyword] objects */
        fun <T : Keyword> encoderOf(cls: Class<T>, factory: Keyword.Factory<T>): KeywordOrNameType.Encoder<T> =
                KeywordOrNameType.Encoder(factory, cls.getStaticObjects()
                        .filter { cls.isAssignableFrom(it.javaClass) }
                        .map {
                            @Suppress("UNCHECKED_CAST")
                            it as T
                        }, cls.simpleName)

        /** Return a new [Encoder] for a class internally defining static [Keyword] objects */
        fun <T : Keyword> encoderOf(cls: Class<T>, factory: (String) -> T): KeywordOrNameType.Encoder<T> =
                encoderOf(cls, object : Keyword.Factory<T> {
                    override fun of(name: String): T = factory(name)
                })
    }
}
