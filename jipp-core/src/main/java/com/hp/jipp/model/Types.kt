// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.model

import com.hp.jipp.encoding.* // ktlint-disable no-wildcard-imports
import com.hp.jipp.util.getStaticObjects

/** Attribute types from various specifications */
object Types {
    @JvmField val attributesCharset = StringType(Tag.charset, "attributes-charset")
    @JvmField val attributesNaturalLanguage = StringType(Tag.naturalLanguage, "attributes-natural-language")
    @JvmField val colorSupported = BooleanType("color-supported")
    @JvmField val copiesSupported = RangeOfIntegerType("copies-supported")
    @JvmField val detailedStatusMessage = StringType(Tag.textWithoutLanguage, "detailed-status-message")
    @JvmField val documentAccessError = StringType(Tag.textWithoutLanguage, "document-access-error")
    @JvmField val documentFormat = StringType(Tag.mimeMediaType, "document-format")
    @JvmField val documentFormatSupported = StringType(Tag.mimeMediaType, "document-format-supported")
    @JvmField val documentName = StringType(Tag.nameWithoutLanguage, "document-name")
    @JvmField val finishings = Finishings.Type("finishings-default")
    @JvmField val finishingsDefault = Finishings.Type("finishings-default")
    @JvmField val finishingsSupported = Finishings.Type("finishings-supported")
    @JvmField val identifyActionsDefault = IdentifyAction("identify-actions-default")
    @JvmField val identifyActions = IdentifyAction("identify-actions")
    @JvmField val identifyActionsSupported = IdentifyAction("identify-actions-supported")
    @JvmField val jobAccountIdSupported = BooleanType("job-account-id-supported")
    @JvmField val jobAccountingUserIdSupported = BooleanType("job-accounting-user-id-supported")
    @JvmField val jobDetailedStatusMessages = StringType(Tag.textWithoutLanguage, "job-detailed-status-messages")
    @JvmField val jobId = IntegerType(Tag.integerValue, "job-id")
    @JvmField val jobName = StringType(Tag.nameWithoutLanguage, "job-name")
    @JvmField val jobPassword = OctetStringType(Tag.octetString, "job-password")
    @JvmField val jobPasswordSupported = IntegerType("job-password-supported")
    @JvmField val jobPasswordEncryption = JobPasswordEncryption.Type("job-password-encryption")
    @JvmField val jobPasswordEncryptionSupported = JobPasswordEncryption.Type("job-password-encryption-supported")
    @JvmField val jobState = JobState.Type("job-state")
    @JvmField val jobStateMessage = StringType(Tag.textWithoutLanguage, "job-state-message")
    @JvmField val jobStateReasons = StringType(Tag.keyword, "job-state-reasons")
    @JvmField val jobUri = UriType(Tag.uri, "job-uri")
    @JvmField val lastDocument = BooleanType("last-document")
    @JvmField val mediaCol = CollectionType("media-col")
    @JvmField val mediaBottomMarginSupported = IntegerType("media-bottom-margin-supported")
    @JvmField val mediaColDatabase = CollectionType("media-col-database")
    @JvmField val mediaColSupported = CollectionType("media-col-supported")
    @JvmField val mediaDefault = MediaSize.Type("media-default")
    @JvmField val mediaKeySupported = CollectionType("media-key-supported")
    @JvmField val mediaLeftMarginSupported = IntegerType("media-left-margin-supported")
    @JvmField val mediaRightMarginSupported = IntegerType("media-right-margin-supported")
    @JvmField val mediaTopMarginSupported = IntegerType("media-top-margin-supported")
    @JvmField val media = MediaSize.Type("media")
    @JvmField val mediaReady = MediaSize.Type("media-ready")
    @JvmField val mediaSize = CollectionType("media-size")
    @JvmField val mediaSizeSupported = CollectionType("media-size-supported")
    @JvmField val mediaSupported = MediaSize.Type("media-supported")
    @JvmField val message = StringType(Tag.textWithoutLanguage, "message")
    @JvmField val myJobs = BooleanType("my-jobs")
    @JvmField val operationsSupported = Operation.Type("operations-supported")
    @JvmField val orientationRequested = Orientation.Type("orientation-requested")
    @JvmField val outputBin = OutputBin("output-bin")
    @JvmField val outputBinSupported = OutputBin("output-bin-supported")
    @JvmField val printerAlert = KeyValueType("printer-alert")
    @JvmField val printerDnsSdName = StringType(Tag.nameWithoutLanguage, "printer-dns-sd-name")
    @JvmField val printerIcons = UriType(Tag.uri, "printer-icons")
    @JvmField val printerInfo = StringType(Tag.textWithoutLanguage, "printer-info")
    @JvmField val printerInputTray = KeyValueType("printer-input-tray")
    @JvmField val printerMakeAndModel = StringType(Tag.textWithoutLanguage, "printer-make-and-model")
    @JvmField val printerName = StringType(Tag.nameWithoutLanguage, "printer-name")
    @JvmField val printerOutputTray = KeyValueType("printer-output-tray")
    @JvmField val printerResolutionDefault = ResolutionType(Tag.resolution, "printer-resolution-default")
    @JvmField val printerState = PrinterState.Type("printer-state")
    @JvmField val printerStateMessage = StringType(Tag.textWithoutLanguage, "printer-state-message")
    @JvmField val printerStateReasons = StringType(Tag.keyword, "printer-state-reasons")
    @JvmField val printerSupply = KeyValueType("printer-supply")
    @JvmField val printerUriSupported = UriType(Tag.uri, "printer-uri-supported")
    @JvmField val printerUri = UriType(Tag.uri, "printer-uri")
    @JvmField val printerUuid = UriType(Tag.uri, "printer-uuid")
    @JvmField val requestedAttributes = StringType(Tag.keyword, "requested-attributes")
    @JvmField val requestingUserName = StringType(Tag.nameWithoutLanguage, "requesting-user-name")
    @JvmField val sides = Sides.Type("sides")
    @JvmField val sidesSupported = Sides.Type("sides-supported")
    @JvmField val statusMessage = StringType(Tag.textWithoutLanguage, "status-message")
    @JvmField val xDimension = IntegerType("x-dimension")
    @JvmField val yDimension = IntegerType("y-dimension")

    /** All known attributes */
    @JvmField
    val all = Types::class.java.getStaticObjects()
            .filter { it is AttributeType<*> }
            .map { it as AttributeType<*> }

    /** An object used to find encoders for all known types above */
    @JvmField
    val allFinder = Encoder.finderOf(Types.all.map {
        it.name to it
    }.toMap(), AttributeGroup.encoders)
}
