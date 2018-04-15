// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.model

import com.hp.jipp.encoding.EnumType

/**
 * A status code, as found in a response packet. See RFC2911 section 13.1.
 */
data class Status(override val code: Int, override val name: String) : Code() {

    override fun toString() = name

    companion object {
        @JvmField val ok = Status(0x0000, "successful-ok")
        @JvmField val okIgnoredOrSubstitutedAttributes =
                Status(0x0001, "successful-ok-ignored-or-substituted-attributes")
        @JvmField val okConflictingAttributes = Status(0x0002, "successful-ok-conflicting-attributes")
        @JvmField val clientErrorBadRequest = Status(0x0400, "client-error-bad-request")
        @JvmField val clientErrorForbidden = Status(0x0401, "client-error-forbidden")
        @JvmField val clientErrorNotAuthenticated = Status(0x0402, "client-error-not-authenticated")
        @JvmField val clientErrorNotAuthorized = Status(0x0403, "client-error-not-authorized")
        @JvmField val clientErrorNotPossible = Status(0x0404, "client-error-not-possible")
        @JvmField val clientErrorTimeout = Status(0x0405, "client-error-timeout")
        @JvmField val clientErrorNotFound = Status(0x0406, "client-error-not-found")
        @JvmField val clientErrorGone = Status(0x0407, "client-error-gone")
        @JvmField val clientErrorRequestEntityTooLarge =
                Status(0x0408, "client-error-request-entity-too-large")
        @JvmField val clientErrorRequestValueTooLong =
                Status(0x0409, "client-error-request-value-too-long")
        @JvmField val clientErrorDocumentFormatNotSupported =
                Status(0x040A, "client-error-document-format-not-supported")
        @JvmField val clientErrorAttributesOrValuesNotSupported =
                Status(0x040B, "client-error-attributes-or-values-not-supported")
        @JvmField val clientErrorUriSchemeNotSupported =
                Status(0x040C, "client-error-uri-scheme-not-supported")
        @JvmField val clientErrorCharsetNotSupported =
                Status(0x040D, "client-error-charset-not-supported")
        @JvmField val clientErrorConflictingAttributes =
                Status(0x040E, "client-error-conflicting-attributes")
        @JvmField val clientErrorCompressionNotSupported =
                Status(0x040F, "client-error-compression-not-supported")
        @JvmField val clientErrorCompressionError = Status(0x0410, "client-error-compression-error")
        @JvmField val clientErrorDocumentFormatError = Status(0x0411, "client-error-document-format-error")
        @JvmField val clientErrorDocumentAccessError = Status(0x0412, "client-error-document-access-error")
        @JvmField val serverErrorInternalError = Status(0x0500, "server-error-internal-error")
        @JvmField val serverErrorOperationNotSupported =
                Status(0x0501, "server-error-operation-not-supported")
        @JvmField val serverErrorServiceUnavailable = Status(0x0502, "server-error-service-unavailable")
        @JvmField val serverErrorVersionNotSupported = Status(0x0503, "server-error-version-not-supported")
        @JvmField val serverErrorDeviceError = Status(0x0504, "server-error-device-error")
        @JvmField val serverErrorTemporaryError = Status(0x0505, "server-error-temporary-error")
        @JvmField val serverErrorNotAcceptingJobs = Status(0x0506, "server-error-not-accepting-jobs")
        @JvmField val serverErrorBusy = Status(0x0507, "server-error-busy")
        @JvmField val serverErrorJobCanceled = Status(0x0508, "server-error-job-canceled")
        @JvmField val serverErrorMultipleDocumentJobsNotSupported =
                Status(0x0509, "server-error-multiple-document-jobs-not-supported")

        @JvmField
        val Encoder = EnumType.Encoder(Status::class.java) { code, name ->
            Status(code, name)
        }
    }
}
