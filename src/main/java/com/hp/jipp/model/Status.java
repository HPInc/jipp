package com.hp.jipp.model;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import com.hp.jipp.encoding.NameCodeType;
import com.hp.jipp.encoding.NameCode;

import java.util.Collection;

/**
 * A status code, as found in a response packet. See RFC2911 section 13.1.
 */
@AutoValue
public abstract class Status extends NameCode {

    public static final Status Ok = create("ok", 0x0000);
    public static final Status ClientErrorBadRequest = create("client-error-bad-request", 0x0400);
    public static final Status ClientErrorForbidden = create("client-error-forbidden", 0x0401);
    public static final Status ClientErrorNotAuthenticated = create("client-error-not-authenticated", 0x0402);
    public static final Status ClientErrorNotAuthorized = create("client-error-not-authorized", 0x0403);
    public static final Status ClientErrorNotPossible = create("client-error-not-possible", 0x0404);
    public static final Status ClientErrorTimeout = create("client-error-timeout", 0x0405);
    public static final Status ClientErrorNotFound = create("client-error-not-found", 0x0406);
    public static final Status ClientErrorGone = create("client-error-gone", 0x0407);
    public static final Status ClientErrorRequestEntityTooLarge =
            create("client-error-request-entity-too-large", 0x0408);
    public static final Status ClientErrorRequestValueTooLong =
            create("client-error-request-value-too-long", 0x0409);
    public static final Status ClientErrorDocumentFormatNotSupported =
            create("client-error-document-format-not-supported", 0x040A);
    public static final Status ClientErrorAttributesOrValuesNotSupported =
            create("client-error-attributes-or-values-not-supported", 0x040B);
    public static final Status ClientErrorUriSchemeNotSupported =
            create("client-error-uri-scheme-not-supported", 0x040C);
    public static final Status ClientErrorCharsetNotSupported = create("client-error-charset-not-supported", 0x040D);
    public static final Status ClientErrorConflictingAttributes =
            create("client-error-conflicting-attributes", 0x040E);
    public static final Status ClientErrorCompressionNotSupported =
            create("client-error-compression-not-supported", 0x040F);
    public static final Status ClientErrorCompressionError = create("client-error-compression-error", 0x0410);
    public static final Status ClientErrorDocumentFormatError = create("client-error-document-format-error", 0x0411);
    public static final Status ClientErrorDocumentAccessError = create("client-error-document-access-error", 0x0412);

    public static final Status ServerErrorInternalError = create("server-error-internal-error", 0x0500);
    public static final Status ServerErrorOperationNotSupported =
            create("server-error-operation-not-supported", 0x0501);
    public static final Status ServerErrorServiceUnavailable = create("server-error-service-unavailable", 0x0502);
    public static final Status ServerErrorVersionNotSupported = create("server-error-version-not-supported", 0x0503);
    public static final Status ServerErrorDeviceError = create("server-error-device-error", 0x0504);
    public static final Status ServerErrorTemporaryError = create("server-error-temporary-error", 0x0505);
    public static final Status ServerErrorNotAcceptingJobs = create("server-error-not-accepting-jobs", 0x0506);
    public static final Status ServerErrorBusy = create("server-error-busy", 0x0507);
    public static final Status ServerErrorJobCanceled = create("server-error-job-canceled", 0x0508);
    public static final Status ServerErrorMultipleDocumentJobsNotSupported =
            create("server-error-multiple-document-jobs-not-supported", 0x0509);

    private final static Collection<Status> All = ImmutableSet.of(
            Ok, ClientErrorBadRequest, ClientErrorBadRequest, ClientErrorForbidden, ClientErrorNotAuthenticated,
            ClientErrorNotAuthorized, ClientErrorNotPossible, ClientErrorTimeout, ClientErrorNotFound, ClientErrorGone,
            ClientErrorRequestEntityTooLarge, ClientErrorRequestValueTooLong, ClientErrorDocumentFormatNotSupported,
            ClientErrorAttributesOrValuesNotSupported, ClientErrorUriSchemeNotSupported,
            ClientErrorCharsetNotSupported, ClientErrorConflictingAttributes, ClientErrorCompressionNotSupported,
            ClientErrorCompressionError, ClientErrorDocumentFormatError, ClientErrorDocumentAccessError,
            ServerErrorInternalError, ServerErrorOperationNotSupported, ServerErrorServiceUnavailable,
            ServerErrorVersionNotSupported, ServerErrorDeviceError, ServerErrorTemporaryError,
            ServerErrorNotAcceptingJobs, ServerErrorBusy, ServerErrorJobCanceled,
            ServerErrorMultipleDocumentJobsNotSupported
    );

    public final static NameCodeType.Encoder<Status> ENCODER = NameCodeType.encoder(
            "status-code", All, new NameCode.Factory<Status>() {
                @Override
                public Status create(String name, int code) {
                    return Status.create(name, code);
                }
            });

    /**
     * Return a new instance
     */
    public static Status create(String name, int code) {
        return new AutoValue_Status(name, code);
    }
}
