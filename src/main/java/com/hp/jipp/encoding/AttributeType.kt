package com.hp.jipp.encoding

import com.hp.jipp.util.BuildError
import com.hp.jipp.util.Hook

/**
 * Associates a specific tag and name such that an attribute can be safely created or retrieved from a group
 */
abstract class AttributeType<T>(val encoder: Encoder<T>, val tag: Tag) {
    abstract val name: String

    init {
        if (!(encoder.valid(tag) || Hook.`is`(Attribute.HOOK_ALLOW_BUILD_INVALID_TAGS))) {
            throw BuildError("Invalid tag $tag for encoder $encoder")
        }
    }

    /** Create an attribute of this attribute type with supplied values */
    open operator fun invoke(values: List<T>) = Attribute(tag, name, values, encoder)

    operator fun invoke(value: T, vararg values: T): Attribute<T> =
            if (values.isEmpty()) invoke(value) else invoke(listOf(value) + values)

    operator fun invoke(value: T) = invoke(listOf(value))

    fun empty() = Attribute(tag, name, listOf(), encoder)

    // "of()" for java uses...

    open fun of(values: List<T>) = invoke(values)

    fun of(values: Array<T>) = invoke(values.toList())

    open fun of(value: T, vararg values: T): Attribute<T> = if (values.isEmpty()) {
        invoke(listOf(value))
    } else {
        invoke(listOf(value) + values)
    }

    /** Return true if the attribute has a matching encoder */
    fun isValid(attribute: Attribute<*>): Boolean {
        return attribute.encoder == encoder
    }

    /** If possible, convert the supplied attribute into an attribute of this type. */
    open fun of(attribute: Attribute<*>): Attribute<T>? =
        if (attribute.encoder === encoder) {
            invoke(attribute.values.map {
                @Suppress("UNCHECKED_CAST")
                it as T
            })
        } else {
            null
        }
}
