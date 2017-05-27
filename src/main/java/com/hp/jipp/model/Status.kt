package com.hp.jipp.model

import com.hp.jipp.encoding.NameCode
import com.hp.jipp.encoding.NameCodeType

/**
 * A status code, as found in a response packet. See RFC2911 section 13.1.
 */
data class Status(override val name: String, override val code: Int) : Code() {
    companion object {

        @JvmField val Ok = Status("ok", 0x0000)
        @JvmField val ClientErrorBadRequest = Status("client-error-bad-request", 0x0400)
        @JvmField val ClientErrorForbidden = Status("client-error-forbidden", 0x0401)
        @JvmField val ClientErrorNotAuthenticated = Status("client-error-not-authenticated", 0x0402)
        @JvmField val ClientErrorNotAuthorized = Status("client-error-not-authorized", 0x0403)
        @JvmField val ClientErrorNotPossible = Status("client-error-not-possible", 0x0404)
        @JvmField val ClientErrorTimeout = Status("client-error-timeout", 0x0405)
        @JvmField val ClientErrorNotFound = Status("client-error-not-found", 0x0406)
        @JvmField val ClientErrorGone = Status("client-error-gone", 0x0407)
        @JvmField val ClientErrorRequestEntityTooLarge = Status("client-error-request-entity-too-large", 0x0408)
        @JvmField val ClientErrorRequestValueTooLong = Status("client-error-request-value-too-long", 0x0409)
        @JvmField val ClientErrorDocumentFormatNotSupported = Status("client-error-document-format-not-supported", 0x040A)
        @JvmField val ClientErrorAttributesOrValuesNotSupported = Status("client-error-attributes-or-values-not-supported", 0x040B)
        @JvmField val ClientErrorUriSchemeNotSupported = Status("client-error-uri-scheme-not-supported", 0x040C)
        @JvmField val ClientErrorCharsetNotSupported = Status("client-error-charset-not-supported", 0x040D)
        @JvmField val ClientErrorConflictingAttributes = Status("client-error-conflicting-attributes", 0x040E)
        @JvmField val ClientErrorCompressionNotSupported = Status("client-error-compression-not-supported", 0x040F)
        @JvmField val ClientErrorCompressionError = Status("client-error-compression-error", 0x0410)
        @JvmField val ClientErrorDocumentFormatError = Status("client-error-document-format-error", 0x0411)
        @JvmField val ClientErrorDocumentAccessError = Status("client-error-document-access-error", 0x0412)

        @JvmField val ServerErrorInternalError = Status("server-error-internal-error", 0x0500)
        @JvmField val ServerErrorOperationNotSupported = Status("server-error-operation-not-supported", 0x0501)
        @JvmField val ServerErrorServiceUnavailable = Status("server-error-service-unavailable", 0x0502)
        @JvmField val ServerErrorVersionNotSupported = Status("server-error-version-not-supported", 0x0503)
        @JvmField val ServerErrorDeviceError = Status("server-error-device-error", 0x0504)
        @JvmField val ServerErrorTemporaryError = Status("server-error-temporary-error", 0x0505)
        @JvmField val ServerErrorNotAcceptingJobs = Status("server-error-not-accepting-jobs", 0x0506)
        @JvmField val ServerErrorBusy = Status("server-error-busy", 0x0507)
        @JvmField val ServerErrorJobCanceled = Status("server-error-job-canceled", 0x0508)
        @JvmField val ServerErrorMultipleDocumentJobsNotSupported = Status("server-error-multiple-document-jobs-not-supported", 0x0509)

        @JvmField
        val ENCODER: NameCodeType.Encoder<Status> = NameCodeType.Encoder.of(
                Status::class.java, object : NameCode.Factory<Status> {
            override fun of(name: String, code: Int) = Status(name, code)
        })

    }
}
