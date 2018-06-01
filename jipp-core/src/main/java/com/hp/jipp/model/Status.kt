// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.model

import com.hp.jipp.encoding.EnumType

/**
 * A status code, as found in a response packet. See RFC2911 section 13.1.
 */
data class Status(override val code: Int, override val name: String) : Code() {

    override fun toString() = "$name(x${Integer.toHexString(code)})"

    /** Raw codes which may be used for direct comparisons */
    object Code {
        const val ok = 0x0000
        const val okIgnoredOrSubstitutedAttributes = 0x0001
        const val okConflictingAttributes = 0x0002
        const val clientErrorBadRequest = 0x0400
        const val clientErrorForbidden = 0x0401
        const val clientErrorNotAuthenticated = 0x0402
        const val clientErrorNotAuthorized = 0x0403
        const val clientErrorNotPossible = 0x0404
        const val clientErrorTimeout = 0x0405
        const val clientErrorNotFound = 0x0406
        const val clientErrorGone = 0x0407
        const val clientErrorRequestEntityTooLarge = 0x0408
        const val clientErrorRequestValueTooLong = 0x0409
        const val clientErrorDocumentFormatNotSupported = 0x040A
        const val clientErrorAttributesOrValuesNotSupported = 0x040B
        const val clientErrorUriSchemeNotSupported = 0x040C
        const val clientErrorCharsetNotSupported = 0x040D
        const val clientErrorConflictingAttributes = 0x040E
        const val clientErrorCompressionNotSupported = 0x040F
        const val clientErrorCompressionError = 0x0410
        const val clientErrorDocumentFormatError = 0x0411
        const val clientErrorDocumentAccessError = 0x0412
        const val serverErrorInternalError = 0x0500
        const val serverErrorOperationNotSupported = 0x0501
        const val serverErrorServiceUnavailable = 0x0502
        const val serverErrorVersionNotSupported = 0x0503
        const val serverErrorDeviceError = 0x0504
        const val serverErrorTemporaryError = 0x0505
        const val serverErrorNotAcceptingJobs = 0x0506
        const val serverErrorBusy = 0x0507
        const val serverErrorJobCanceled = 0x0508
        const val serverErrorMultipleDocumentJobsNotSupported = 0x0509
    }

    companion object {
        @JvmField val ok = Status(Code.ok, "successful-ok")
        @JvmField val okIgnoredOrSubstitutedAttributes =
            Status(Code.okIgnoredOrSubstitutedAttributes, "successful-ok-ignored-or-substituted-attributes")
        @JvmField val okConflictingAttributes =
            Status(Code.okConflictingAttributes, "successful-ok-conflicting-attributes")
        @JvmField val clientErrorBadRequest = Status(Code.clientErrorBadRequest, "client-error-bad-request")
        @JvmField val clientErrorForbidden = Status(Code.clientErrorForbidden, "client-error-forbidden")
        @JvmField val clientErrorNotAuthenticated =
            Status(Code.clientErrorNotAuthenticated, "client-error-not-authenticated")
        @JvmField val clientErrorNotAuthorized = Status(Code.clientErrorNotAuthorized, "client-error-not-authorized")
        @JvmField val clientErrorNotPossible = Status(Code.clientErrorNotPossible, "client-error-not-possible")
        @JvmField val clientErrorTimeout = Status(Code.clientErrorTimeout, "client-error-timeout")
        @JvmField val clientErrorNotFound = Status(Code.clientErrorNotFound, "client-error-not-found")
        @JvmField val clientErrorGone = Status(Code.clientErrorGone, "client-error-gone")
        @JvmField val clientErrorRequestEntityTooLarge =
            Status(Code.clientErrorRequestEntityTooLarge, "client-error-request-entity-too-large")
        @JvmField val clientErrorRequestValueTooLong =
            Status(Code.clientErrorRequestValueTooLong, "client-error-request-value-too-long")
        @JvmField val clientErrorDocumentFormatNotSupported =
            Status(Code.clientErrorDocumentFormatNotSupported, "client-error-document-format-not-supported")
        @JvmField val clientErrorAttributesOrValuesNotSupported =
            Status(Code.clientErrorAttributesOrValuesNotSupported,
                "client-error-attributes-or-values-not-supported")
        @JvmField val clientErrorUriSchemeNotSupported =
            Status(Code.clientErrorUriSchemeNotSupported, "client-error-uri-scheme-not-supported")
        @JvmField val clientErrorCharsetNotSupported =
            Status(Code.clientErrorCharsetNotSupported, "client-error-charset-not-supported")
        @JvmField val clientErrorConflictingAttributes =
            Status(Code.clientErrorConflictingAttributes, "client-error-conflicting-attributes")
        @JvmField val clientErrorCompressionNotSupported =
            Status(Code.clientErrorCompressionNotSupported, "client-error-compression-not-supported")
        @JvmField val clientErrorCompressionError =
            Status(Code.clientErrorCompressionError, "client-error-compression-error")
        @JvmField val clientErrorDocumentFormatError =
            Status(Code.clientErrorDocumentFormatError, "client-error-document-format-error")
        @JvmField val clientErrorDocumentAccessError =
            Status(Code.clientErrorDocumentAccessError, "client-error-document-access-error")
        @JvmField val serverErrorInternalError = Status(Code.serverErrorInternalError, "server-error-internal-error")
        @JvmField val serverErrorOperationNotSupported =
            Status(Code.serverErrorOperationNotSupported, "server-error-operation-not-supported")
        @JvmField val serverErrorServiceUnavailable =
            Status(Code.serverErrorServiceUnavailable, "server-error-service-unavailable")
        @JvmField val serverErrorVersionNotSupported =
            Status(Code.serverErrorVersionNotSupported, "server-error-version-not-supported")
        @JvmField val serverErrorDeviceError = Status(Code.serverErrorDeviceError, "server-error-device-error")
        @JvmField val serverErrorTemporaryError =
            Status(Code.serverErrorTemporaryError, "server-error-temporary-error")
        @JvmField val serverErrorNotAcceptingJobs =
            Status(Code.serverErrorNotAcceptingJobs, "server-error-not-accepting-jobs")
        @JvmField val serverErrorBusy = Status(Code.serverErrorBusy, "server-error-busy")
        @JvmField val serverErrorJobCanceled = Status(Code.serverErrorJobCanceled, "server-error-job-canceled")
        @JvmField val serverErrorMultipleDocumentJobsNotSupported =
            Status(Code.serverErrorMultipleDocumentJobsNotSupported,
                "server-error-multiple-document-jobs-not-supported")

        @JvmField
        val Encoder = EnumType.Encoder(Status::class.java) { code, name ->
            Status(code, name)
        }
    }
}
