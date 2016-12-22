package com.hp.jipp.encoding;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.hp.jipp.model.Operation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A request packet as specified in RFC2910.
 */
@AutoValue
public abstract class Packet {
    /** Default version number to be sent in a packet (0x0101 for v1.1). */
    public static final int DEFAULT_VERSION_NUMBER = 0x0101;
    private static final byte[] EMPTY_DATA = new byte[0];

    abstract public int getVersionNumber();

    /**
     * Return this packet's operation code (for a request) or status code (for a response).
     */
    abstract public int getOperation();

    /**
     * Return the request ID for this packet
     */
    abstract public int getRequestId();

    /**
     * Return the attribute groups in this packet
     */
    abstract public ImmutableList<AttributeGroup> getAttributeGroups();

    /**
     * Return the packet's data field (bytes found after all attributes)
     */
    @SuppressWarnings("mutable")
    abstract public byte[] getData();

    /** Construct and return a builder for creating packets */
    public static Builder builder() {
        return new AutoValue_Packet.Builder().setVersionNumber(DEFAULT_VERSION_NUMBER)
                .setAttributeGroups(ImmutableList.<AttributeGroup>of()).setData(EMPTY_DATA);
    }

    /** Construct and return a builder based on an existing packet */
    public static Builder builder(Packet source) {
        return new AutoValue_Packet.Builder(source);
    }

    /** Write the contents of this object to the output stream as per RFC2910 */
    public void write(DataOutputStream out) throws IOException {
        out.writeShort(getVersionNumber());
        out.writeShort(getOperation());
        out.writeInt(getRequestId());
        for (AttributeGroup group : getAttributeGroups()) {
            group.write(out);
        }
        out.writeByte(Tags.EndOfAttributes);
        out.write(getData());
    }

    /** Read the contents of the input stream, returning a parsed Packet or throwing an exception */
    public static Packet read(DataInputStream in) throws IOException {
        Packet.Builder builder = builder().setVersionNumber(in.readShort())
                .setOperation(in.readShort()).setRequestId(in.readInt());
        ImmutableList.Builder<AttributeGroup> attributeGroupsBuilder =
                new ImmutableList.Builder<>();

        boolean moreAttributes = true;
        while(moreAttributes) {
            int tag = in.readByte();
            if (tag == Tags.EndOfAttributes) {
                if (in.available() > 0) {
                    byte data[] = new byte[in.available()];
                    int size = in.read(data);
                    if (size != data.length) throw new IOException(
                            "Failed to read " + data.length + ": " + size);
                    builder.setData(data);
                }
                moreAttributes = false;
            } else if (Tags.isDelimiter(tag)) {
                AttributeGroup attributeGroup = AttributeGroup.read(tag, in);
                attributeGroupsBuilder.add(attributeGroup);
            } else {
                throw new IOException("Illegal delimiter tag " + tag);
            }
        }
        builder.setAttributeGroups(attributeGroupsBuilder.build());
        return builder.build();
    }

    @AutoValue.Builder
    abstract public static class Builder {
        abstract public Builder setVersionNumber(int versionNumber);
        abstract public Builder setOperation(int operation);
        public Builder setOperation(Operation operation) {
            return setOperation(operation.getValue());
        }
        abstract public Builder setRequestId(int requestId);
        abstract public Builder setAttributeGroups(ImmutableList<AttributeGroup> groups);
        public Builder setAttributeGroups(Iterable<AttributeGroup> groups) {
            return setAttributeGroups(ImmutableList.copyOf(groups));
        }
        public Builder setAttributeGroup(AttributeGroup group) {
            return setAttributeGroups(ImmutableList.of(group));
        }
        abstract public Builder setData(byte[] data);
        abstract public Packet build();
    }
}
