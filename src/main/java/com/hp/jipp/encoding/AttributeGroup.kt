package com.hp.jipp.encoding

import com.hp.jipp.util.BuildError
import com.hp.jipp.util.Hook
import com.hp.jipp.util.ParseError
import com.hp.jipp.util.PrettyPrinter
import org.jetbrains.annotations.Nullable

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.Arrays
import java.util.HashSet

/** A specific group of attributes found in a packet. */
data class AttributeGroup(val tag: Tag, val attributes: List<Attribute<*>>) : PrettyPrinter.Printable {

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

    /** Return a map of attribute names to a list of matching attributes  */
    internal val map: Map<String, Attribute<*>> by lazy {
        attributes.map { it.name to it }.toMap()
    }

    /** Return a attribute from this group.  */
    operator fun <T> get(attributeType: AttributeType<T>): Attribute<T>? {
        val attribute = map[attributeType.name] ?: return null

        if (attributeType.isValid(attribute)) {
            @Suppress("UNCHECKED_CAST")
            return attribute as Attribute<T>
        } else {
            return attributeType.of(attribute)
        }
    }

    /**
     * Return all values for the specified attribute type in this group, or an empty list if not present
     */
    fun <T> getValues(attributeType: AttributeType<T>): List<T> =
        get(attributeType)?.values ?: listOf()

    @Nullable fun <T> getValue(attributeType: AttributeType<T>): T? =
        get(attributeType)?.values?.get(0)

    @Throws(IOException::class)
        fun write(out: DataOutputStream) {
            out.writeByte(tag.code)
            attributes.forEach { it.write(out) }
        }

    override fun print(printer: PrettyPrinter) {
        printer.open(PrettyPrinter.OBJECT, tag.toString())
        printer.addAll(attributes)
        printer.close()
    }

    companion object {
        val HOOK_ALLOW_BUILD_DUPLICATE_NAMES_IN_GROUP = AttributeGroup::class.java.name + ".HOOK_ALLOW_BUILD_DUPLICATE_NAMES_IN_GROUP"

        /** Default encoders available to parse incoming data  */
        @JvmField val ENCODERS = listOf(
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
                              encoders: List<Encoder<*>>): Encoder.Finder {
            return object : Encoder.Finder {
                @Throws(IOException::class)
                override fun find(valueTag: Tag, name: String): Encoder<*> {
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
        @JvmStatic fun read(startTag: Tag, attributeTypes: Map<String, AttributeType<*>>, input: DataInputStream): AttributeGroup {
            var more = true
            val attributes = ArrayList<Attribute<*>>()
            val finder = finderOf(attributeTypes, AttributeGroup.ENCODERS)

            while (more) {
                input.mark(1)
                val valueTag = Tag.read(input)
                if (valueTag.isDelimiter) {
                    input.reset()
                    more = false
                } else {
                    attributes.add(Attribute.read(input, finder, valueTag))
                }
            }
            return of(startTag, attributes)
        }
    }
}
