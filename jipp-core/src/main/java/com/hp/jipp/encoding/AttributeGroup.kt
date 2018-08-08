// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.model.EnumTypes
import com.hp.jipp.model.KeyValueTypes
import com.hp.jipp.util.BuildError
import com.hp.jipp.util.ParseError
import com.hp.jipp.util.PrettyPrintable
import com.hp.jipp.util.PrettyPrinter
import com.hp.jipp.util.repeatUntilNull
import java.io.IOException
import java.util.* // ktlint-disable

/**
 * A tagged group of attributes.
 */
class AttributeGroup(
    val tag: Tag,
    private val attributes: List<Attribute<*>>
) : PrettyPrintable, List<Attribute<*>> by attributes {

    constructor(tag: Tag, vararg attribute: Attribute<*>) : this(tag, attribute.toList())

    init {
        if (!tag.isDelimiter) {
            throw BuildError("Group tag $tag must be a delimiter")
        }
        // RFC2910: Within an attribute group, if two or more attributes have the same name, the attribute group
        // is malformed (see [RFC2911] section 3.1.3). Throw if someone attempts this.
        val names = HashSet<String>()
        for (attribute in attributes) {
            val name = attribute.name
            if (names.contains(name)) {
                throw BuildError("Attribute Group contains more than one '$name` in $attributes")
            }
            names.add(name)
        }
    }

    /** A map of of attribute names to matching attributes found in this group. */
    private val map: Map<String, Attribute<*>> by lazy {
        attributes.map { it.name to it }.toMap()
    }

    /** Return the attribute corresponding to the specified [name]. */
    operator fun get(name: String): Attribute<*>? = map[name]

    /** Return all values found having this attribute type. */
    fun <T : Any> getValues(type: AttributeType<T>): List<T> =
        get(type) ?: listOf()

    /** Return all values found having this attribute type. */
    fun <T : Any> getStrings(type: AttributeType<T>): List<String> =
        get(type)?.strings() ?: listOf()

    /** Return the attribute as conforming to the supplied attribute type. */
    operator fun <T : Any> get(type: AttributeType<T>): Attribute<T>? =
        map[type.name]?.let {
            type.coerce(it)
        }

    /** Return the first value of an attribute matching [type]. */
    fun <T : Any> getValue(type: AttributeType<T>): T? =
        get(type)?.get(0)

    /** Write this group to the [IppOutputStream] */
    @Throws(IOException::class)
    fun write(output: IppOutputStream) {
        output.writeByte(tag.code)
        forEach { output.writeAttribute(it) }
    }

    override fun print(printer: PrettyPrinter) {
        printer.open(PrettyPrinter.OBJECT, tag.toString())
        printer.addAll(this)
        printer.close()
    }

    override fun equals(other: Any?) =
        if (this === other) true else when (other) {
            is AttributeGroup -> other.tag == tag && other.attributes == attributes
            is List<*> -> attributes == other
            else -> false
        }

    override fun hashCode(): Int {
        // Note: tag is not included because we might need to hash this with other List objects
        return attributes.hashCode()
    }

    override fun toString(): String {
        return "AttributeGroup($tag, $attributes)"
    }

    companion object {
        const val BYTE_LENGTH = 1
        const val INT_LENGTH = 4
        const val CALENDAR_LENGTH = 11
        const val BYTE_MASK = 0xFF

        @JvmStatic
        fun groupOf(tag: Tag, attributes: List<Attribute<*>>) =
            AttributeGroup(tag, attributes)

        @JvmStatic
        fun groupOf(tag: Tag, vararg attributes: Attribute<*>) =
            AttributeGroup(tag, attributes.toList())

        /** Codecs for core types */
        private val codecs by lazy {
            listOf(
                IntType.codec,
                BooleanType.codec,
                EnumType.codec,
                codec(Tag.octetString, {
                    readValueBytes()
                }, {
                    writeValueBytes(it)
                }),
                DateTimeType.codec,
                ResolutionType.codec,
                IntRangeType.codec,
                IntOrIntRangeType.codec,
                CollectionType.codec,
                TextType.codec,
                NameType.codec,
                OctetsType.codec,
                KeyValues.codec,
                codec({ it.isOctetString || it.isInteger }, { tag ->
                    // Used when we don't know how to interpret the content. Even with integers,
                    // we don't know whether to expect a short or byte or int or whatever.
                    OtherOctets(tag, readValueBytes())
                }, {
                    writeValueBytes(it.value)
                }),
                KeywordType.codec,
                UriType.codec,
                codec({ it.isCharString }, { tag ->
                    // Handle other harder-to-type values here:
                    // uriScheme, naturalLanguage, mimeMediaType, charset etc.
                    OtherString(tag, readString())
                }, {
                    writeString(it.value)
                })
            )
        }

        private val clsToCodec by lazy {
            codecs.map { it.cls to it }.toMap()
        }

        private val tagToCodec by lazy {
            Tag.all
                .map { tag -> tag to codecs.firstOrNull { it.handlesTag(tag) } }
                .filter { it.second != null }
                .toMap()
        }

        /** Reads/writes values of [T]. */
        interface Codec<T> {
            val cls: Class<T>
            /** Return true if this codec handles the specified tag. */
            fun handlesTag(tag: Tag): Boolean
            /** Read a value from the input stream */
            fun readValue(input: IppInputStream, startTag: Tag): T
            /** Write value (assuming it is an instance of [T]). */
            fun writeValue(output: IppOutputStream, value: Any)
            /** The tag to use for a particular value. */
            fun tagOf(value: Any): Tag
        }

        fun Byte.toUint(): Int = this.toInt() and BYTE_MASK

        /** Construct a codec handling [TaggedValue] values, covering any number of [Tag] input values. */
        inline fun <reified T : TaggedValue> codec(
            crossinline handlesTagFunc: (Tag) -> Boolean,
            crossinline readAttrFunc: IppInputStream.(startTag: Tag) -> T,
            crossinline writeAttrFunc: IppOutputStream.(value: T) -> Unit
        ) =
            object : Codec<T> {
                override val cls: Class<T> = T::class.java
                override fun tagOf(value: Any) = (value as T).tag
                override fun handlesTag(tag: Tag) = handlesTagFunc(tag)
                override fun readValue(input: IppInputStream, startTag: Tag): T =
                    input.readAttrFunc(startTag)
                override fun writeValue(output: IppOutputStream, value: Any) {
                    output.writeAttrFunc(value as T)
                }
            }

        /** Construct a codec handling values encoded by a particular [Tag]. */
        inline fun <reified T> codec(
            valueTag: Tag,
            crossinline readAttrFunc: IppInputStream.(startTag: Tag) -> T,
            crossinline writeAttrFunc: IppOutputStream.(value: T) -> Unit
        ) =
            object : Codec<T> {
                override val cls: Class<T> = T::class.java
                override fun tagOf(value: Any) = valueTag
                override fun handlesTag(tag: Tag) = valueTag == tag
                override fun readValue(input: IppInputStream, startTag: Tag): T =
                    input.readAttrFunc(startTag)
                override fun writeValue(output: IppOutputStream, value: Any) {
                    output.writeAttrFunc(value as T)
                }
            }

        /**
         * Read and return the next tag in the input.
         */
        fun IppInputStream.readTag() =
            Tag.read(this)

        /**
         * Read an entire attribute group if available in the input stream.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun read(input: IppInputStream, groupTag: Tag) =
            AttributeGroup(groupTag, { input.readNextAttribute() }.repeatUntilNull().toList())

        /** Read the next attribute if present */
        fun IppInputStream.readNextAttribute(): Attribute<Any>? =
            if (available() == 0) {
                null
            } else {
                mark(1)
                val tag = readTag()
                if (tag.isDelimiter) {
                    reset()
                    null
                } else {
                    readAnyAttribute(tag)
                }
            }

        /**
         * Read and return an attribute with all of its values.
         */
        private fun IppInputStream.readAnyAttribute(initTag: Tag): Attribute<Any> =
            readAnyAttribute(readString(), initTag)

        /**
         * Read and return an attribute with all of its values.
         */
        fun IppInputStream.readAnyAttribute(attributeName: String, initTag: Tag): Attribute<Any> {
            if (initTag.isOutOfBand) {
                readValueBytes()
                return EmptyAttribute(attributeName, initTag)
            }

            return codecs.firstOrNull { it.handlesTag(initTag) }?.let {
                val values = listOf(readValue(it, initTag, attributeName)) + {
                    readNextValue(attributeName)
                }.repeatUntilNull()
                UnknownAttribute(attributeName, values)
            } ?: throw ParseError("No codec found for tag $initTag")
        }

        @Suppress("ReturnCount")
        private fun <T : Any> IppInputStream.readValue(codec: Codec<T>, tag: Tag, attributeName: String): Any {
            // Apply a special case for enum values which we can match with all known [Enums]
            if (tag == Tag.enumValue) {
                EnumTypes.all[attributeName]?.also {
                    takeLength(AttributeGroup.INT_LENGTH)
                    // Note: !! is safe because we know EnumTypes can handle Int input
                    return it.coerce(readInt())!!
                }
            } else if (tag == Tag.octetString) {
                KeyValueTypes.all[attributeName]?.also {
                    return KeyValues.codec.readValue(this, tag)
                }
            }

            return codec.readValue(this, tag)
        }

        @Suppress("ReturnCount") // Best way to handle errors in this case
        private fun IppInputStream.readNextValue(attributeName: String): Any? {
            // Must have at least enough for another tag and name length string
            if (available() < IppInputStream.TAG_LEN + IppInputStream.LENGTH_LENGTH) {
                return null
            }
            mark(IppInputStream.TAG_LEN + IppInputStream.LENGTH_LENGTH)
            val tag = readTag()
            if (tag.isDelimiter || tag.isOutOfBand || tag == Tag.memberAttributeName || tag == Tag.endCollection ||
                readShort().toInt() != 0) {
                // Non-value tag or non-empty name means its a completely different attribute.
                reset()
                return null
            }
            val codec = tagToCodec[tag] // Fast lookup
                ?: codecs.firstOrNull { it.handlesTag(tag) } // Slower, more thorough lookup
                ?: throw ParseError("No codec found for tag $tag")
            return readValue(codec, tag, attributeName)
        }

        /** Write the attribute to this stream. */
        fun IppOutputStream.writeAttribute(attribute: Attribute<*>, name: String = attribute.name) {
            attribute.tag?.also {
                // Write the out-of-band tag
                writeTag(it)
                writeString(name)
                writeShort(0) // 0 value length = no values
            } ?: run {
                var nameToWrite = name
                attribute.forEach { value ->
                    val encoder = clsToCodec[value.javaClass]
                        ?: codecs.firstOrNull { it.cls.isAssignableFrom(value.javaClass) }
                        ?: throw BuildError("Cannot handle $value: ${value.javaClass}")

                    // If the attribute has an enforced tag then apply it
                    val tag = if (attribute.type is StringType) {
                        (attribute.type as StringType).tag
                    } else {
                        encoder.tagOf(value)
                    }
                    writeTag(tag)
                    writeString(nameToWrite)
                    encoder.writeValue(this, value)

                    // Only write attribute name for the first item
                    nameToWrite = ""
                }
            }
        }
    }
}
