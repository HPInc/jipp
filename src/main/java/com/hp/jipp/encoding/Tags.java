package com.hp.jipp.encoding;

import java.util.HashMap;

/**
 * Value Tags as specified by RFC2910 (https://tools.ietf.org/html/rfc2910).
 */
public class Tags {
    public static final byte OperationAttributes = (byte)0x01;
    public static final byte JobAttributes = (byte)0x02;
    public static final byte EndOfAttributes = (byte)0x03;
    public static final byte PrinterAttributes = (byte)0x04;
    public static final byte UnsupportedAttributes = (byte)0x05;

    public static final byte Unsupported = (byte)0x10;
    public static final byte Unknown = (byte)0x12;
    public static final byte NoValue = (byte)0x12;

    // Integer values
    public static final byte Integer = (byte)0x21;
    public static final byte Boolean = (byte)0x22;
    public static final byte Enum = (byte)0x23;

    // Octet-string values
    public static final byte UnspecifiedOctet = (byte)0x30;
    public static final byte DateTime = (byte)0x31;
    public static final byte Resolution = (byte)0x32;
    public static final byte RangeOfInteger = (byte)0x33;
    public static final byte BeginCollection = (byte)0x34;
    public static final byte TextWithLanguage = (byte)0x35;
    public static final byte NameWithLanguage = (byte)0x36;
    public static final byte EndCollection = (byte)0x37;

    // Character-string values
    public static final byte TextWithoutLanguage = (byte)0x41;
    public static final byte NameWithoutLanguage = (byte)0x42;
    public static final byte Keyword = (byte)0x44;
    public static final byte Uri = (byte)0x45;
    public static final byte UriScheme = (byte)0x46;
    public static final byte Charset = (byte)0x47;
    public static final byte NaturalLanguage = (byte)0x48;
    public static final byte MimeMedia = (byte)0x49;
    public static final byte MemberAttributeName = (byte)0x4A;

    public static boolean isDelimiter(int tag) {
        return tag >= OperationAttributes && tag <= 0x0F;
    }

    private final static HashMap<Byte, String> map = new HashMap<Byte, String>() {{
        put(OperationAttributes, "operationAttributes");
        put(JobAttributes, "jobAttributes");
        put(EndOfAttributes, "endOfAttributes");
        put(PrinterAttributes, "printerAttributes");
        put(UnsupportedAttributes, "unsupportedAttributes");
        put(Integer, "integer");
        put(Boolean, "boolean");
        put(Enum, "enum");
        put(UnspecifiedOctet, "unspecifiedOctet");
        put(DateTime, "dateTime");
        put(Resolution, "resolution");
        put(RangeOfInteger, "RangeOfInteger");
        put(BeginCollection, "begCollection");
        put(TextWithLanguage, "textWithLanguage");
        put(NameWithLanguage, "nameWithLanguage");
        put(EndCollection, "endCollection");
        put(TextWithoutLanguage, "textWithoutLanguage");
        put(NameWithoutLanguage, "nameWithoutLanguage");
        put(Keyword, "keyword");
        put(Uri, "uri");
        put(UriScheme, "uriScheme");
        put(Charset, "charset");
        put(NaturalLanguage, "naturalLanguage");
        put(MimeMedia, "mimeMedia");
        put(MemberAttributeName, "memberAttributeName");
    }};

    public static String toString(int id) {
        String result = map.get((byte)id);
        if (result == null) {
            result = "0x" + java.lang.Integer.toHexString(id);
        }
        return result;
    }
}
