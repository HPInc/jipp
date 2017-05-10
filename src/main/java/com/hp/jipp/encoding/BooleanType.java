package com.hp.jipp.encoding;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/** A boolean attribute type */
public class BooleanType extends AttributeType<Boolean> {
    private static final String TYPE_NAME = "Boolean";

    static final Attribute.SimpleEncoder<Boolean> ENCODER = new Attribute.SimpleEncoder<Boolean>(TYPE_NAME) {
        @Override
        public void writeValue(DataOutputStream out, Boolean value) throws IOException {
            out.writeShort(1);
            out.writeByte(value ? 0x01 : 0x00);
        }

        @Override
        public Boolean readValue(DataInputStream in, Tag valueTag) throws IOException {
            Attribute.expectLength(in, 1);
            return in.readByte() != 0;
        }

        @Override
        public boolean valid(Tag valueTag) {
            return valueTag == Tag.BooleanValue;
        }
    };

    public BooleanType(Tag tag, String name) {
        super(ENCODER, tag, name);
    }
}
