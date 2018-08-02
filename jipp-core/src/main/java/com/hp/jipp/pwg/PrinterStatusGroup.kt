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
 * Attribute types for the Printer Status group.
 */
object PrinterStatusGroup {

    /**
     * "device-service-count" as defined in:
     * [PWG5100.13](http://ftp.pwg.org/pub/pwg/candidates/cs-ippjobprinterext3v10-20120727-5100.13.pdf).
     */
    @JvmField val deviceServiceCount = IntType("device-service-count")

    /**
     * "device-uuid" as defined in:
     * [PWG5100.13](http://ftp.pwg.org/pub/pwg/candidates/cs-ippjobprinterext3v10-20120727-5100.13.pdf).
     */
    @JvmField val deviceUuid = UriType("device-uuid")

    /**
     * "document-format-varying-attributes" as defined in:
     * [RFC3380](http://www.iana.org/go/rfc3380).
     * May contain any keyword from [DocumentFormatVaryingAttribute].
     */
    @JvmField val documentFormatVaryingAttributes = KeywordType("document-format-varying-attributes")

    /**
     * "job-settable-attributes-supported" as defined in:
     * [RFC3380](http://www.iana.org/go/rfc3380).
     * May contain any keyword from [JobSettableAttributesSupported].
     */
    @JvmField val jobSettableAttributesSupported = KeywordType("job-settable-attributes-supported")

    /**
     * "pages-per-minute" as defined in:
     * [RFC8011](http://www.iana.org/go/rfc8011).
     */
    @JvmField val pagesPerMinute = IntType("pages-per-minute")

    /**
     * "pages-per-minute-color" as defined in:
     * [RFC8011](http://www.iana.org/go/rfc8011).
     */
    @JvmField val pagesPerMinuteColor = IntType("pages-per-minute-color")

    /**
     * "printer-alert" as defined in:
     * [PWG5100.9](http://ftp.pwg.org/pub/pwg/candidates/cs-ippstate10-20090731-5100.9.pdf).
     */
    @JvmField val printerAlert = KeyValueType("printer-alert")

    /**
     * "printer-alert-description" as defined in:
     * [PWG5100.9](http://ftp.pwg.org/pub/pwg/candidates/cs-ippstate10-20090731-5100.9.pdf).
     */
    @JvmField val printerAlertDescription = TextType("printer-alert-description")

    /**
     * "printer-config-change-date-time" as defined in:
     * [PWG5100.13](http://ftp.pwg.org/pub/pwg/candidates/cs-ippjobprinterext3v10-20120727-5100.13.pdf).
     */
    @JvmField val printerConfigChangeDateTime = DateTimeType("printer-config-change-date-time")

    /**
     * "printer-config-change-time" as defined in:
     * [PWG5100.13](http://ftp.pwg.org/pub/pwg/candidates/cs-ippjobprinterext3v10-20120727-5100.13.pdf).
     */
    @JvmField val printerConfigChangeTime = IntType("printer-config-change-time")

    /**
     * "printer-detailed-status-messages" as defined in:
     * [PWG5100.11](http://ftp.pwg.org/pub/pwg/candidates/cs-ippjobprinterext10-20101030-5100.11.pdf).
     */
    @JvmField val printerDetailedStatusMessages = TextType("printer-detailed-status-messages")

    /**
     * "printer-finisher" as defined in:
     * [PWG5100.1](http://ftp.pwg.org/pub/pwg/candidates/cs-ippfinishings10-20010205-5100.1.pdf).
     */
    @JvmField val printerFinisher = OctetsType("printer-finisher")

    /**
     * "printer-finisher-description" as defined in:
     * [PWG5100.1](http://ftp.pwg.org/pub/pwg/candidates/cs-ippfinishings10-20010205-5100.1.pdf).
     */
    @JvmField val printerFinisherDescription = TextType("printer-finisher-description")

    /**
     * "printer-finisher-supplies" as defined in:
     * [PWG5100.1](http://ftp.pwg.org/pub/pwg/candidates/cs-ippfinishings10-20010205-5100.1.pdf).
     */
    @JvmField val printerFinisherSupplies = OctetsType("printer-finisher-supplies")

    /**
     * "printer-finisher-supplies-description" as defined in:
     * [PWG5100.1](http://ftp.pwg.org/pub/pwg/candidates/cs-ippfinishings10-20010205-5100.1.pdf).
     */
    @JvmField val printerFinisherSuppliesDescription = TextType("printer-finisher-supplies-description")

    /**
     * "printer-input-tray" as defined in:
     * [PWG5100.13](http://ftp.pwg.org/pub/pwg/candidates/cs-ippjobprinterext3v10-20120727-5100.13.pdf).
     */
    @JvmField val printerInputTray = OctetsType("printer-input-tray")

    /**
     * "printer-is-accepting-jobs" as defined in:
     * [RFC8011](http://www.iana.org/go/rfc8011).
     */
    @JvmField val printerIsAcceptingJobs = BooleanType("printer-is-accepting-jobs")

    /**
     * "printer-message-date-time" as defined in:
     * [RFC3380](http://www.iana.org/go/rfc3380).
     */
    @JvmField val printerMessageDateTime = DateTimeType("printer-message-date-time")

    /**
     * "printer-message-from-operator" as defined in:
     * [RFC8011](http://www.iana.org/go/rfc8011).
     */
    @JvmField val printerMessageFromOperator = TextType("printer-message-from-operator")

    /**
     * "printer-message-time" as defined in:
     * [RFC3380](http://www.iana.org/go/rfc3380).
     */
    @JvmField val printerMessageTime = IntType("printer-message-time")

    /**
     * "printer-more-info" as defined in:
     * [RFC8011](http://www.iana.org/go/rfc8011).
     */
    @JvmField val printerMoreInfo = UriType("printer-more-info")

    /**
     * "printer-output-tray" as defined in:
     * [PWG5100.13](http://ftp.pwg.org/pub/pwg/candidates/cs-ippjobprinterext3v10-20120727-5100.13.pdf).
     */
    @JvmField val printerOutputTray = OctetsType("printer-output-tray")

    /**
     * "printer-settable-attributes-supported" as defined in:
     * [RFC3380](http://www.iana.org/go/rfc3380).
     * May contain any keyword from [PrinterSettableAttributesSupported].
     */
    @JvmField val printerSettableAttributesSupported = KeywordType("printer-settable-attributes-supported")

    /**
     * "printer-state" as defined in:
     * [RFC8011](http://www.iana.org/go/rfc8011).
     */
    @JvmField val printerState = PrinterState.Type("printer-state")

    /**
     * "printer-state-change-date-time" as defined in:
     * [RFC3995](http://www.iana.org/go/rfc3995).
     */
    @JvmField val printerStateChangeDateTime = DateTimeType("printer-state-change-date-time")

    /**
     * "printer-state-change-time" as defined in:
     * [RFC3995](http://www.iana.org/go/rfc3995).
     */
    @JvmField val printerStateChangeTime = IntType("printer-state-change-time")

    /**
     * "printer-state-message" as defined in:
     * [RFC8011](http://www.iana.org/go/rfc8011).
     */
    @JvmField val printerStateMessage = TextType("printer-state-message")

    /**
     * "printer-state-reasons" as defined in:
     * [RFC8011](http://www.iana.org/go/rfc8011).
     * May contain any keyword from [PrinterStateReason].
     */
    @JvmField val printerStateReasons = KeywordType("printer-state-reasons")

    /**
     * "printer-static-resource-k-octets-free" as defined in:
     * [PWG5100.18](http://ftp.pwg.org/pub/pwg/candidates/cs-ippinfra10-20150619-5100.18.pdf).
     */
    @JvmField val printerStaticResourceKOctetsFree = IntType("printer-static-resource-k-octets-free")

    /**
     * "printer-supply" as defined in:
     * [PWG5100.13](http://ftp.pwg.org/pub/pwg/candidates/cs-ippjobprinterext3v10-20120727-5100.13.pdf).
     */
    @JvmField val printerSupply = OctetsType("printer-supply")

    /**
     * "printer-supply-description" as defined in:
     * [PWG5100.13](http://ftp.pwg.org/pub/pwg/candidates/cs-ippjobprinterext3v10-20120727-5100.13.pdf).
     */
    @JvmField val printerSupplyDescription = TextType("printer-supply-description")

    /**
     * "printer-supply-info-uri" as defined in:
     * [PWG5100.13](http://ftp.pwg.org/pub/pwg/candidates/cs-ippjobprinterext3v10-20120727-5100.13.pdf).
     */
    @JvmField val printerSupplyInfoUri = UriType("printer-supply-info-uri")

    /**
     * "printer-up-time" as defined in:
     * [RFC8011](http://www.iana.org/go/rfc8011).
     */
    @JvmField val printerUpTime = IntType("printer-up-time")

    /**
     * "printer-uri-supported" as defined in:
     * [RFC8011](http://www.iana.org/go/rfc8011).
     */
    @JvmField val printerUriSupported = UriType("printer-uri-supported")

    /**
     * "printer-uuid" as defined in:
     * [PWG5100.13](http://ftp.pwg.org/pub/pwg/candidates/cs-ippjobprinterext3v10-20120727-5100.13.pdf).
     */
    @JvmField val printerUuid = UriType("printer-uuid")

    /**
     * "queued-job-count" as defined in:
     * [RFC8011](http://www.iana.org/go/rfc8011).
     */
    @JvmField val queuedJobCount = IntType("queued-job-count")

    /**
     * "xri-authentication-supported" as defined in:
     * [RFC3380](http://www.iana.org/go/rfc3380).
     * May contain any keyword from [XriAuthenticationSupported].
     */
    @JvmField val xriAuthenticationSupported = KeywordType("xri-authentication-supported")

    /**
     * "xri-security-supported" as defined in:
     * [RFC3380](http://www.iana.org/go/rfc3380).
     * May contain any keyword from [XriSecuritySupported].
     */
    @JvmField val xriSecuritySupported = KeywordType("xri-security-supported")

    /**
     * "xri-uri-scheme-supported" as defined in:
     * [RFC3380](http://www.iana.org/go/rfc3380).
     */
    @JvmField val xriUriSchemeSupported = StringType(Tag.uriScheme, "xri-uri-scheme-supported")
}
