package com.hp.jipp.encoding

import com.google.common.base.Optional
import com.google.common.base.Suppliers
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.hp.jipp.util.BuildError
import com.hp.jipp.util.Hook
import com.hp.jipp.util.ParseError
import com.hp.jipp.util.Pretty

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.Arrays
import java.util.HashSet

/** A specific group of attributes found in a packet. */
data class AttributeGroup(val tag: Tag, val attributes: List<Attribute<*>>) : Pretty.Printable {

    init {
        // RFC2910: Within an attribute group, if two or more attributes have the same name, the attribute group
        // is malformed (see [RFC2911] section 3.1.3).
        // Throw if someone attempts this.
        val exist = HashSet<String>()
        for ((_, name) in attributes) {
            if (exist.contains(name) && !Hook.`is`(HOOK_ALLOW_BUILD_DUPLICATE_NAMES_IN_GROUP)) {
                throw BuildError("Attribute Group contains more than one '" + name +
                        "' in " + attributes)
            }
            exist.add(name)
        }
    }

    /** Lazy attribute map, generated only when needed  */
    private val mAttributeMap = Suppliers.memoize {
        val builder = ImmutableMap.Builder<String, Attribute<*>>()
        for (attribute in attributes) {
            builder.put(attribute.name, attribute)
        }
        builder.build()
    }

    /** Return a lazily-created, parse-only map of attribute name to a list of matching attributes  */
    internal val map: Map<String, Attribute<*>>
        get() = mAttributeMap.get()

    /** Return a attribute from this group.  */
    operator fun <T> get(attributeType: AttributeType<T>): Optional<Attribute<T>> {
        val attribute = map[attributeType.name] ?: return Optional.absent<Attribute<T>>()

        if (attributeType.isValid(attribute)) {
            return Optional.of(attribute as Attribute<T>)
        } else {
            return attributeType.of(attribute)
        }
    }

    /**
     * Return values for the specified attribute type in this group, or an empty list if not present
     */
    fun <T> getValues(attributeType: AttributeType<T>): List<T> {
        val attribute = get(attributeType)
        if (!attribute.isPresent) return ImmutableList.of<T>()
        return attribute.get().values
    }

    /**
     * Return a single value, if any exist for this attribute
     */
    fun <T> getValue(attributeType: AttributeType<T>): Optional<T> {
        val values = getValues(attributeType)
        if (values.isEmpty()) return Optional.absent<T>()
        return Optional.of(values[0])
    }

    @Throws(IOException::class)
    fun write(out: DataOutputStream) {
        out.writeByte(tag.code)
        for (attribute in attributes) {
            attribute.write(out)
        }
    }

    override fun print(printer: Pretty.Printer) {
        printer.open(Pretty.OBJECT, tag.toString())
        printer.addAll(attributes)
        printer.close()
    }

    companion object {
        val HOOK_ALLOW_BUILD_DUPLICATE_NAMES_IN_GROUP = AttributeGroup::class.java.name + ".HOOK_ALLOW_BUILD_DUPLICATE_NAMES_IN_GROUP"

        /** Default encoders available to parse incoming data  */
        @JvmField val ENCODERS: List<Attribute.BaseEncoder<*>> = ImmutableList.of<Attribute.BaseEncoder<out Any>>(
                IntegerType.ENCODER, UriType.ENCODER, StringType.ENCODER, BooleanType.ENCODER, LangStringType.ENCODER,
                CollectionType.ENCODER, RangeOfIntegerType.ENCODER, ResolutionType.ENCODER, OctetStringType.ENCODER)

        /** Return a complete attribute group  */
        @JvmStatic fun of(startTag: Tag, vararg attributes: Attribute<*>): AttributeGroup {
            return of(startTag, Arrays.asList(*attributes))
        }

        /** Return a complete attribute group  */
        @JvmStatic fun of(startTag: Tag, attributes: List<Attribute<*>>): AttributeGroup {
            if (!startTag.isDelimiter) throw BuildError("Not a delimiter: " + startTag)
            return AttributeGroup(startTag, attributes)
        }

        @JvmStatic fun finderOf(attributeTypes: Map<String, AttributeType<*>>,
                              encoders: List<Attribute.BaseEncoder<*>>): Attribute.EncoderFinder {
            return object : Attribute.EncoderFinder {
                @Throws(IOException::class)
                override fun find(valueTag: Tag, name: String): Attribute.BaseEncoder<*> {
                    // Check for a matching attribute type
                    val attributeType = attributeTypes[name]
                    if (attributeType != null && attributeType.encoder.valid(valueTag)) {
                        return attributeType.encoder
                    }

                    // If no valid match above then try with each default encoder
                    return encoders.find { it.valid(valueTag) } ?:
                            throw ParseError("No encoder found for $name($valueTag)")
                }
            }
        }

        @Throws(IOException::class)
        fun read(startTag: Tag, attributeTypes: Map<String, AttributeType<*>>, `in`: DataInputStream): AttributeGroup {
            var attributes = true
            val attributesBuilder = ImmutableList.Builder<Attribute<*>>()

            val finder = finderOf(attributeTypes, AttributeGroup.ENCODERS)

            while (attributes) {
                `in`.mark(1)
                val valueTag = Tag.read(`in`)
                if (valueTag.isDelimiter) {
                    `in`.reset()
                    attributes = false
                } else {
                    attributesBuilder.add(Attribute.read(`in`, finder, valueTag))
                }
            }
            return of(startTag, attributesBuilder.build())
        }
    }
}
