// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

import com.hp.jipp.model.EnumTypes
import com.hp.jipp.model.KeyValuesTypes
import com.hp.jipp.util.BuildError
import com.hp.jipp.util.ParseError
import com.hp.jipp.util.PrettyPrintable
import com.hp.jipp.util.PrettyPrinter
import com.hp.jipp.util.repeatUntilNull
import java.io.IOException

/**
 * A tagged list of attributes. Only one attribute of a given type may appear in the group, although each attribute
 * may contain 0 or more values.
 */
interface AttributeGroup : PrettyPrintable, List<Attribute<*>> {
    val tag: Tag

    /** Return the attribute corresponding to the specified [name]. */
    operator fun get(name: String): Attribute<*>?

    /** Return the attribute as conforming to the supplied attribute type. */
    operator fun <T : Any> get(type: AttributeType<T>): Attribute<T>?

    /** Return all values found having this attribute type. */
    fun <T : Any> getValues(type: AttributeType<T>): List<T> =
        get(type) ?: listOf()

    /** Return the first value of an attribute matching [type]. */
    fun <T : Any> getValue(type: AttributeType<T>): T? =
        get(type)?.firstOrNull()

    /** Return the string form of any values present for this attribute [type]. */
    fun <T : Any> getStrings(type: AttributeType<T>): List<String> =
        get(type)?.strings() ?: listOf()

    /** Return the string form (or null) of the first value present for this attribute [type]. */
    fun <T : Any> getString(type: AttributeType<T>): String? =
        get(type)?.strings()?.firstOrNull()

    /** Write this group to [output]. */
    @Throws(IOException::class)
    fun write(output: IppOutputStream) {
        output.writeByte(tag.code)
        forEach { writeAttribute(output, it) }
    }

    override fun print(printer: PrettyPrinter) {
        printer.open(PrettyPrinter.OBJECT, tag.toString())
        printer.addAll(this)
        printer.close()
    }

    /** Return a copy of this attribute group in mutable form. */
    fun toMutable(): MutableAttributeGroup =
        mutableGroupOf(tag, this)

    @Suppress("TooManyFunctions")
    companion object {
        const val BYTE_LENGTH = 1
        const val INT_LENGTH = 4
        const val CALENDAR_LENGTH = 11
        private const val BYTE_MASK = 0xFF

        /** Return a fixed group of attributes. */
        @JvmStatic
        fun groupOf(tag: Tag, attributes: List<Attribute<*>>): AttributeGroup =
            AttributeGroupImpl(tag, attributes)

        /** Return a fixed group of attributes. */
        @JvmStatic
        fun groupOf(tag: Tag, vararg attributes: Attribute<*>): AttributeGroup =
            groupOf(tag, attributes.toList())

        /** Return a mutable group of attributes. */
        @JvmStatic
        fun mutableGroupOf(tag: Tag, attributes: List<Attribute<*>>): MutableAttributeGroup =
            MutableAttributeGroup(tag, attributes)

        /** Return a mutable group of attributes. */
        @JvmStatic
        fun mutableGroupOf(tag: Tag, vararg attributes: Attribute<*>): MutableAttributeGroup =
            mutableGroupOf(tag, attributes.toList())

        /** Codecs for core types */
        private val codecs by lazy {
            // Note: lazy is required for late evaluation
            // Also note: codecs are defined and referenced internally here to avoid problems passing
            // them through the Collection codec, which also needs them.
            listOf(
                IntType.codec,
                BooleanType.codec,
                EnumType.codec,
                Codec(Tag.octetString, {
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
                Codec({ it.isOctetString || it.isInteger }, { tag ->
                    // Used when we don't know how to interpret the content. Even with integers,
                    // we don't know whether to expect a short or byte or int or whatever.
                    OtherOctets(tag, readValueBytes())
                }, {
                    writeValueBytes(it.value)
                }),
                KeywordType.codec,
                KeywordOrNameType.codec, // Must follow both Keyword and Name
                UriType.codec,
                Codec({ it.isCharString }, { tag ->
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

        fun Byte.toUint(): Int = this.toInt() and BYTE_MASK

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
            groupOf(groupTag, { input.readNextAttribute() }.repeatUntilNull().toList())

        /** Read the next attribute if present */
        fun IppInputStream.readNextAttribute(): Attribute<*>? {
            mark(1)
            return readTag()?.let { tag ->
                if (tag.isDelimiter) {
                    reset()
                    null
                } else {
                    readAnyAttribute(tag)
                }
            }
        }

        /**
         * Read and return an attribute with all of its values.
         */
        private fun IppInputStream.readAnyAttribute(initTag: Tag): Attribute<*> =
            readAnyAttribute(readString(), initTag)

        /**
         * Read and return an attribute with all of its values.
         */
        fun IppInputStream.readAnyAttribute(attributeName: String, initTag: Tag): Attribute<*> {
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
            // Apply a special case for enum values which we can match with all known [EnumTypes]
            if (tag == Tag.enumValue) {
                EnumTypes.all[attributeName]?.also {
                    takeLength(INT_LENGTH)
                    // Note: !! is safe because we know EnumTypes can handle Int input
                    return it.coerce(readInt())!!
                }
            } else if (tag == Tag.octetString) {
                KeyValuesTypes.all[attributeName]?.also {
                    return KeyValues.codec.readValue(this, tag)
                }
            }

            return codec.readValue(this, tag)
        }

        @Suppress("ReturnCount") // Best way to handle errors in this case
        private fun IppInputStream.readNextValue(attributeName: String): Any? {
            mark(IppInputStream.TAG_LEN + IppInputStream.LENGTH_LENGTH)
            return readTag()?.let { tag ->
                if (tag.isEndOfValueStream() || readShort().toInt() != 0) {
                    // Non-value tag or non-empty name means its a completely different attribute.
                    reset()
                    null
                } else {
                    val codec = tagToCodec[tag] // Fast lookup
                        ?: codecs.firstOrNull { it.handlesTag(tag) } // Slower, more thorough lookup
                        ?: throw ParseError("No codec found for tag $tag")
                    readValue(codec, tag, attributeName)
                }
            }
        }

        /** Identify tags that indicate the current attribute has no more values */
        private fun Tag.isEndOfValueStream() =
            isDelimiter || isOutOfBand || this == Tag.memberAttributeName || this == Tag.endCollection

        /** Write the attribute to this stream. */
        fun writeAttribute(stream: IppOutputStream, attribute: Attribute<*>, name: String = attribute.name) {
            with(stream) {
                attribute.tag?.also {
                    // Write the out-of-band tag
                    writeTag(it)
                    writeString(name)
                    writeShort(0) // 0 value length = no values
                } ?: run {
                    writeValueAttribute(attribute, name)
                }
            }
        }

        /** Write an attribute having value(s) to this stream. */
        private fun IppOutputStream.writeValueAttribute(attribute: Attribute<*>, name: String) {
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
