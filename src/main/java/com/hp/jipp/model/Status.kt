package com.hp.jipp.model

import com.hp.jipp.encoding.encoderOf

/**
 * A status code, as found in a response packet. See RFC2911 section 13.1.
 */
data class Status(override val name: String, override val code: Int) : Code() {

    override fun toString() = name

    companion object {
        @JvmField val ok = Status("ok", 0x0000)
        @JvmField val clientErrorBadRequest = Status("client-error-bad-request", 0x0400)
        @JvmField val clientErrorForbidden = Status("client-error-forbidden", 0x0401)
        @JvmField val clientErrorNotAuthenticated = Status("client-error-not-authenticated", 0x0402)
        @JvmField val clientErrorNotAuthorized = Status("client-error-not-authorized", 0x0403)
        @JvmField val clientErrorNotPossible = Status("client-error-not-possible", 0x0404)
        @JvmField val clientErrorTimeout = Status("client-error-timeout", 0x0405)
        @JvmField val clientErrorNotFound = Status("client-error-not-found", 0x0406)
        @JvmField val clientErrorGone = Status("client-error-gone", 0x0407)
        @JvmField val clientErrorRequestEntityTooLarge =
                Status("client-error-request-entity-too-large", 0x0408)
        @JvmField val clientErrorRequestValueTooLong =
                Status("client-error-request-value-too-long", 0x0409)
        @JvmField val clientErrorDocumentFormatNotSupported =
                Status("client-error-document-format-not-supported", 0x040A)
        @JvmField val clientErrorAttributesOrValuesNotSupported =
                Status("client-error-attributes-or-values-not-supported", 0x040B)
        @JvmField val clientErrorUriSchemeNotSupported =
                Status("client-error-uri-scheme-not-supported", 0x040C)
        @JvmField val clientErrorCharsetNotSupported =
                Status("client-error-charset-not-supported", 0x040D)
        @JvmField val clientErrorConflictingAttributes =
                Status("client-error-conflicting-attributes", 0x040E)
        @JvmField val clientErrorCompressionNotSupported =
                Status("client-error-compression-not-supported", 0x040F)
        @JvmField val clientErrorCompressionError = Status("client-error-compression-error", 0x0410)
        @JvmField val clientErrorDocumentFormatError = Status("client-error-document-format-error", 0x0411)
        @JvmField val clientErrorDocumentAccessError = Status("client-error-document-access-error", 0x0412)

        @JvmField val serverErrorInternalError = Status("server-error-internal-error", 0x0500)
        @JvmField val serverErrorOperationNotSupported =
                Status("server-error-operation-not-supported", 0x0501)
        @JvmField val serverErrorServiceUnavailable = Status("server-error-service-unavailable", 0x0502)
        @JvmField val serverErrorVersionNotSupported = Status("server-error-version-not-supported", 0x0503)
        @JvmField val serverErrorDeviceError = Status("server-error-device-error", 0x0504)
        @JvmField val serverErrorTemporaryError = Status("server-error-temporary-error", 0x0505)
        @JvmField val serverErrorNotAcceptingJobs = Status("server-error-not-accepting-jobs", 0x0506)
        @JvmField val serverErrorBusy = Status("server-error-busy", 0x0507)
        @JvmField val serverErrorJobCanceled = Status("server-error-job-canceled", 0x0508)
        @JvmField val serverErrorMultipleDocumentJobsNotSupported =
                Status("server-error-multiple-document-jobs-not-supported", 0x0509)

        @JvmField
        val ENCODER = encoderOf(Status::class.java, { name, code -> Status(name, code) })
    }
}
