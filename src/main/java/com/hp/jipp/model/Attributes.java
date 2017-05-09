package com.hp.jipp.model;

import com.google.common.collect.ImmutableList;
import com.hp.jipp.encoding.AttributeType;
import com.hp.jipp.encoding.BooleanType;
import com.hp.jipp.encoding.IntegerType;
import com.hp.jipp.encoding.NameCodeType;
import com.hp.jipp.encoding.RangeOfIntegerType;
import com.hp.jipp.encoding.ResolutionType;
import com.hp.jipp.encoding.StringType;
import com.hp.jipp.encoding.Tag;
import com.hp.jipp.encoding.UriType;
import com.hp.jipp.util.Util;

import java.util.List;

/** A library of attribute types as defined by RFC2911 */
public final class Attributes {

    // RFC2911 3.1.4.1 Request Operation Attributes
    // RFC2911 3.1.4.2 Response Operation Attributes

    public static final StringType AttributesCharset =
            new StringType(Tag.Charset, "attributes-charset");

    public static final StringType AttributesNaturalLanguage =
            new StringType(Tag.NaturalLanguage, "attributes-natural-language");

    // RFC2911 3.1.6 Operation Response Status Codes and Status Messages
    public static final StringType StatusMessage =
            new StringType(Tag.TextWithoutLanguage, "status-message");

    public static final StringType DetailedStatusMessage =
            new StringType(Tag.TextWithoutLanguage, "detailed-status-message");

    public static final StringType DocumentAccessError =
            new StringType(Tag.TextWithoutLanguage, "document-access-error");

    // Get-Printer-Attributes request fields

    public static final StringType RequestingUserName =
            new StringType(Tag.TextWithoutLanguage, "requesting-user-name");

    public static final UriType PrinterUri =
            new UriType(Tag.Uri, "printer-uri");

    public static final NameCodeType<Operation> OperationsSupported =
            Operation.typeOf("operations-supported");

    public static final StringType RequestedAttributes =
            new StringType(Tag.Keyword, "requested-attributes");

    public static final MediaSize.Type MediaSupported =
            new MediaSize.Type("media-supported");

    public static final MediaSize.Type MediaReady =
            new MediaSize.Type("media-ready");

    public static final MediaSize.Type MediaDefault =
            new MediaSize.Type("media-default");

    // Get-Printer-Attributes response fields

    public static final StringType PrinterInfo =
            new StringType(Tag.TextWithoutLanguage, "printer-info");

    public static final NameCodeType<PrinterState> PrinterState =
            NameCodeType.typeOf(com.hp.jipp.model.PrinterState.ENCODER, "printer-state");

    public static final StringType PrinterStateReasons =
            new StringType(Tag.Keyword, "printer-state-reasons");

    public static final StringType PrinterStateMessage =
            new StringType(Tag.TextWithoutLanguage, "printer-state-message");

    public static final RangeOfIntegerType CopiesSupported =
            new RangeOfIntegerType("copies-supported");

    public static final UriType PrinterIcons =
            new UriType(Tag.Uri, "printer-icons");

    public static final StringType DocumentFormatSupported =
            new StringType(Tag.MimeMediaType, "document-format-supported");

    public static final UriType PrinterUriSupported =
            new UriType(Tag.Uri, "printer-uri-supported");

    // 3.2.1.1 Print-Job Request

    public static final StringType DocumentFormat =
            new StringType(Tag.MimeMediaType, "document-format");

    public static final MediaSize.Type Media =
            new MediaSize.Type("media");

    // 3.2.1.1 Print-Job Response

    public static final NameCodeType<JobState> JobState =
            NameCodeType.typeOf(com.hp.jipp.model.JobState.ENCODER, "job-state");

    public static final UriType JobUri = new UriType(Tag.Uri, "job-uri");

    public static final IntegerType JobId = new IntegerType(Tag.IntegerValue, "job-id");

    public static final StringType JobStateMessage =
            new StringType(Tag.TextWithoutLanguage, "job-state-message");

    public static final StringType JobStateReasons =
            new StringType(Tag.Keyword, "job-state-reasons");

    public static final StringType JobDetailedStatusMessages =
            new StringType(Tag.TextWithoutLanguage, "job-detailed-status-messages");

    // 3.2.6.1 Get-Jobs request
    public static final BooleanType MyJobs = new BooleanType(Tag.BooleanValue, "my-jobs");

    // 3.3.1.1 Send-Document Request
    public static final BooleanType LastDocument =
            new BooleanType(Tag.BooleanValue, "last-document");

    // Others

    public static final StringType JobName =
            new StringType(Tag.NameWithoutLanguage, "job-name");

    public static final StringType DocumentName =
            new StringType(Tag.NameWithoutLanguage, "document-name");

    public static final ResolutionType PrinterResolutionDefault =
            new ResolutionType(Tag.Resolution, "printer-resolution-default");

    /** All known attributes */
    public static final List<AttributeType<?>> All = staticMembers(Attributes.class);

    /** Return all accessible static members of the specified class which are AttributeType objects */
    public static List<AttributeType<?>> staticMembers(Class<?> cls) {
        ImmutableList.Builder<AttributeType<?>> members = new ImmutableList.Builder<>();
        for (Object object : Util.getStaticObjects(cls)) {
            if (object instanceof AttributeType<?>) {
                members.add((AttributeType<?>) object);
            }
        }
        return members.build();
    }
}
