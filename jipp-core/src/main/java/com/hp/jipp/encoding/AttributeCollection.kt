package com.hp.jipp.encoding

import com.hp.jipp.util.PrettyPrintable
import com.hp.jipp.util.PrettyPrinter

/** An ordered collection of attributes as defined in [RFC3382](https://www.iana.org/go/rfc3382). */
interface AttributeCollection : PrettyPrintable {
    val attributes: List<Attribute<*>>

    override fun print(printer: PrettyPrinter) {
        printer.open(PrettyPrinter.OBJECT)
        printer.addAll(attributes)
        printer.close()
    }

    /**
     * An [AttributeType] for a value of an [AttributeCollection] subclass.
     *
     * Note: subclasses may only represent recognizable data types in the type. If additional data is required,
     * it may be necessary to extract the original attribute list using an [UntypedCollection] instance.
     */
    class Type<T : AttributeCollection>(
        override val name: String,
        private val converter: Converter<T>
    ) : AttributeType<T> {
        override fun coerce(value: Any): T? =
            when (value) {
                is AttributeCollection -> converter.convert(value.attributes)
                else -> null
            }
    }

    /**
     * An [AttributeType] for a multiple values of an [AttributeCollection] subclass.
     *
     * Note: subclasses may only represent recognizable data types in the type. If additional data is required,
     * it may be necessary to extract the original attribute list using an [UntypedCollection] instance.
     */
    class SetType<T : AttributeCollection>(
        override val name: String,
        private val converter: Converter<T>
    ) : AttributeSetType<T> {
        override fun coerce(value: Any): T? =
            when (value) {
                is AttributeCollection -> converter.convert(value.attributes)
                else -> null
            }
    }

    /** Converts a [List] of [Attribute] objects into an [AttributeCollection]. */
    interface Converter<T : AttributeCollection> {
        /**
         * Progressively convert attributes into the destination type
         */
        fun convert(attributes: List<Attribute<*>>): T

        /** Returns the first value of attribute [type] from [attributes]. */
        fun <T : Any> extractOne(attributes: List<Attribute<*>>, type: AttributeType<T>): T? =
            coerced(attributes, type)?.let {
                when (it.size) {
                    0 -> null
                    else -> it[0]
                }
            }

        /** Returns all values of attribute [type] from [attributes]. */
        fun <T : Any> extractAll(attributes: List<Attribute<*>>, type: AttributeType<T>): List<T>? =
            coerced(attributes, type)?.let {
                when (it.size) {
                    0 -> null
                    else -> it
                }
            }

        /** Return the attribute having the same name and coerced into the given attribute type, if possible. */
        fun <T : Any> coerced(attributes: List<Attribute<*>>, type: AttributeType<T>): Attribute<T>? =
            attributes.find { it.name == type.name }?.let {
                type.coerce(it)
            }
    }
}
