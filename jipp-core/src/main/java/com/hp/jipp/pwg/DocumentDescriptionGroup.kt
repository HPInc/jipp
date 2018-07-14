// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT
//
// DO NOT MODIFY. Code is auto-generated by genTypes.py. Content taken from registry at
// https://www.iana.org/assignments/ipp-registrations/ipp-registrations.xml, updated on 2018-04-06
@file:Suppress("MaxLineLength", "WildcardImport", "MagicNumber")

package com.hp.jipp.pwg

import com.hp.jipp.encoding.* // ktlint-disable no-wildcard-imports
import com.hp.jipp.util.getStaticObjects

/**
 * Attribute types for the Document Description group.
 */
object DocumentDescriptionGroup {

    /**
     * "document-metadata" as defined in:
     * [PWG5100.13](http://ftp.pwg.org/pub/pwg/candidates/cs-ippjobprinterext3v10-20120727-5100.13.pdf).
     */
    @JvmField val documentMetadata = OctetsType("document-metadata")

    /**
     * "document-name" as defined in:
     * [PWG5100.5](http://ftp.pwg.org/pub/pwg/candidates/cs-ippdocobject10-20031031-5100.5.pdf).
     */
    @JvmField val documentName = NameType("document-name")

    /**
     * "impressions" as defined in:
     * [PWG5100.5](http://ftp.pwg.org/pub/pwg/candidates/cs-ippdocobject10-20031031-5100.5.pdf).
     */
    @JvmField val impressions = IntType("impressions")

    /**
     * "impressions-col" as defined in:
     * [APRIL2015F2F](http://ftp.pwg.org/pub/pwg/ipp/minutes/ippv2-f2f-minutes-20150429.pdf).
     */
    @JvmField val impressionsCol = ImpressionsCol.Type("impressions-col")

    /**
     * "k-octets" as defined in:
     * [PWG5100.5](http://ftp.pwg.org/pub/pwg/candidates/cs-ippdocobject10-20031031-5100.5.pdf).
     */
    @JvmField val kOctets = IntType("k-octets")

    /**
     * "media-sheets" as defined in:
     * [PWG5100.5](http://ftp.pwg.org/pub/pwg/candidates/cs-ippdocobject10-20031031-5100.5.pdf).
     */
    @JvmField val mediaSheets = IntType("media-sheets")

    /**
     * "media-sheets-col" as defined in:
     * [APRIL2015F2F](http://ftp.pwg.org/pub/pwg/ipp/minutes/ippv2-f2f-minutes-20150429.pdf).
     */
    @JvmField val mediaSheetsCol = MediaSheetsCol.Type("media-sheets-col")

    /**
     * "pages" as defined in:
     * [PWG5100.13](http://ftp.pwg.org/pub/pwg/candidates/cs-ippjobprinterext3v10-20120727-5100.13.pdf).
     */
    @JvmField val pages = IntType("pages")

    /**
     * "pages-col" as defined in:
     * [APRIL2015F2F](http://ftp.pwg.org/pub/pwg/ipp/minutes/ippv2-f2f-minutes-20150429.pdf).
     */
    @JvmField val pagesCol = PagesCol.Type("pages-col")

    /** All known attributes */
    @JvmField
    val all = DocumentDescriptionGroup::class.java.getStaticObjects()
            .filter { it is AttributeType<*> }
            .map { it as AttributeType<*> }
}
