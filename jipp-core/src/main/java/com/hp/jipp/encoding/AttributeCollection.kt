package com.hp.jipp.encoding

import com.hp.jipp.util.PrettyPrintable
import com.hp.jipp.util.PrettyPrinter

/** Attributes found within an attribute collection. */
interface AttributeCollection : PrettyPrintable {
    val attributes: List<Attribute<*>>

    override fun print(printer: PrettyPrinter) {
        printer.open(PrettyPrinter.OBJECT)
        printer.addAll(attributes)
        printer.close()
    }

    abstract class Type<T : AttributeCollection>(private val converter: Converter<T>) : AttributeType<T> {
        override fun coerce(value: Any): T? =
            if (value is AttributeCollection) {
                converter.convert(value.attributes.toMutableList())
            } else {
                null
            }
    }

    interface Converter<T : AttributeCollection> {
        /**
         * Progressively convert attributes into the destination type
         */
        fun convert(attributes: List<Attribute<*>>): T

        /**
         * Consumes the first value of attribute [type] from [attributes], removing it and its attribute if nothing
         * remains of it.
         */
        fun <T : Any> extractOne(attributes: List<Attribute<*>>, type: AttributeType<T>): T? =
            coerced(attributes, type)?.let {
                when (it.values.size) {
                    0 -> null
                    else -> it.value
                }
            }

        /**
         * Consumes all values of attribute [type] from [attributes], removing it if consumed.
         */
        fun <T : Any> extractAll(attributes: List<Attribute<*>>, type: AttributeType<T>): List<T>? =
            coerced(attributes, type)?.let {
                when (it.values.size) {
                    0 -> null
                    else -> it.values
                }
            }

        /** Return the attribute having the same name and coerced into the given attribute type, if possible */
        fun <T : Any> coerced(attributes: List<Attribute<*>>, type: AttributeType<T>): Attribute<T>? =
            attributes.find { it.name == type.name }?.let {
                type.coerce(it)
            }
    }
}
