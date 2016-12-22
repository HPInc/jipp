package com.hp.jipp.model;

import java.util.HashMap;

/**
 * Created by gladed on 10/15/16.
 */

public class StatusCodes {
    public static final int ClientErrorBadRequest = 0x0400;
    public static final int ClientErrorForbidden = 0x0401;
    public static final int ClientErrorNotAuthenticated = 0x0402;
    public static final int ClientErrorNotAuthorized = 0x0403;
    public static final int ClientErrorNotPossible = 0x0404;
    public static final int ClientErrorTimeout = 0x0405;
    public static final int ClientErrorNotFound = 0x0406;
    public static final int ClientErrorGone = 0x0407;
    public static final int ClientErrorRequestEntityTooLarge = 0x0408;
    public static final int ClientErrorRequestValueTooLong = 0x0409;
    public static final int ClientErrorDocumentFormatNotSupported = 0x040A;
    public static final int ClientErrorAttributesOrValuesNotSupported = 0x040B;
    public static final int ClientErrorUriSchemeNotSupported = 0x040C;
    public static final int ClientErrorCharsetNotSupported = 0x040D;
    public static final int ClientErrorConflictingAttributes = 0x040E;
    public static final int ClientErrorCompressionNotSupported = 0x040F;
    public static final int ClientErrorCompressionError = 0x0410;
    public static final int ClientErrorDocumentFormatError = 0x0411;
    public static final int ClientErrorDocumentAccessError = 0x0412;

    public static final int ServerErrorInternalError = 0x0500;
    public static final int ServerErrorOperationNotSupported = 0x0501;
    public static final int ServerErrorServiceUnavailable = 0x0502;
    public static final int ServerErrorVersionNotSupported = 0x0503;
    public static final int ServerErrorDeviceError = 0x0504;
    public static final int ServerErrorTemporaryError = 0x0505;
    public static final int ServerErrorNotAcceptingJobs = 0x0506;
    public static final int ServerErrorBusy = 0x0507;
    public static final int ServerErrorJobCanceled = 0x0508;
    public static final int ServerErrorMultipleDocumentJobsNotSupported = 0x0509;


    private final static HashMap<Integer, String> map = new HashMap<Integer, String>() {{
        put(ClientErrorBadRequest, "client-error-bad-request");
        put(ClientErrorForbidden, "client-error-forbidden");
        put(ClientErrorNotAuthenticated, "client-error-not-authenticated");
        put(ClientErrorNotAuthorized, "client-error-not-authorized");
        put(ClientErrorNotPossible, "client-error-not-possible");
        put(ClientErrorTimeout, "client-error-timeout");
        put(ClientErrorNotFound, "client-error-not-found");
        put(ClientErrorGone, "client-error-gone");
        put(ClientErrorRequestEntityTooLarge, "client-error-request-entity-too-large");
        put(ClientErrorRequestValueTooLong, "client-error-request-value-too-long");
        put(ClientErrorDocumentFormatNotSupported, "client-error-document-format-not-supported");
        put(ClientErrorAttributesOrValuesNotSupported, "client-error-attributes-or-values-not-supported");
        put(ClientErrorUriSchemeNotSupported, "client-error-uri-scheme-not-supported");
        put(ClientErrorCharsetNotSupported, "client-error-charset-not-supported");
        put(ClientErrorConflictingAttributes, "client-error-conflicting-attributes");
        put(ClientErrorCompressionNotSupported, "client-error-compression-not-supported");
        put(ClientErrorCompressionError, "client-error-compression-error");
        put(ClientErrorDocumentFormatError, "client-error-document-format-error");
        put(ClientErrorDocumentAccessError, "client-error-document-access-error");

        put(ServerErrorInternalError, "server-error-internal-error");
        put(ServerErrorOperationNotSupported, "server-error-operation-not-supported");
        put(ServerErrorServiceUnavailable, "server-error-service-unavailable");
        put(ServerErrorVersionNotSupported, "server-error-version-not-supported");
        put(ServerErrorDeviceError, "server-error-device-error");
        put(ServerErrorTemporaryError, "server-error-temporary-error");
        put(ServerErrorNotAcceptingJobs, "server-error-not-accepting-jobs");
        put(ServerErrorBusy, "server-error-busy");
        put(ServerErrorJobCanceled, "server-error-job-canceled");
        put(ServerErrorMultipleDocumentJobsNotSupported, "server-error-multiple-document-jobs-not-supported");
    }};

    public static String toString(int id) {
        String result = map.get(id);
        if (result == null) {
            result = "0x" + Integer.toHexString(id);
        }
        return result;
    }

}
