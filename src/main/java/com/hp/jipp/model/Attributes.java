package com.hp.jipp.model;

import com.hp.jipp.encoding.Attribute;
import com.hp.jipp.encoding.AttributeType;
import com.hp.jipp.encoding.Tag;

import java.net.URI;

/** A library of attribute types as defined by RFC2911 */
public class Attributes {

    public static final AttributeType<String> AttributesNaturalLanguage =
            Attribute.stringType(Tag.NaturalLanguage, "attributes-natural-language");

    public static final AttributeType<String> AttributesCharset =
            Attribute.stringType(Tag.Charset, "attributes-charset");

    public static final AttributeType<URI> PrinterUri =
            Attribute.uriType(Tag.Uri, "printer-uri");

    public static final AttributeType<Operation> OperationsSupported =
            Attribute.enumType(Operation.Encoder, Tag.EnumValue, Operation.NAME);

    // TODO: Add the other 1001...

}
