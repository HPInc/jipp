package com.hp.jipp.model;

import com.hp.jipp.encoding.AttributeType;
import com.hp.jipp.encoding.EnumType;
import com.hp.jipp.encoding.StringType;
import com.hp.jipp.encoding.Tag;
import com.hp.jipp.encoding.UriType;

import java.net.URI;

/** A library of attribute types as defined by RFC2911 */
public class Attributes {

    public static final AttributeType<String> AttributesNaturalLanguage =
            new StringType(Tag.NaturalLanguage, "attributes-natural-language");

    public static final AttributeType<String> AttributesCharset =
            new StringType(Tag.Charset, "attributes-charset");

    public static final AttributeType<URI> PrinterUri =
            new UriType(Tag.Uri, "printer-uri");

    public static final AttributeType<Operation> OperationsSupported =
            new EnumType<>(Operation.Encoder, Tag.EnumValue,  "operations-supported");

    // TODO: Add the other 1001...

}
