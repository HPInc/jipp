// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.model

import com.hp.jipp.encoding.* // ktlint-disable no-wildcard-imports
import com.hp.jipp.pwg.Finishing
import com.hp.jipp.pwg.JobState
import com.hp.jipp.pwg.OperationGroup
import com.hp.jipp.pwg.Orientation
import com.hp.jipp.pwg.PrintQuality
import com.hp.jipp.pwg.PrinterDescriptionGroup
import com.hp.jipp.pwg.PrinterState

/** Attribute types from various specifications */
object Types {
    @JvmField val attributesCharset = OperationGroup.attributesCharset
    @JvmField val attributesNaturalLanguage = StringType(Tag.naturalLanguage, "attributes-natural-language")
    @JvmField val charsetConfigured = StringType(Tag.charset, "charset-configured")
    @JvmField val charsetSupported = StringType(Tag.charset, "charset-supported")
    @JvmField val colorSupported = BooleanType("color-supported")
    @JvmField val copiesSupported = IntRangeType("copies-supported")
    @JvmField val detailedStatusMessage = TextType( "detailed-status-message")
    @JvmField val documentAccessError = TextType( "document-access-error")
    @JvmField val documentFormat = StringType(Tag.mimeMediaType, "document-format")
    @JvmField val documentFormatSupported = StringType(Tag.mimeMediaType, "document-format-supported")
    @JvmField val documentName = NameType("document-name")
    @JvmField val finishings = Finishing.Type("finishings")
    @JvmField val finishingsDefault = Finishing.Type("finishings-default")
    @JvmField val finishingsSupported = Finishing.Type("finishings-supported")
    @JvmField val generatedNaturalLanguageSupported = StringType(Tag.naturalLanguage,
        "generated-natural-language-supported")
    @JvmField val identifyActions = KeywordType("identify-actions")
    @JvmField val identifyActionsDefault = KeywordType("identify-actions-default")
    @JvmField val identifyActionsSupported = KeywordType("identify-actions-supported")
    @JvmField val jobAccountIdSupported = BooleanType("job-account-id-supported")
    @JvmField val jobAccountingUserIdSupported = BooleanType("job-accounting-user-id-supported")
    @JvmField val jobDetailedStatusMessages = TextType( "job-detailed-status-messages")
    @JvmField val jobId = IntType("job-id")
    @JvmField val jobName = NameType("job-name")
    @JvmField val jobPassword = OctetsType("job-password")
    @JvmField val jobPasswordSupported = IntType("job-password-supported")
    @JvmField val jobPasswordEncryption = KeywordType("job-password-encryption")
    @JvmField val jobPasswordEncryptionSupported = KeywordType("job-password-encryption-supported")
    @JvmField val jobState = JobState.Type("job-state")
    @JvmField val jobStateMessage = TextType( "job-state-message")
    @JvmField val jobStateReasons = KeywordType("job-state-reasons")
    @JvmField val jobUri = UriType("job-uri")
    @JvmField val lastDocument = BooleanType("last-document")
//    @JvmField val mediaCol = CollectionType("media-col")
    @JvmField val mediaBottomMarginSupported = IntType("media-bottom-margin-supported")
//    @JvmField val mediaColDatabase = CollectionType("media-col-database")
//    @JvmField val mediaColSupported = CollectionType("media-col-supported")
    @JvmField val mediaDefault = MediaSize.Type("media-default")
//    @JvmField val mediaKeySupported = CollectionType("media-key-supported")
    @JvmField val mediaLeftMarginSupported = IntType("media-left-margin-supported")
    @JvmField val mediaRightMarginSupported = IntType("media-right-margin-supported")
    @JvmField val mediaSourceFeedOrientation = Orientation.Type("media-source-feed-orientation")
    @JvmField val mediaTopMarginSupported = IntType("media-top-margin-supported")
    @JvmField val media = MediaSize.Type("media")
    @JvmField val mediaReady = MediaSize.Type("media-ready")
//    @JvmField val mediaSize = CollectionType("media-size")
//    @JvmField val mediaSizeSupported = CollectionType("media-size-supported")
    @JvmField val mediaSupported = MediaSize.Type("media-supported")
    @JvmField val message = TextType( "message")
    @JvmField val myJobs = BooleanType("my-jobs")
    @JvmField val operationsSupported = PrinterDescriptionGroup.operationsSupported
    @JvmField val orientationRequested = Orientation.Type("orientation-requested")
    @JvmField val orientationRequestedActual = Orientation.Type("orientation-requested-actual")
    @JvmField val orientationRequestedSupported = Orientation.Type("orientation-requested-supported")
    @JvmField val orientationRequestedDefault = Orientation.Type("orientation-requested-default")
    @JvmField val outputBin = KeywordType("output-bin")
    @JvmField val outputBinSupported = KeywordType("output-bin-supported")
//    @JvmField val printerAlert = KeyValueType("printer-alert")
    @JvmField val printerCurrentTime = DateTimeType("printer-current-time")
    @JvmField val printerDnsSdName = NameType("printer-dns-sd-name")
    @JvmField val printerIcons = UriType("printer-icons")
    @JvmField val printerInfo = TextType( "printer-info")
    // TODO: Replace KeyValue support? Not really called out as a syntax but should be. Gen could handle.
//    @JvmField val printerInputTray = KeyValueType("printer-input-tray")
    @JvmField val printerMakeAndModel = TextType( "printer-make-and-model")
    @JvmField val printerName = NameType("printer-name")
//    @JvmField val printerOutputTray = KeyValueType("printer-output-tray")
    @JvmField val printerResolutionDefault = ResolutionType("printer-resolution-default")
    @JvmField val printerState = PrinterState.Type("printer-state")
    @JvmField val printerStateMessage = TextType( "printer-state-message")
    @JvmField val printerStateReasons = KeywordType("printer-state-reasons")
//    @JvmField val printerSupply = KeyValueType("printer-supply")
    @JvmField val printerUriSupported = UriType("printer-uri-supported")
    @JvmField val printerUri = UriType("printer-uri")
    @JvmField val printerUuid = UriType("printer-uuid")
    @JvmField val printQuality = PrintQuality.Type("print-quality")
    @JvmField val printQualityDefault = PrintQuality.Type("print-quality-default")
    @JvmField val printQualitySupported = PrintQuality.Type("print-quality-supported")
    @JvmField val requestedAttributes = KeywordType("requested-attributes")
    @JvmField val requestingUserName = NameType("requesting-user-name")
    @JvmField val sides = KeywordType("sides")
    @JvmField val sidesSupported = KeywordType("sides-supported")
    @JvmField val statusMessage = TextType( "status-message")
    @JvmField val xDimension = IntType("x-dimension")
    @JvmField val yDimension = IntType("y-dimension")
}
