package com.hp.jipp.encoding;

import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;

/**
 * Value and delimiter tags as specified by RFC2910 and RFC3382
 */
@AutoValue
public abstract class Tag {
    // Delimiter tags
    public static final Tag OperationAttributes = create("operation-attributes", 0x01);
    public static final Tag JobAttributes = create("job-attributes", 0x02);
    public static final Tag EndOfAttributes = create("end-of-attributes", 0x03);
    public static final Tag PrinterAttributes = create("printer-attributes", 0x04);
    public static final Tag UnsupportedAttributes = create("unsupported-attributes", 0x05);

    public static final Tag Unsupported = create("unsupported", 0x10);
    public static final Tag Unknown = create("unknown", 0x12);
    public static final Tag NoValue = create("no-value", 0x13);

    // Integer values
    public static final Tag IntegerValue = create("integer", 0x21);
    public static final Tag BooleanValue = create("boolean", 0x22);
    public static final Tag EnumValue = create("enum", 0x23);

    // Octet-string values
    public static final Tag OctetString = create("octetString", 0x30);
    public static final Tag DateTime = create("dateTime", 0x31);
    public static final Tag Resolution = create("resolution", 0x32);
    public static final Tag RangeOfInteger = create("rangeOfInteger", 0x33);
    public static final Tag BeginCollection = create("begCollection", 0x34);
    public static final Tag TextWithLanguage = create("textWithLanguage", 0x35);
    public static final Tag NameWithLanguage = create("nameWithLanguage", 0x36);
    public static final Tag EndCollection = create("endCollection", 0x37);

    // Character-string values
    public static final Tag TextWithoutLanguage = create("textWithoutLanguage", 0x41);
    public static final Tag NameWithoutLanguage = create("nameWithoutLanguage", 0x42);
    public static final Tag Keyword = create("keyword", 0x44);
    public static final Tag Uri = create("uri", 0x45);
    public static final Tag UriScheme = create("uriScheme", 0x46);
    public static final Tag Charset = create("charset", 0x47);
    public static final Tag NaturalLanguage = create("naturalLanguage", 0x48);
    public static final Tag MimeMediaType = create("mimeMediaType", 0x49);
    public static final Tag MemberAttributeName = create("memberAttrName", 0x4A);

    private final static Set<Tag> All = ImmutableSet.of(
            OperationAttributes, JobAttributes, EndOfAttributes, PrinterAttributes,
            UnsupportedAttributes, Unsupported, Unknown, NoValue, IntegerValue, BooleanValue,
            EnumValue, OctetString, DateTime, Resolution, RangeOfInteger, BeginCollection,
            TextWithLanguage, NameWithLanguage, EndCollection, TextWithoutLanguage,
            NameWithoutLanguage, Keyword, Uri, UriScheme, Charset, NaturalLanguage,
            MimeMediaType, MemberAttributeName
    );

    private final static ImmutableMap<Integer, Tag> CodeToTag;
    static {
        ImmutableMap.Builder<Integer, Tag> builder = new ImmutableMap.Builder<>();
        for (Tag op : All) {
            builder.put(op.getValue(), op);
        }
        CodeToTag = builder.build();
    }

    /**
     * Return or toAttribute a tag corresponding to the value. This is not particularly
     * efficient for unrecognized tags.
     *
     * Known tags can be tested for equality with ==.
     */
    public static Tag toTag(int value) {
        Optional<Tag> tag = Optional.fromNullable(CodeToTag.get(value));
        if (tag.isPresent()) return tag.get();
        return create("UNKNOWN(x" + Integer.toHexString(value) + ")", value);
    }

    /** Read and return a tag from the input stream */
    public static Tag read(DataInputStream in) throws IOException {
        return toTag(in.readByte());
    }

    /** Write this tag to the output stream */
    public void write(DataOutputStream out) throws IOException {
        out.writeByte((byte)getValue());
    }

    /**
     * Returns a new instance
     * @param name human-readable name
     * @param value machine-readable identifier
     */
    public static Tag create(String name, int value) {
        return new AutoValue_Tag(name, value);
    }

    abstract public String getName();
    abstract public int getValue();

    /** Return true if this tag is a delimiter tag */
    public boolean isDelimiter() {
        return getValue() >= 0x01 && getValue() < 0x10;
    }

    @Override
    public final String toString() {
        return getName();
    }
}
