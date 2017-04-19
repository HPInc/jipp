package com.hp.jipp.model;

import com.hp.jipp.encoding.AttributeType;
import com.hp.jipp.encoding.EnumType;
import com.hp.jipp.encoding.StringType;
import com.hp.jipp.encoding.Tag;
import com.hp.jipp.encoding.UriType;

import java.net.URI;

/** A library of attribute types as defined by RFC2911 */
public final class Attributes {

    // RFC2911 3.1.4.1 Request Operation Attributes
    // RFC2911 3.1.4.2 Response Operation Attributes

    public static final AttributeType<String> AttributesCharset =
            new StringType(Tag.Charset, "attributes-charset");

    public static final AttributeType<String> AttributesNaturalLanguage =
            new StringType(Tag.NaturalLanguage, "attributes-natural-language");

    // RFC2911 3.1.6 Operation Response Status Codes and Status Messages
    // Actually this is NOT a string but a LangString or something
//    public static final AttributeType<String> StatusMessage =
//            new StringType(Tag.TextWithLanguage, "status-message");

    public static final AttributeType<URI> PrinterUri =
            new UriType(Tag.Uri, "printer-uri");

    public static final AttributeType<Operation> OperationsSupported =
            new EnumType<>(Operation.Encoder, Tag.EnumValue,  "operations-supported");

    // TODO: Add the other 1001...

}
