package com.hp.jipp.model

import com.hp.jipp.encoding.AttributeType
import com.hp.jipp.encoding.BooleanType
import com.hp.jipp.encoding.IntegerType
import com.hp.jipp.encoding.KeyValueType
import com.hp.jipp.encoding.EnumType
import com.hp.jipp.encoding.RangeOfIntegerType
import com.hp.jipp.encoding.ResolutionType
import com.hp.jipp.encoding.StringType
import com.hp.jipp.encoding.Tag
import com.hp.jipp.encoding.UriType
import com.hp.jipp.util.getStaticObjects

/** A library of attribute types as defined by RFC2911  */
class Types {

    companion object {
        // Note: it's easier to work with simple objects for each type of attribute. But then we can't type-constrain
        // attributes to appear only in the correct request/response/attribute group (although they could be runtime
        // constrained).

        // RFC2911 3.1.4.1 Request Operation Attributes
        // RFC2911 3.1.4.2 Response Operation Attributes

        @JvmField
        val attributesCharset = StringType(Tag.charset, "attributes-charset")

        @JvmField
        val attributesNaturalLanguage = StringType(Tag.naturalLanguage, "attributes-natural-language")

        // RFC2911 3.1.6 Operation Response Status Codes and Status Messages
        @JvmField
        val statusMessage = StringType(Tag.textWithoutLanguage, "status-message")

        @JvmField
        val detailedStatusMessage = StringType(Tag.textWithoutLanguage, "detailed-status-message")

        @JvmField
        val documentAccessError = StringType(Tag.textWithoutLanguage, "document-access-error")

        // Get-Printer-Attributes request fields

        @JvmField
        val requestingUserName = StringType(Tag.nameWithoutLanguage, "requesting-user-name")

        @JvmField
        val printerUri = UriType(Tag.uri, "printer-uri")

        @JvmField
        val printerUuid = UriType(Tag.uri, "printer-uuid")

        @JvmField
        val operationsSupported = Operation.Type("operations-supported")

        @JvmField
        val requestedAttributes = StringType(Tag.keyword, "requested-attributes")

        @JvmField
        val mediaSupported = MediaSize.Type("media-supported")

        @JvmField
        val mediaReady = MediaSize.Type("media-ready")

        @JvmField
        val mediaDefault = MediaSize.Type("media-default")

        // Get-Printer-Attributes response fields

        @JvmField
        val printerInfo = StringType(Tag.textWithoutLanguage, "printer-info")

        @JvmField
        val printerName = StringType(Tag.nameWithoutLanguage, "printer-name")

        @JvmField
        val printerDnsSdName = StringType(Tag.nameWithoutLanguage, "printer-dns-sd-name")

        @JvmField
        val printerState = com.hp.jipp.model.PrinterState.Type("printer-state")

        @JvmField
        val printerStateReasons = StringType(Tag.keyword, "printer-state-reasons")

        @JvmField
        val printerStateMessage = StringType(Tag.textWithoutLanguage, "printer-state-message")

        @JvmField
        val copiesSupported = RangeOfIntegerType("copies-supported")

        @JvmField
        val printerIcons = UriType(Tag.uri, "printer-icons")

        @JvmField
        val documentFormatSupported = StringType(Tag.mimeMediaType, "document-format-supported")

        @JvmField
        val printerUriSupported = UriType(Tag.uri, "printer-uri-supported")

        @JvmField
        val identifyActionsSupported = IdentifyAction.typeOf("identify-actions-supported")

        @JvmField
        val identifyActionsDefault = IdentifyAction.typeOf("identify-actions-default")

        // Printer Attributes in PWG 5100.9
        @JvmField
        val printerAlert = KeyValueType("printer-alert")

        // Printer Attributes in PWG 5100.13
        @JvmField
        val printerInputTray = KeyValueType("printer-input-tray")

        @JvmField
        val printerOutputTray = KeyValueType("printer-output-tray")

        @JvmField
        val printerSupply = KeyValueType("printer-supply")

        // Attributes in 5100.11
        @JvmField
        val jobPasswordEncryptionSupported = JobPasswordEncryption.Type("job-password-encryption-supported")

        // 3.2.1.1 Print-Job Request

        @JvmField
        val documentFormat = StringType(Tag.mimeMediaType, "document-format")

        @JvmField
        val media = MediaSize.Type("media")

        @JvmField
        val finishings = com.hp.jipp.model.Finishings.Type("finishings-default")

        // 3.2.1.1 Print-Job Response

        @JvmField
        val jobState = com.hp.jipp.model.JobState.Type("job-state")

        @JvmField
        val jobUri = UriType(Tag.uri, "job-uri")

        @JvmField
        val jobId = IntegerType(Tag.integerValue, "job-id")

        @JvmField
        val jobStateMessage = StringType(Tag.textWithoutLanguage, "job-state-message")

        @JvmField
        val jobStateReasons = StringType(Tag.keyword, "job-state-reasons")

        @JvmField
        val jobDetailedStatusMessages = StringType(Tag.textWithoutLanguage, "job-detailed-status-messages")

        @JvmField
        val finishingsDefault = com.hp.jipp.model.Finishings.Type("finishings-default")

        @JvmField
        val finishingsSupported: EnumType<Finishings> = com.hp.jipp.model.Finishings.Type("finishings-supported")

        // 3.2.6.1 Get-Jobs request
        @JvmField
        val myJobs = BooleanType(Tag.booleanValue, "my-jobs")

        // 3.3.1.1 Send-Document Request
        @JvmField
        val lastDocument = BooleanType(Tag.booleanValue, "last-document")

        // PWG5100.13: 4.1 Identify-Printer Request
        @JvmField
        val message = StringType(Tag.textWithoutLanguage, "message")

        @JvmField
        val identifyActions = IdentifyAction.typeOf("identify-actions")

        // Others

        @JvmField
        val jobName = StringType(Tag.nameWithoutLanguage, "job-name")

        @JvmField
        val documentName = StringType(Tag.nameWithoutLanguage, "document-name")

        @JvmField
        val printerResolutionDefault = ResolutionType(Tag.resolution, "printer-resolution-default")

        /** All known attributes */
        @JvmField
        val all = Types::class.java.getStaticObjects()
                .filter { it is AttributeType<*> }
                .map { it as AttributeType<*> }
    }
}
