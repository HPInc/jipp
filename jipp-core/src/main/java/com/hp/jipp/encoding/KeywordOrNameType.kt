// Copyright 2017 - 2021 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/**
 * An [AttributeType] for a [Name] or keyword value.
 *
 * See [RFC8011 Section 5.1.3](https://tools.ietf.org/html/rfc8011#section-5.1.3).
 */
open class KeywordOrNameType(name: String) : AttributeTypeImpl<KeywordOrName>(name, KeywordOrName::class.java) {
    /** An [AttributeType] for multiple [Name] and keyword values. */
    class Set(name: String) : KeywordOrNameType(name), AttributeSetType<KeywordOrName> {
        /** Return an [Attribute] containing keyword values as given. */
        fun of(value: String, vararg values: String) = of((listOf(value) + values).map { KeywordOrName(it) })

        /** Return an [Attribute] containing one or more values of type [KeywordOrName]. */
        fun of(value: Name, vararg values: Name): Attribute<KeywordOrName> =
            of((listOf(value) + values).map { KeywordOrName(it) })

        override fun toString() = "KeywordOrNameType.Set($name)"
    }

    override fun coerce(value: Any): KeywordOrName? =
        when (value) {
            is KeywordOrName -> value
            is Name -> KeywordOrName(value)
            is String -> KeywordOrName(value)
            else -> null
        }

    /** Return an [Attribute] containing a single value of type [KeywordOrName]. */
    fun of(value: String): Attribute<KeywordOrName> =
        of(KeywordOrName(value))

    /** Return an [Attribute] containing a single value of type [KeywordOrName]. */
    fun of(value: Name): Attribute<KeywordOrName> =
        of(KeywordOrName(value))

    /**
     * A form of this [AttributeType] that represents all incoming Name and Keyword data as [String] objects.
     */
    val asString: AttributeType<String> by lazy {
        object : AttributeType<String> {
            override val name = this@KeywordOrNameType.name

            override fun coerce(value: Any): String? =
                when (value) {
                    is KeywordOrName -> value.keyword ?: value.name?.value
                    is Name -> value.value
                    is String -> value
                    else -> null
                }
        }
    }

    companion object {
        val codec = Codec<KeywordOrName>(
            { false },
            {
                throw IllegalArgumentException("This codec is not used for reading")
            },
            {
                if (NameType.codec.handlesTag(it.tag)) {
                    NameType.codec.writeValue(this, it.name!!)
                } else {
                    KeywordType.codec.writeValue(this, it.keyword!!)
                }
            }
        )
    }
}
