package com.hp.jipp.encoding

import com.hp.jipp.util.Pretty

/** The collection of attributes found within a [CollectionType] attribute.  */
data class AttributeCollection(val attributes: List<Attribute<*>>) : Pretty.Printable {
    constructor(vararg attributes: Attribute<*>) : this(listOf(*attributes))

    /** Return the first attribute matching the type  */
    operator fun <T> get(type: AttributeType<T>): Attribute<T>? {
        return attributes
                .firstOrNull { it.valueTag == type.tag && it.name == type.name }
                ?.let { type.of(it) }
    }

    /** Return all values found from the first attribute matching the type, or an empty list if no match  */
    fun <T> values(type: AttributeType<T>): List<T> = get(type)?.values ?: listOf()

    override fun print(printer: Pretty.Printer) {
        printer.open(Pretty.OBJECT)
        printer.addAll(attributes)
        printer.close()
    }

    override fun toString() = attributes.toString()
}
