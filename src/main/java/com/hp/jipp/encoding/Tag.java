package com.hp.jipp.encoding;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Value and delimiter tags as specified by RFC2910 and RFC3382
 */
@AutoValue
public abstract class Tag {
    public static final Tag OperationAttributes = create("operation-attributes", (byte)0x01);
    public static final Tag JobAttributes = create("job-attributes", (byte)0x02);
    public static final Tag EndOfAttributes = create("end-of-attributes", (byte)0x03);
    public static final Tag PrinterAttributes = create("printer-attributes", (byte)0x04);
    public static final Tag UnsupportedAttributes = create("unsupported-attributes", (byte)0x05);

    public static final Tag Unsupported = create("unsupported", (byte)0x10);
    public static final Tag Unknown = create("unknown", (byte)0x12);
    public static final Tag NoValue = create("no-value", (byte)0x13);

    // Integer values
    public static final Tag IntegerValue = create("integer", (byte)0x21);
    public static final Tag BooleanValue = create("boolean", (byte)0x22);
    public static final Tag EnumValue = create("enum", (byte)0x23);

    // Octet-string values
    public static final Tag OctetString = create("octetString", (byte)0x30);
    public static final Tag DateTime = create("dateTime", (byte)0x31);
    public static final Tag Resolution = create("resolution", (byte)0x32);
    public static final Tag RangeOfInteger = create("rangeOfInteger", (byte)0x33);
    public static final Tag BeginCollection = create("begCollection", (byte)0x34);
    public static final Tag TextWithLanguage = create("textWithLanguage", (byte)0x35);
    public static final Tag NameWithLanguage = create("nameWithLanguage", (byte)0x36);
    public static final Tag EndCollection = create("endCollection", (byte)0x37);

    // Character-string values
    public static final Tag TextWithoutLanguage = create("textWithoutLanguage", (byte)0x41);
    public static final Tag NameWithoutLanguage = create("nameWithoutLanguage", (byte)0x42);
    public static final Tag Keyword = create("keyword", (byte)0x44);
    public static final Tag Uri = create("uri", (byte)0x45);
    public static final Tag UriScheme = create("uriScheme", (byte)0x46);
    public static final Tag Charset = create("charset", (byte)0x47);
    public static final Tag NaturalLanguage = create("naturalLanguage", (byte)0x48);
    public static final Tag MimeMediaType = create("mimeMediaType", (byte)0x49);
    public static final Tag MemberAttributeName = create("memberAttrName", (byte)0x4A);

    public final static ImmutableSet<Tag> All = new ImmutableSet.Builder<Tag>().add(
            OperationAttributes, JobAttributes, EndOfAttributes, PrinterAttributes,
            UnsupportedAttributes, Unsupported, Unknown, NoValue, IntegerValue, BooleanValue,
            EnumValue, OctetString, DateTime, Resolution, RangeOfInteger, BeginCollection,
            TextWithLanguage, NameWithLanguage, EndCollection, TextWithoutLanguage,
            NameWithoutLanguage, Keyword, Uri, UriScheme, Charset, NaturalLanguage,
            MimeMediaType, MemberAttributeName
    ).build();

    private final static ImmutableMap<Byte, Tag> CODE_TO_TAG;
    static {
        ImmutableMap.Builder<Byte, Tag> builder = new ImmutableMap.Builder<>();
        for (Tag op : All) {
            builder.put(op.getValue(), op);
        }
        CODE_TO_TAG = builder.build();
    }

    /**
     * Return or create a tag corresponding to the value. This is not particularly
     * efficient for unrecognized tags.
     *
     * Known tags can be tested for equality with ==.
     */
    public static Tag toTag(byte value) {
        Tag tag = CODE_TO_TAG.get(value);
        if (tag != null) return tag;
        return create("UNKNOWN(x" + Integer.toHexString((int)value) + ")", value);
    }

    /** Read and return a tag from the input stream */
    public static Tag read(DataInputStream in) throws IOException {
        return toTag(in.readByte());
    }

    /**
     * Returns a new instance
     * @param name human-readable name of the the operation
     * @param value machine-readable identifier for the operation
     */
    public static Tag create(String name, byte value) {
        return new AutoValue_Tag(name, value);
    }

    abstract public String getName();
    abstract public byte getValue();

    /** Return true if this tag is a delimiter tag */
    public boolean isDelimiter() {
        return getValue() >= 0x01 && getValue() < 0x10;
    }

    @Override
    public final String toString() {
        return getName();
    }
}
