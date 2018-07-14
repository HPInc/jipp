// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT
//
// DO NOT MODIFY. Code is auto-generated by genTypes.py. Content taken from registry at
// https://www.iana.org/assignments/ipp-registrations/ipp-registrations.xml, updated on 2018-04-06
@file:Suppress("MaxLineLength", "WildcardImport")

package com.hp.jipp.pwg

import com.hp.jipp.encoding.* // ktlint-disable no-wildcard-imports

/**
 * Data object corresponding to a "job-save-disposition" collection as defined in:
 * [PWG5100.11](http://ftp.pwg.org/pub/pwg/candidates/cs-ippjobprinterext10-20101030-5100.11.pdf).
 */
@Suppress("RedundantCompanionReference", "unused")
data class JobSaveDisposition
@JvmOverloads constructor(
    /** May contain any keyword from [SaveDisposition]. */
    val saveDisposition: String? = null,
    val saveInfo: List<SaveInfo>? = null,
    /** Original parameters received, if any. */
    val _original: List<Attribute<*>>? = null
) : AttributeCollection {

    /** Produce an attribute list from members, or return the [_original] attribute list (if it exists). */
    override val attributes: List<Attribute<*>> by lazy {
        _original ?: listOfNotNull(
            saveDisposition?.let { Members.saveDisposition.of(it) },
            saveInfo?.let { Members.saveInfo.of(it) }
        )
    }

    /** Type for attributes of this collection */
    class Type(override val name: String) : AttributeCollection.Type<JobSaveDisposition>(Members)

    /** All member names as strings. */
    object Name {
        /** "save-disposition" member name */
        const val saveDisposition = "save-disposition"
        /** "save-info" member name */
        const val saveInfo = "save-info"
    }

    companion object Members : AttributeCollection.Converter<JobSaveDisposition> {
        override fun convert(attributes: List<Attribute<*>>): JobSaveDisposition =
            JobSaveDisposition(
                extractOne(attributes, saveDisposition),
                extractAll(attributes, saveInfo),
                _original = attributes)
        /**
         * "save-disposition" member type.
         * May contain any keyword from [SaveDisposition].
         */
        @JvmField val saveDisposition = KeywordType(Name.saveDisposition)
        /**
         * "save-info" member type.
         */
        @JvmField val saveInfo = SaveInfo.Type(Name.saveInfo)
    }

    /**
     * Data object corresponding to a "save-info" collection.
     */
    @Suppress("RedundantCompanionReference", "unused")
    data class SaveInfo
    @JvmOverloads constructor(
        val saveDocumentFormat: String? = null,
        val saveLocation: java.net.URI? = null,
        val saveName: String? = null,
        /** Original parameters received, if any. */
        val _original: List<Attribute<*>>? = null
    ) : AttributeCollection {

        /** Produce an attribute list from members, or return the [_original] attribute list (if it exists). */
        override val attributes: List<Attribute<*>> by lazy {
            _original ?: listOfNotNull(
                saveDocumentFormat?.let { Members.saveDocumentFormat.of(it) },
                saveLocation?.let { Members.saveLocation.of(it) },
                saveName?.let { Members.saveName.of(it) }
            )
        }

        /** Type for attributes of this collection */
        class Type(override val name: String) : AttributeCollection.Type<SaveInfo>(Members)

        /** All member names as strings. */
        object Name {
            /** "save-document-format" member name */
            const val saveDocumentFormat = "save-document-format"
            /** "save-location" member name */
            const val saveLocation = "save-location"
            /** "save-name" member name */
            const val saveName = "save-name"
        }

        companion object Members : AttributeCollection.Converter<SaveInfo> {
            override fun convert(attributes: List<Attribute<*>>): SaveInfo =
                SaveInfo(
                    extractOne(attributes, saveDocumentFormat),
                    extractOne(attributes, saveLocation),
                    extractOne(attributes, saveName)?.value,
                    _original = attributes)
            /**
             * "save-document-format" member type.
             */
            @JvmField val saveDocumentFormat = StringType(Tag.mimeMediaType, Name.saveDocumentFormat)
            /**
             * "save-location" member type.
             */
            @JvmField val saveLocation = UriType(Name.saveLocation)
            /**
             * "save-name" member type.
             */
            @JvmField val saveName = NameType(Name.saveName)
        }
    }
}
