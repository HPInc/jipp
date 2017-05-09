package com.hp.jipp.encoding;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ResolutionType extends AttributeType<Resolution> {
    private static final String TYPE_NAME = "Resolution";

    private static final int INT_LENGTH = 4;
    private static final int BYTE_LENGTH = 1;

    static final Attribute.Encoder<Resolution> ENCODER = new Attribute.Encoder<Resolution>(TYPE_NAME) {
        @Override
        public Resolution readValue(DataInputStream in, Tag valueTag) throws IOException {
            Attribute.expectLength(in, INT_LENGTH + INT_LENGTH + BYTE_LENGTH);
            return Resolution.of(in.readInt(), in.readInt(),
                    Resolution.Unit.ENCODER.get(in.readByte()));
        }

        @Override
        public void writeValue(DataOutputStream out, Resolution value) throws IOException {
            out.writeShort(INT_LENGTH + INT_LENGTH + BYTE_LENGTH);
            out.writeInt(value.getCrossFeedResolution());
            out.writeInt(value.getFeedResolution());
            out.writeByte((byte) value.getUnit().getCode());
        }

        @Override
        public boolean valid(Tag valueTag) {
            return Tag.Resolution.equals(valueTag);
        }
    };

    public ResolutionType(Tag tag, String name) {
        super(ENCODER, tag, name);
    }
}
