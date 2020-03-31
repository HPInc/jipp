// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.trans

import com.hp.jipp.encoding.Attribute
import com.hp.jipp.encoding.AttributeGroup
import com.hp.jipp.encoding.AttributeType
import com.hp.jipp.encoding.DelimiterTag
import com.hp.jipp.encoding.MutableAttributeGroup
import com.hp.jipp.encoding.Tag
import com.hp.jipp.model.JobState
import com.hp.jipp.model.JobStateReason
import com.hp.jipp.model.Operation
import com.hp.jipp.model.Status
import com.hp.jipp.model.Types
import com.hp.jipp.util.PrettyPrinter
import java.net.URI

/**
 * An IPP packet consisting of header information and zero or more attribute groups.
 */
@Suppress("TooManyFunctions")
data class IppPacket constructor(
    val versionNumber: Int = DEFAULT_VERSION_NUMBER,
    val code: Int,
    val requestId: Int,
    val attributeGroups: List<AttributeGroup> = listOf()
) {
    @JvmOverloads
    constructor(
        versionNumber: Int = DEFAULT_VERSION_NUMBER,
        code: Int,
        requestId: Int,
        vararg groups: AttributeGroup
    ) : this(versionNumber, code, requestId, groups.toList())

    constructor(status: Status, requestId: Int, vararg groups: AttributeGroup) :
        this(code = status.code, requestId = requestId, attributeGroups = groups.toList())

    constructor(operation: Operation, requestId: Int, vararg groups: AttributeGroup) :
        this(code = operation.code, requestId = requestId, attributeGroups = groups.toList())

    /** Return this response packet's code as a [Status]. */
    val status: Status by lazy {
        Status[code]
    }

    /** Return this request packet's code as an [Operation] */
    val operation: Operation by lazy {
        Operation[code]
    }

    /** Get the attribute group having a delimiter */
    operator fun get(groupDelimiter: Tag): AttributeGroup? =
        attributeGroups.firstOrNull { it.tag == groupDelimiter }

    /** Return all values found within the specified group and having the same attribute type */
    fun <T : Any> getValues(groupDelimiter: Tag, type: AttributeType<T>): List<T> =
        this[groupDelimiter]?.get(type) ?: listOf()

    /** Return the first value within the group of [groupDelimiter] and [type]. */
    fun <T : Any> getValue(groupDelimiter: Tag, type: AttributeType<T>): T? =
        this[groupDelimiter]?.get(type)?.get(0)

    /** Return the string form of any attribute values within the group of [groupDelimiter] and [type]. */
    fun <T : Any> getStrings(groupDelimiter: Tag, type: AttributeType<T>): List<String> =
        this[groupDelimiter]?.getStrings(type) ?: listOf()

    /** Return the string form of the first attribute value within the group of [groupDelimiter] and [type]. */
    fun <T : Any> getString(groupDelimiter: Tag, type: AttributeType<T>): String? =
        this[groupDelimiter]?.getString(type)

    /** Make a copy of this packet but replace with the supplied attribute groups */
    fun withAttributeGroups(attributeGroups: List<AttributeGroup>): IppPacket =
        copy(attributeGroups = attributeGroups)

    /** Return a pretty-printed version of this packet (including separators and line breaks) */
    fun prettyPrint(maxWidth: Int, indent: String) = PrettyPrinter(prefix(), PrettyPrinter.OBJECT, indent, maxWidth)
        .addAll(attributeGroups)
        .print()

    private fun prefix(): String {
        return "IppPacket(v=0x" + Integer.toHexString(versionNumber) +
            ", c=" + statusOrOperationString(code) +
            ", r=0x" + Integer.toHexString(requestId) +
            ")"
    }

    private fun statusOrOperationString(code: Int) =
        (Operation.all[code] ?: Status.all[code] ?: code).toString()

    override fun toString(): String {
        return prefix() + " " + attributeGroups
    }

    class Builder
    @JvmOverloads
    constructor(
        var code: Int,
        var versionNumber: Int = DEFAULT_VERSION_NUMBER,
        var requestId: Int = DEFAULT_REQUEST_ID
    ) {
        constructor(
            status: Status,
            versionNumber: Int = DEFAULT_VERSION_NUMBER,
            requestId: Int = DEFAULT_REQUEST_ID
        ) : this(status.code, versionNumber, requestId)

        constructor(
            operation: Operation,
            versionNumber: Int = DEFAULT_VERSION_NUMBER,
            requestId: Int = DEFAULT_REQUEST_ID
        ) : this(operation.code, versionNumber, requestId)

        private val groups: MutableList<MutableAttributeGroup> = mutableListOf()

        init {
            // All packets must have an operation attributes group with these initial attributes
            putOperationAttributes(
                Types.attributesNaturalLanguage.of(DEFAULT_LANGUAGE),
                Types.attributesCharset.of(DEFAULT_CHARSET))
        }

        fun setVersionNumber(versionNumber: Int) = apply {
            this.versionNumber = versionNumber
        }

        fun setRequestId(requestId: Int) = apply {
            this.requestId = requestId
        }

        fun setCode(code: Int) = apply {
            this.code = code
        }

        /** Append a new [AttributeGroup] after other groups. */
        fun addGroup(group: AttributeGroup) = apply {
            groups.add(group.toMutable())
        }

        /** Return the last group with the specified tag, creating it if necessary */
        fun getOrCreateGroup(tag: DelimiterTag) =
            groups.findLast { it.tag == tag } ?: MutableAttributeGroup(tag).also { groups.add(it) }

        /** Get or create a group with [tag] and add or replace [attributes] in it. */
        fun putAttributes(tag: DelimiterTag, attributes: Iterable<Attribute<*>>) = apply {
            getOrCreateGroup(tag) += attributes
        }

        /** Get or create a group with [tag] and add or replace [attributes] in it. */
        fun putAttributes(tag: DelimiterTag, vararg attributes: Attribute<*>) =
            putAttributes(tag, attributes.toList())

        /** Get the [Tag.operationAttributes] group and add or replace [attributes] in it. */
        fun putOperationAttributes(attributes: Iterable<Attribute<*>>) =
            putAttributes(Tag.operationAttributes, attributes)

        /** Get the [Tag.operationAttributes] group and add or replace [attributes] in it. */
        fun putOperationAttributes(vararg attributes: Attribute<*>) =
            putOperationAttributes(attributes.toList())

        /** Get or create the [Tag.jobAttributes] group and add or replace [attributes] in it. */
        fun putJobAttributes(attributes: Iterable<Attribute<*>>) =
            putAttributes(Tag.jobAttributes, attributes)

        /** Get or create the [Tag.jobAttributes] group and add or replace [attributes] in it. */
        fun putJobAttributes(vararg attributes: Attribute<*>) =
            putJobAttributes(attributes.toList())

        /** Get or create the [Tag.printerAttributes] group and add or replace [attributes] in it. */
        fun putPrinterAttributes(attributes: Iterable<Attribute<*>>) =
            putAttributes(Tag.printerAttributes, attributes)

        /** Get or create the [Tag.printerAttributes] group and add or replace [attributes] in it. */
        fun putPrinterAttributes(vararg attributes: Attribute<*>) =
            putPrinterAttributes(attributes.toList())

        /** Get or create the [Tag.unsupportedAttributes] group and add or replace [attributes] in it. */
        fun putUnsupportedAttributes(attributes: Iterable<Attribute<*>>) =
            putAttributes(Tag.unsupportedAttributes, attributes)

        /** Get or create the [Tag.unsupportedAttributes] group and add or replace [attributes] in it. */
        fun putUnsupportedAttributes(vararg attributes: Attribute<*>) =
            putUnsupportedAttributes(attributes.toList())

        /** Add a new [Tag.jobAttributes] group containing default attributes. */
        @JvmOverloads
        fun addJobAttributesGroup(
            /** The job-id to be used to identify this job in future requests. */
            jobId: Int,
            /** The job-uri to be used as a target for future requests. */
            jobUri: URI,
            /** The current job-state. */
            jobState: JobState,
            /** A list of job-state-reasons, if any. */
            jobStateReasons: List<String> = listOf(JobStateReason.none),
            /** Other job attributes, if any. */
            vararg attributes: Attribute<*>
        ) = apply {
            addGroup(MutableAttributeGroup(Tag.jobAttributes, listOf(
                Types.jobId.of(jobId),
                Types.jobUri.of(jobUri),
                Types.jobState.of(jobState),
                Types.jobStateReasons.of(jobStateReasons)) + attributes.toList()))
        }

        /** Construct and return an [IppPacket] containing all settings given to this [Builder]. */
        fun build() = IppPacket(versionNumber, code, requestId,
            // Strip out any empty unsupported-attributes or job-attributes groups.
            groups.filterNot { (it.tag == Tag.unsupportedAttributes || it.tag == Tag.jobAttributes) && it.isEmpty() })
    }

    companion object {
        /** Default version number for IPP packets (0x200 for IPP 2.0). */
        const val DEFAULT_VERSION_NUMBER = 0x0200

        /** Default request ID (1). */
        const val DEFAULT_REQUEST_ID = 1

        /** Default language to use in operation groups ("en-us"). */
        const val DEFAULT_LANGUAGE = "en-us"

        /** Default charset to use in operation groups ("utf-8"). */
        const val DEFAULT_CHARSET = "utf-8"

        /** Return a Get-Printer-Attributes request [Builder]. */
        @JvmStatic
        fun getPrinterAttributes(
            printerUri: URI,
            /** Printer attributes of interest. */
            types: Iterable<AttributeType<*>>
        ) = Builder(Operation.getPrinterAttributes.code)
            .putAttributes(Tag.operationAttributes, Types.printerUri.of(printerUri))
            .putRequestedAttributes(types.toList())

        /** Return a Get-Printer-Attributes request [Builder]. */
        @JvmStatic
        fun getPrinterAttributes(
            printerUri: URI,
            /** Printer attributes of interest. */
            vararg types: AttributeType<*>
        ) = getPrinterAttributes(printerUri, types.toList())

        /** If supplied types are not empty, attach them as requested attributes. */
        private fun Builder.putRequestedAttributes(types: List<AttributeType<*>>) = apply {
            if (types.isNotEmpty()) {
                putAttributes(Tag.operationAttributes,
                    Types.requestedAttributes.of(types.toList().map { it.name }))
            }
        }

        /** Return a Validate-Job request [Builder]. */
        @JvmStatic
        fun validateJob(
            printerUri: URI
        ) = Builder(Operation.validateJob.code)
            .putAttributes(Tag.operationAttributes, Types.printerUri.of(printerUri))

        /** Return a Print-Job request [Builder]. */
        @JvmStatic
        fun printJob(
            printerUri: URI
        ) = Builder(code = Operation.printJob.code)
            .putAttributes(Tag.operationAttributes, Types.printerUri.of(printerUri))

        /** Return a Create-Job request [Builder]. */
        @JvmStatic
        fun createJob(
            printerUri: URI
        ) = Builder(Operation.createJob.code)
            .putAttributes(Tag.operationAttributes, Types.printerUri.of(printerUri))

        /** Return a Get-Jobs request [Builder]. */
        @JvmStatic
        fun getJobs(
            printerUri: URI,
            /** Job attributes of interest. */
            vararg types: AttributeType<*>
        ) = Builder(Operation.getJobs.code)
            .putAttributes(Tag.operationAttributes, Types.printerUri.of(printerUri))
            .putRequestedAttributes(types.toList())

        /** Return a Send-Document request [Builder]. */
        @JvmStatic
        fun sendDocument(
            printerUri: URI,
            jobId: Int
        ) = Builder(Operation.sendDocument.code)
            .putAttributes(Tag.operationAttributes,
                Types.printerUri.of(printerUri),
                Types.jobId.of(jobId))

        /** Return a Send-Document request [Builder] */
        @JvmStatic
        fun sendDocument(
            jobUri: URI,
            /** Job attributes of interest. */
            vararg types: AttributeType<*>
        ) = Builder(Operation.sendDocument.code)
            .putAttributes(Tag.operationAttributes, Types.jobUri.of(jobUri))
            .putRequestedAttributes(types.toList())

        /** Return a Get-Job-Attributes request [Builder]. */
        @JvmStatic
        fun getJobAttributes(
            printerUri: URI,
            jobId: Int,
            /** Job attributes of interest. */
            vararg types: AttributeType<*>
        ) = Builder(Operation.getJobAttributes.code)
            .putAttributes(Tag.operationAttributes,
                Types.printerUri.of(printerUri),
                Types.jobId.of(jobId))
            .putRequestedAttributes(types.toList())

        /** Return a Get-Job-Attributes request [Builder]. */
        @JvmStatic
        fun getJobAttributes(
            jobUri: URI,
            /** Types of interest, if any. */
            vararg types: AttributeType<*>
        ) = Builder(Operation.getJobAttributes.code)
            .putAttributes(Tag.operationAttributes, Types.jobUri.of(jobUri))
            .putRequestedAttributes(types.toList())

        /** Return a Cancel-Job request [Builder]. */
        @JvmStatic
        fun cancelJob(
            printerUri: URI,
            jobId: Int
        ) = Builder(Operation.cancelJob.code)
            .putAttributes(Tag.operationAttributes,
                Types.printerUri.of(printerUri),
                Types.jobId.of(jobId))

        /** Return a Cancel-Job request [Builder]. */
        @JvmStatic
        fun cancelJob(
            jobUri: URI
        ) = Builder(Operation.cancelJob.code)
            .putAttributes(Tag.operationAttributes, Types.jobUri.of(jobUri))

        /** Return a generic response [Builder]. */
        @JvmStatic
        fun response(
            status: Status
        ) = Builder(status.code)
            .putAttributes(Tag.unsupportedAttributes)

        /** Return a job-related response packet [Builder]. */
        @JvmStatic
        @JvmOverloads
        @Suppress("LongParameterList") // All of these parameters required in Job responses.
        fun jobResponse(
            /** The status code for the request. */
            status: Status,
            /** The job-id to be used to identify this job in future requests. */
            jobId: Int,
            /** The job-uri to be used as a target for future requests. */
            jobUri: URI,
            /** The current job-state. */
            jobState: JobState,
            /** A list of job-state-reasons, if any. */
            jobStateReasons: List<String> = listOf(JobStateReason.none)
        ) = Builder(status.code)
            .putAttributes(Tag.unsupportedAttributes)
            .addJobAttributesGroup(jobId, jobUri, jobState, jobStateReasons)
    }
}
