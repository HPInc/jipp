package com.hp.jipp.encoding;

import com.google.auto.value.AutoValue;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A request packet as specified in RFC2910.
 */
@AutoValue
public abstract class Packet {
    /** Default version number to be sent in a packet (0x0101 for IPP 1.1) */
    public static final int DEFAULT_VERSION_NUMBER = 0x0101;

    private static final byte[] EMPTY_DATA = new byte[0];

    /** Construct and return a builder for creating packets */
    public static Builder builder() {
        return new AutoValue_Packet.Builder().setVersionNumber(DEFAULT_VERSION_NUMBER)
                .setAttributeGroups(ImmutableList.<AttributeGroup>of()).setData(EMPTY_DATA);
    }

    /** Construct and return a builder based on an existing packet */
    public static Builder builder(Packet source) {
        return new AutoValue_Packet.Builder(source);
    }

    /**
     * Construct a packet containing the default version number and the specified operation
     * and request ID
     */
    public static Builder builder(NameCode code, int requestId) {
        return builder().setCode(code).setRequestId(requestId);
    }

    /**
     * Construct and return a complete packet
     */
    public static Packet create(NameCode code, int requestId, AttributeGroup... groups) {
        return builder(code, requestId).setAttributeGroups(Arrays.asList(groups)).build();
    }

    abstract public int getVersionNumber();

    /**
     * Return this packet's code.
     */
    abstract public int getCode();

    /**
     * Return a NameCode corresponding to this packet's code.
     */
    public <T extends NameCode> T getCode(EnumType.Encoder<T> encoder) {
        return encoder.get(getCode());
    }

    /**
     * Return the request ID for this packet
     */
    abstract public int getRequestId();

    /**
     * Return the attribute groups in this packet
     */
    abstract public List<AttributeGroup> getAttributeGroups();

    /** Returns the first attribute with the specified delimiter */
    public Optional<AttributeGroup> getAttributeGroup(Tag delimiter) {
        for (AttributeGroup group : getAttributeGroups()) {
            if (group.getTag() == delimiter) return Optional.of(group);
        }
        return Optional.absent();
    }

    /**
     * Return the packet's data field (bytes found after all attributes)
     */
    @SuppressWarnings("mutable")
    abstract public byte[] getData();

    /** Write the contents of this object to the output stream as per RFC2910 */
    public void write(DataOutputStream out) throws IOException {
        out.writeShort(getVersionNumber());
        out.writeShort(getCode());
        out.writeInt(getRequestId());
        for (AttributeGroup group : getAttributeGroups()) {
            group.write(out);
        }
        out.writeByte(Tag.EndOfAttributes.getValue());
        out.write(getData());
    }

    /** Write the entire contents of this packet to a single byte array */
    public byte[] getBytes() {
        try (ByteArrayOutputStream outBytes = new ByteArrayOutputStream()) {
            write(new DataOutputStream(outBytes));
            return outBytes.toByteArray();
        } catch (IOException ignored) {
            throw new IllegalArgumentException("Packet could not be written");
        }
    }

    /**
     * Read the contents of the input stream, returning a parsed Packet or throwing an exception.
     * Note: the input stream is not closed.
     */
    public static Packet read(DataInputStream in) throws IOException {
        Packet.Builder builder = builder().setVersionNumber(in.readShort())
                .setCode(in.readShort()).setRequestId(in.readInt());
        ImmutableList.Builder<AttributeGroup> attributeGroupsBuilder =
                new ImmutableList.Builder<>();

        boolean moreAttributes = true;
        while(moreAttributes) {
            Tag tag = Tag.read(in);
            if (tag == Tag.EndOfAttributes) {
                if (in.available() > 0) {
                    byte data[] = new byte[in.available()];
                    int size = in.read(data);
                    if (size != data.length) throw new ParseError("Failed to read " + data.length + ": " + size);
                    builder.setData(data);
                }
                moreAttributes = false;
            } else if (tag.isDelimiter()) {
                AttributeGroup attributeGroup = AttributeGroup.read(tag, in);
                attributeGroupsBuilder.add(attributeGroup);
            } else {
                throw new ParseError("Illegal delimiter tag " + tag);
            }
        }
        builder.setAttributeGroups(attributeGroupsBuilder.build());
        return builder.build();
    }

    @AutoValue.Builder
    abstract public static class Builder {
        abstract public Builder setVersionNumber(int versionNumber);
        abstract public Builder setCode(int code);
        public Builder setCode(NameCode code) {
            return setCode(code.getCode());
        }
        abstract public Builder setRequestId(int requestId);
        abstract public Builder setAttributeGroups(List<AttributeGroup> groups);
        public Builder setAttributeGroups(AttributeGroup... groups) {
            return setAttributeGroups(Arrays.asList(groups));
        }
        abstract public Builder setData(byte[] data);
        abstract public Packet build();
    }

    /** Describes a packet including its proper code and attribute types */
    public final String describe(EnumType.Encoder<?> codeEncoder, List<AttributeType<?>> attributeTypes) {
        // Construct a map of attribute names to attributeTypes for speed
        final Map<String, AttributeType<?>> attributeTypeMap = Maps.uniqueIndex(attributeTypes,
                new Function<AttributeType<?>, String>() {
                    @Override
                    public String apply(AttributeType<?> type) {
                        return type.getName();
                    }
                });

        String attributeGroups = Lists.transform(getAttributeGroups(), new Function<AttributeGroup, String>() {
            @Override
            public String apply(AttributeGroup input) {
                return input.describe(attributeTypeMap);
            }
        }).toString();

        return "Packet{v=x" + Integer.toHexString(getVersionNumber()) +
                ", code=" + codeEncoder.get(getCode()) +
                ", rId=x" + Integer.toHexString(getRequestId()) +
                ", ags=" + attributeGroups +
                (getData().length == 0 ? "" : ", dLen=" + getData().length) +
                "}";
    }

    @Override
    public final String toString() {
        return "Packet{v=x" + Integer.toHexString(getVersionNumber()) +
                ", code=x" + Integer.toHexString(getCode()) +
                ", rId=x" + Integer.toHexString(getRequestId()) +
                ", ags=" + getAttributeGroups() +
                (getData().length == 0 ? "" : ", dLen=" + getData().length) +
                "}";
    }
}
