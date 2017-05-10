package com.hp.jipp.model;

import com.google.auto.value.AutoValue;
import com.hp.jipp.encoding.NameCode;
import com.hp.jipp.encoding.NameCodeType;

/**
 * A status code, as found in a response packet. See RFC2911 section 13.1.
 */
@AutoValue
public abstract class Status extends NameCode {

    public static final Status Ok = of("ok", 0x0000);
    public static final Status ClientErrorBadRequest = of("client-error-bad-request", 0x0400);
    public static final Status ClientErrorForbidden = of("client-error-forbidden", 0x0401);
    public static final Status ClientErrorNotAuthenticated = of("client-error-not-authenticated", 0x0402);
    public static final Status ClientErrorNotAuthorized = of("client-error-not-authorized", 0x0403);
    public static final Status ClientErrorNotPossible = of("client-error-not-possible", 0x0404);
    public static final Status ClientErrorTimeout = of("client-error-timeout", 0x0405);
    public static final Status ClientErrorNotFound = of("client-error-not-found", 0x0406);
    public static final Status ClientErrorGone = of("client-error-gone", 0x0407);
    public static final Status ClientErrorRequestEntityTooLarge =
            of("client-error-request-entity-too-large", 0x0408);
    public static final Status ClientErrorRequestValueTooLong =
            of("client-error-request-value-too-long", 0x0409);
    public static final Status ClientErrorDocumentFormatNotSupported =
            of("client-error-document-format-not-supported", 0x040A);
    public static final Status ClientErrorAttributesOrValuesNotSupported =
            of("client-error-attributes-or-values-not-supported", 0x040B);
    public static final Status ClientErrorUriSchemeNotSupported =
            of("client-error-uri-scheme-not-supported", 0x040C);
    public static final Status ClientErrorCharsetNotSupported = of("client-error-charset-not-supported", 0x040D);
    public static final Status ClientErrorConflictingAttributes =
            of("client-error-conflicting-attributes", 0x040E);
    public static final Status ClientErrorCompressionNotSupported =
            of("client-error-compression-not-supported", 0x040F);
    public static final Status ClientErrorCompressionError = of("client-error-compression-error", 0x0410);
    public static final Status ClientErrorDocumentFormatError = of("client-error-document-format-error", 0x0411);
    public static final Status ClientErrorDocumentAccessError = of("client-error-document-access-error", 0x0412);

    public static final Status ServerErrorInternalError = of("server-error-internal-error", 0x0500);
    public static final Status ServerErrorOperationNotSupported =
            of("server-error-operation-not-supported", 0x0501);
    public static final Status ServerErrorServiceUnavailable = of("server-error-service-unavailable", 0x0502);
    public static final Status ServerErrorVersionNotSupported = of("server-error-version-not-supported", 0x0503);
    public static final Status ServerErrorDeviceError = of("server-error-device-error", 0x0504);
    public static final Status ServerErrorTemporaryError = of("server-error-temporary-error", 0x0505);
    public static final Status ServerErrorNotAcceptingJobs = of("server-error-not-accepting-jobs", 0x0506);
    public static final Status ServerErrorBusy = of("server-error-busy", 0x0507);
    public static final Status ServerErrorJobCanceled = of("server-error-job-canceled", 0x0508);
    public static final Status ServerErrorMultipleDocumentJobsNotSupported =
            of("server-error-multiple-document-jobs-not-supported", 0x0509);

    public static final NameCodeType.Encoder<Status> ENCODER = NameCodeType.Encoder.of(
            Status.class, new NameCode.Factory<Status>() {
                @Override
                public Status of(String name, int code) {
                    return Status.of(name, code);
                }
            });

    /**
     * Construct and return a new instance
     */
    public static Status of(String name, int code) {
        return new AutoValue_Status(name, code);
    }
}
