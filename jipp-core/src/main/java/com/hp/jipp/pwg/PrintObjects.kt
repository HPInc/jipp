// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT
//
// DO NOT MODIFY. Code is auto-generated by genTypes.py. Content taken from registry at
// https://www.iana.org/assignments/ipp-registrations/ipp-registrations.xml, updated on 2018-04-06
@file:Suppress("MaxLineLength", "WildcardImport")

package com.hp.jipp.pwg

import com.hp.jipp.encoding.* // ktlint-disable no-wildcard-imports

/**
 * Data object corresponding to a "print-objects" collection as defined in:
 * [PWG5100.21](http://ftp.pwg.org/pub/pwg/candidates/cs-ipp3d10-20170210-5100.21.pdf).
 */
@Suppress("RedundantCompanionReference", "unused")
data class PrintObjects
@JvmOverloads constructor(
    val documentNumber: Int? = null,
    val objectOffset: ObjectOffset? = null,
    val objectSize: ObjectSize? = null,
    val objectUuid: java.net.URI? = null,
    /** Original parameters received, if any. */
    val _original: List<Attribute<*>>? = null
) : AttributeCollection {

    /** Produce an attribute list from members, or return the [_original] attribute list (if it exists). */
    override val attributes: List<Attribute<*>> by lazy {
        _original ?: listOfNotNull(
            documentNumber?.let { Members.documentNumber.of(it) },
            objectOffset?.let { Members.objectOffset.of(it) },
            objectSize?.let { Members.objectSize.of(it) },
            objectUuid?.let { Members.objectUuid.of(it) }
        )
    }

    /** Type for attributes of this collection */
    class Type(override val name: String) : AttributeCollection.Type<PrintObjects>(Members)

    /** All member names as strings. */
    object Name {
        /** "document-number" member name */
        const val documentNumber = "document-number"
        /** "object-offset" member name */
        const val objectOffset = "object-offset"
        /** "object-size" member name */
        const val objectSize = "object-size"
        /** "object-uuid" member name */
        const val objectUuid = "object-uuid"
    }

    companion object Members : AttributeCollection.Converter<PrintObjects> {
        override fun convert(attributes: List<Attribute<*>>): PrintObjects =
            PrintObjects(
                extractOne(attributes, documentNumber),
                extractOne(attributes, objectOffset),
                extractOne(attributes, objectSize),
                extractOne(attributes, objectUuid),
                _original = attributes)
        /**
         * "document-number" member type.
         */
        @JvmField val documentNumber = IntType(Name.documentNumber)
        /**
         * "object-offset" member type.
         */
        @JvmField val objectOffset = ObjectOffset.Type(Name.objectOffset)
        /**
         * "object-size" member type.
         */
        @JvmField val objectSize = ObjectSize.Type(Name.objectSize)
        /**
         * "object-uuid" member type.
         */
        @JvmField val objectUuid = UriType(Name.objectUuid)
    }

    /**
     * Data object corresponding to a "object-offset" collection.
     */
    @Suppress("RedundantCompanionReference", "unused")
    data class ObjectOffset
    @JvmOverloads constructor(
        val xOffset: Int? = null,
        val yOffset: Int? = null,
        val zOffset: Int? = null,
        /** Original parameters received, if any. */
        val _original: List<Attribute<*>>? = null
    ) : AttributeCollection {

        /** Produce an attribute list from members, or return the [_original] attribute list (if it exists). */
        override val attributes: List<Attribute<*>> by lazy {
            _original ?: listOfNotNull(
                xOffset?.let { Members.xOffset.of(it) },
                yOffset?.let { Members.yOffset.of(it) },
                zOffset?.let { Members.zOffset.of(it) }
            )
        }

        /** Type for attributes of this collection */
        class Type(override val name: String) : AttributeCollection.Type<ObjectOffset>(Members)

        /** All member names as strings. */
        object Name {
            /** "x-offset" member name */
            const val xOffset = "x-offset"
            /** "y-offset" member name */
            const val yOffset = "y-offset"
            /** "z-offset" member name */
            const val zOffset = "z-offset"
        }

        companion object Members : AttributeCollection.Converter<ObjectOffset> {
            override fun convert(attributes: List<Attribute<*>>): ObjectOffset =
                ObjectOffset(
                    extractOne(attributes, xOffset),
                    extractOne(attributes, yOffset),
                    extractOne(attributes, zOffset),
                    _original = attributes)
            /**
             * "x-offset" member type.
             */
            @JvmField val xOffset = IntType(Name.xOffset)
            /**
             * "y-offset" member type.
             */
            @JvmField val yOffset = IntType(Name.yOffset)
            /**
             * "z-offset" member type.
             */
            @JvmField val zOffset = IntType(Name.zOffset)
        }
    }

    /**
     * Data object corresponding to a "object-size" collection.
     */
    @Suppress("RedundantCompanionReference", "unused")
    data class ObjectSize
    @JvmOverloads constructor(
        val xDimension: Int? = null,
        val yDimension: Int? = null,
        val zDimension: Int? = null,
        /** Original parameters received, if any. */
        val _original: List<Attribute<*>>? = null
    ) : AttributeCollection {

        /** Produce an attribute list from members, or return the [_original] attribute list (if it exists). */
        override val attributes: List<Attribute<*>> by lazy {
            _original ?: listOfNotNull(
                xDimension?.let { Members.xDimension.of(it) },
                yDimension?.let { Members.yDimension.of(it) },
                zDimension?.let { Members.zDimension.of(it) }
            )
        }

        /** Type for attributes of this collection */
        class Type(override val name: String) : AttributeCollection.Type<ObjectSize>(Members)

        /** All member names as strings. */
        object Name {
            /** "x-dimension" member name */
            const val xDimension = "x-dimension"
            /** "y-dimension" member name */
            const val yDimension = "y-dimension"
            /** "z-dimension" member name */
            const val zDimension = "z-dimension"
        }

        companion object Members : AttributeCollection.Converter<ObjectSize> {
            override fun convert(attributes: List<Attribute<*>>): ObjectSize =
                ObjectSize(
                    extractOne(attributes, xDimension),
                    extractOne(attributes, yDimension),
                    extractOne(attributes, zDimension),
                    _original = attributes)
            /**
             * "x-dimension" member type.
             */
            @JvmField val xDimension = IntType(Name.xDimension)
            /**
             * "y-dimension" member type.
             */
            @JvmField val yDimension = IntType(Name.yDimension)
            /**
             * "z-dimension" member type.
             */
            @JvmField val zDimension = IntType(Name.zDimension)
        }
    }
}
